package edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sencha.gxt.core.client.ValueProvider;
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
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class LeadCell extends MenuExtendedCell<Vertex> {
	
	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div class=\"{0}\" qtip=\"{4}\">" +
				"<div class=\"{1}\" " +
				"style=\"" +
				"width: calc(100% - 9px); " +
				"height:14px; " +
				"background: no-repeat 0 0;" +
				"background-image:{6};" +
				"background-color:{5};" +
				"\">{3}<a class=\"{2}\" style=\"height: 22px;\"></a>" +
				"</div>" +
				"</div>")
		SafeHtml cell(String grandParentStyleClass, String parentStyleClass,
				String aStyleClass, String value, String quickTipText, String colorHex, String backgroundImage);
	}
	
	private TermsGrid termsGrid;
	private ValueProvider<Vertex, String> valueProvider;
	private EventBus eventBus;
	protected static Templates templates = GWT.create(Templates.class);
	
	public LeadCell(EventBus eventBus, TermsGrid termsGrid, ValueProvider<Vertex, String> valueProvider) {
		this.eventBus = eventBus;
		this.termsGrid = termsGrid;
		this.valueProvider = valueProvider;
	}
	
	@Override
	protected Menu createContextMenu(int column, int rowIndex) {
		Menu menu = new Menu();
		final OntologyGraph g = ModelController.getCollection().getGraph();
		final Row row = termsGrid.getRow(rowIndex);
		
		MenuItem addItem = new MenuItem("Add attached term");
		addItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				
				final PromptMessageBox box = Alerter.showPromptMessageBox("Attach term", "Attach term");
				box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						termsGrid.fire(new CreateRelationEvent(
								new Relation(row.getLead(), new Vertex(box.getTextField().getText()), new Edge(termsGrid.getType(), Source.USER))));
					}
				});
			}
		});
		
		MenuItem removeItem = new MenuItem("Remove all attached terms");
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
		MenuItem context = new MenuItem("Show Context");
		context.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				eventBus.fireEvent(new SelectTermEvent(row.getLead().getValue()));
			}
		});
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
		final MessageBox box = Alerter.showYesNoCancelConfirm("Remove relation", "You are about to remove the relation " + relation.toString() + ".\n" +
				"Do you want to remove all children of " + relation.getDestination() + " or attach them instead to " + relation.getSource());
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
	
	
	@Override
	public void render(Context context, Vertex value, SafeHtmlBuilder sb) {
		SafeHtml rendered = templates.cell("", columnHeaderStyles.headInner(),
				columnHeaderStyles.headButton(), valueProvider.getValue(value), "", "#009933", "");
		sb.append(rendered);
	}

}
