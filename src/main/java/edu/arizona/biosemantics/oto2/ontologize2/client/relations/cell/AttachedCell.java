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
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class AttachedCell extends MenuExtendedCell<Row> {

	public interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div class=\"{0}\" qtip=\"{4}\">" +
				"<div class=\"{1}\" " +
				"style=\"" +
				"width: calc(100% - 9px); " +
				"height:14px; " +
				"background: no-repeat 0 0;" +
				"background-image:{6};" +
				"background-color:{5};" +
				"color: {7};" +
				"\">{3}<a class=\"{2}\" style=\"height: 22px;\"></a>" +
				"</div>" +
				"</div>")
		SafeHtml cell(String grandParentStyleClass, String parentStyleClass,
				String aStyleClass, String value, String quickTipText, String colorHex, String backgroundImage, String color);
	}
	
	protected EventBus eventBus;
	private int i;
	private TermsGrid termsGrid;
	protected static Templates templates = GWT.create(Templates.class);
	
	public AttachedCell(EventBus eventBus, TermsGrid termsGrid, int i) {
		this.eventBus = eventBus;
		this.termsGrid = termsGrid;
		this.i = i;
	}
	
	/**
	 * create context menu items for cells such as Remove items
	 */
	@Override
	protected Menu createContextMenu(int columnIndex, int rowIndex) {
		final Row row = termsGrid.getRow(rowIndex);
		final Edge r = row.getAttached().get(columnIndex - 1);
		Menu menu = new Menu();
		MenuItem removeItem = new MenuItem("Remove this " + termsGrid.getType().getTargetLabel());
		removeItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				if(g.getInRelations(r.getDest(), termsGrid.getType()).size() <= 1) {
					if(g.getOutRelations(r.getDest(), termsGrid.getType()).isEmpty()) {
						eventBus.fireEvent(new RemoveRelationEvent(false, r));
					} else {
						doAskForRecursiveRemoval(r);
					}
				} else {
					eventBus.fireEvent(new RemoveRelationEvent(false, r));
				}
			}
		});
		MenuItem context = new MenuItem("Show Term Context");
		context.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				eventBus.fireEvent(new SelectTermEvent(r.getDest().getValue()));
			}
		});
		menu.add(removeItem);
		menu.add(context);
		return menu;
	}
	
	protected void doAskForRecursiveRemoval(final Edge relation) {
		OntologyGraph g = ModelController.getCollection().getGraph();
		List<Vertex> targets = new LinkedList<Vertex>();
		for(Edge r : g.getOutRelations(relation.getDest(), termsGrid.getType())) 
			targets.add(r.getDest());
		final MessageBox box = Alerter.showYesNoCancelConfirm("Remove " + termsGrid.getType().getTargetLabel(), 
				"You are about to remove " + termsGrid.getType().getTargetLabel() + "<i>" + relation.getDest() + "</i>"
				+ " from <i>" + relation.getSrc() + "</i>.\n" +
				"Do you want to remove all " + termsGrid.getType().getTargetLabelPlural() + " of <i>" + relation.getDest() + "</i>" +
				" or make them instead a " + termsGrid.getType().getTargetLabel() + " of <i>" + relation.getSrc() + "</i>?");
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

	/*private List<GwtEvent> createRemoveEvents(boolean recursive, Relation relation) {
		List<GwtEvent> result = new LinkedList<GwtEvent>();
		if(recursive) {
			OntologyGraph graph = ModelController.getCollection().getGraph();
			
			List<Edge> targetOutEdges = graph.getOutEdges(relation.getTarget(), termsGrid.getType());
			if(targetOutEdges.isEmpty()) {
				result.add(new RemoveRelationEvent(relation));
			} else {
				for(Edge edge : targetOutEdges) {
					result.addAll(createRemoveEvents(recursive, graph.getRelation(edge)));
				}
			}
		} else {
			OntologyGraph graph = ModelController.getCollection().getGraph();
			
			List<Edge> targetOutEdges = graph.getOutEdges(relation.getTarget(), termsGrid.getType());
			if(targetOutEdges.isEmpty()) {
				result.add(new RemoveRelationEvent(relation));
			} else {
				for(Edge edge : targetOutEdges) {
					Relation childRelation = graph.getRelation(edge);
					childRelation.getTarget();
					result.add(new CreateRelationEvent(new Relation(relation.getSource(), childRelation.getTarget(), edge)));
					result.add(new RemoveRelationEvent(relation));
				}
			}
		}
		return result;
	}*/

	@Override
	public void render(Context context, Row value, final SafeHtmlBuilder sb) {
		if(value.getAttached().isEmpty() || value.getAttached().size() <= i)
			return;
		final Edge r = value.getAttached().get(i);
		String textColor = "#000000";
		String backgroundColor = "";// "#FFFFFF";
		OntologyGraph g = ModelController.getCollection().getGraph();
		if(g.getInRelations(r.getDest(), r.getType()).size() > 1) {
			backgroundColor = "#ffff00";
		}
		switch(r.getOrigin()) {
			case IMPORT:
				//backgroundColor = "#0033cc"; //blue
				break;
			case USER:
				break;
			default:
				break;
		}
		SafeHtml rendered = templates.cell("", columnHeaderStyles.headInner(),
				columnHeaderStyles.headButton(), r.getDest().getValue(), "", backgroundColor, 
				"", textColor);
		sb.append(rendered);
	}
}
