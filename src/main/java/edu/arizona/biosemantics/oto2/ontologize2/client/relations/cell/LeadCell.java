package edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
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
		final Row row = termsGrid.getRow(rowIndex);
		Menu menu = new Menu();
		MenuItem removeItem = new MenuItem("Remove this row");
		removeItem.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				Vertex targetVertex = row.getLead();
				for(final Relation r : g.getInRelations(targetVertex, termsGrid.getType())) {
					final MessageBox box = Alerter.showYesNoCancelConfirm("Remove relation", "You are about to remove the relation " + r.toString() + ".\n" +
							"Do you want to remove all children of " + r.getDestination() + " or attach them instead to " + r.getSource());
					box.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler() {
						@Override
						public void onSelect(SelectEvent event) {
							eventBus.fireEvent(new RemoveRelationEvent(true, r));
							/*for(GwtEvent<Handler> e : createRemoveEvents(true, relation)) {
								eventBus.fireEvent(e);
							}*/
							box.hide();
						}
					});
					box.getButton(PredefinedButton.NO).addSelectHandler(new SelectHandler() {
						@Override
						public void onSelect(SelectEvent event) {
							eventBus.fireEvent(new RemoveRelationEvent(false, r));
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
		});
		menu.add(removeItem);
		
		return menu;
	}

	@Override
	public void render(Context context, Vertex value, SafeHtmlBuilder sb) {
		SafeHtml rendered = templates.cell("", columnHeaderStyles.headInner(),
				columnHeaderStyles.headButton(), valueProvider.getValue(value), "", "#009933", "");
		sb.append(rendered);
	}

}
