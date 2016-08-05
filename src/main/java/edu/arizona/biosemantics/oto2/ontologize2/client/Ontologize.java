package edu.arizona.biosemantics.oto2.ontologize2.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.box.MultiLinePromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuBar;
import com.sencha.gxt.widget.core.client.menu.MenuBarItem;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

import edu.arizona.biosemantics.oto2.ontologize2.client.relations.RelationsView;
import edu.arizona.biosemantics.oto2.ontologize2.client.candidate.CandidateView;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.info.ContextView;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.TreeView;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.VisualizationView;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionServiceAsync;

public class Ontologize extends SimpleContainer {

	public class MenuView extends MenuBar {

		private EventBus eventBus;
		private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);

		public MenuView() {
			Menu sub = new Menu();
			MenuBarItem item = new MenuBarItem("File", sub);
			MenuItem generateItem = new MenuItem("Generate OWL");
			generateItem.addSelectionHandler(new SelectionHandler<Item>() {
				@Override
				public void onSelection(SelectionEvent<Item> event) {
					collectionService.getOWL(ModelController.getCollection().getId(), 
							ModelController.getCollection().getSecret(), new AsyncCallback<String>() {
							@Override
							public void onFailure(Throwable caught) {
								Alerter.showAlert("Generate OWL", "Failed to generate OWL", caught);
							}
							@Override
							public void onSuccess(String result) {
								final MultiLinePromptMessageBox box = new MultiLinePromptMessageBox("OWL", "");
								box.setModal(true);
								box.getTextArea().setText(result);
								box.show();
							}
					});
				}
			});
			sub.add(generateItem);
			add(item);
		}
	}
	
	public static String user;
	
	private EventBus eventBus = new SimpleEventBus();
	private ModelController modelController;
	
	public Ontologize() {
		MenuView menuView = new MenuView();
		BorderLayoutContainer blc = new BorderLayoutContainer();
		
		CandidateView candidateView = new CandidateView(eventBus);
		ContentPanel cp = new ContentPanel();
		cp.setHeadingText("Candidates");
		cp.add(candidateView);
		BorderLayoutData d = new BorderLayoutData(.30);
		d.setMargins(new Margins(0, 0, 0, 0));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		blc.setWestWidget(cp, d);
		
		VisualizationView visualizationView = new VisualizationView(eventBus);
		cp = new ContentPanel();
		cp.setHeadingText("Trees");
		cp.add(visualizationView);
		d = new BorderLayoutData(.30);
		d.setMargins(new Margins(0, 0, 0, 0));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		blc.setEastWidget(cp, d);
		
		ContextView contextView = new ContextView(eventBus);
		cp = new ContentPanel();
		cp.setHeadingText("Context");
		cp.add(contextView);
		d = new BorderLayoutData(.30);
		d.setMargins(new Margins(0, 0, 0, 0));
		d.setCollapsible(true);
		d.setSplit(true);
		d.setCollapseMini(true);
		blc.setSouthWidget(cp, d);
		
		RelationsView relationsView = new RelationsView(eventBus);
		cp = new ContentPanel();
		cp.setHeadingText("Relations");
		cp.add(relationsView);
		d = new BorderLayoutData();
		d.setMargins(new Margins(0, 0, 0, 0));
		blc.setCenterWidget(cp, d);
		
		
		modelController = new ModelController(eventBus);
		

		VerticalLayoutContainer verticalLayoutContainer = new VerticalLayoutContainer();
		verticalLayoutContainer.add(menuView, new VerticalLayoutData(1,-1));
		verticalLayoutContainer.add(blc, new VerticalLayoutData(1,1));
		this.setWidget(verticalLayoutContainer);
	}

	public EventBus getEventBus() {
		return eventBus;
	}

}
