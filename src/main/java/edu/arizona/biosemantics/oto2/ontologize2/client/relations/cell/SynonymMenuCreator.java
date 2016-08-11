package edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class SynonymMenuCreator implements LeadCell.MenuCreator {

	private EventBus eventBus;
	private TermsGrid termsGrid;

	public SynonymMenuCreator(EventBus eventBus, TermsGrid termsGrid) {
		this.eventBus = eventBus;
		this.termsGrid = termsGrid;
	}
	
	@Override
	public Menu create(int rowIndex) {
		Menu menu = new Menu();
		final OntologyGraph g = ModelController.getCollection().getGraph();
		final Row row = termsGrid.getRow(rowIndex);
		
		MenuItem addItem = new MenuItem("Add " + termsGrid.getType().getTargetLabel());
		addItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				
				final PromptMessageBox box = Alerter.showPromptMessageBox("Add " + termsGrid.getType().getTargetLabel(), 
						"Add " + termsGrid.getType().getTargetLabel());
				box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						termsGrid.fire(new CreateRelationEvent(
								new Relation(row.getLead(), new Vertex(box.getTextField().getText()), new Edge(termsGrid.getType(), Source.USER))));
					}
				});
			}
		});
		
		MenuItem removeItem = new MenuItem("Remove all " + termsGrid.getType().getTargetLabelPlural());
		removeItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				Vertex targetVertex = row.getLead();
				for(final Relation r : g.getOutRelations(targetVertex, termsGrid.getType())) {
					if(g.getInRelations(r.getDestination(), termsGrid.getType()).size() <= 1) {
						if(g.getOutRelations(r.getDestination(), termsGrid.getType()).isEmpty()) {
							eventBus.fireEvent(new RemoveRelationEvent(false, r));
						} else {
							doAskForRecursiveRemoval(r);
						}
					} else {
						eventBus.fireEvent(new RemoveRelationEvent(false, r));
					}
				} 
			}
		});
		
		MenuItem removeRowItem = new MenuItem("Remove row");
		removeRowItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				Vertex targetVertex = row.getLead();
				eventBus.fireEvent(new RemoveRelationEvent(true, 
						g.getInRelations(targetVertex, termsGrid.getType()).iterator().next()));
			}
		});
		
		MenuItem context = new MenuItem("Show Term Context");
		context.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				eventBus.fireEvent(new SelectTermEvent(row.getLead().getValue()));
			}
		});
		menu.add(removeRowItem);
		menu.add(addItem);
		menu.add(removeItem);
		menu.add(context);
		
		return menu;
	}
	
	protected void doAskForRecursiveRemoval(final Relation relation) {
		OntologyGraph g = ModelController.getCollection().getGraph();
		List<Vertex> targets = new LinkedList<Vertex>();
		for(Relation r : g.getOutRelations(relation.getDestination(), termsGrid.getType())) 
			targets.add(r.getDestination());
		final MessageBox box = Alerter.showYesNoCancelConfirm("Remove " + termsGrid.getType().getTargetLabel(), 
				"You are about to remove " + termsGrid.getType().getTargetLabel() + "<i>" + relation.getDestination() + "</i>"
				+ " from <i>" + relation.getSource() + "</i>.\n" +
				"Do you want to remove all " + termsGrid.getType().getTargetLabelPlural() + " of <i>" + relation.getDestination() + "</i>" +
				" or make them instead a " + termsGrid.getType().getTargetLabel() + " of <i>" + relation.getSource() + "</i>?");
		box.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				eventBus.fireEvent(new RemoveRelationEvent(true, relation));
				/*for(GwtEvent<Handler> e : createRemoveEvents(true, relation)) {
					eventBus.fireEvent(e);
				}*/
				box.hide();
			}
		});
		box.getButton(PredefinedButton.NO).addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				eventBus.fireEvent(new RemoveRelationEvent(false, relation));
				/*for(GwtEvent<Handler> e : createRemoveEvents(false, relation)) {
					eventBus.fireEvent(e);
				}*/
				box.hide();
			}
		});
		box.getButton(PredefinedButton.CANCEL).addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				box.hide();
			}
		});
	}

}
