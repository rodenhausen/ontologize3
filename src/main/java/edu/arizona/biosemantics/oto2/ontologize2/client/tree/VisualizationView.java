package edu.arizona.biosemantics.oto2.ontologize2.client.tree;

import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;

import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class VisualizationView extends SimpleContainer {

	private EventBus eventBus;

	public VisualizationView(final EventBus eventBus) {
		this.eventBus = eventBus;
		
		SubclassTreeView subclassTree = new SubclassTreeView(eventBus);
		PartsTreeView partsTree = new PartsTreeView(eventBus);
		//TreeView synonymsTree = new TreeView(eventBus, Type.SYNONYM_OF);
		
		//VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		//vlc.add(subclassTree, new VerticalLayoutData(1, 0.5));
		//vlc.add(partsTree, new VerticalLayoutData(1, 0.5));
		//vlc.add(synonymsTree, new VerticalLayoutData(1, 0.33));
		//this.setWidget(vlc);
		
		TabPanel tabPanel = new TabPanel();
		tabPanel.setTabScroll(true);
		tabPanel.setAnimScroll(true);
		tabPanel.add(subclassTree, new TabItemConfig("Categories", false));
		tabPanel.add(partsTree, new TabItemConfig("Parts", false));
		//tabPanel.add(synonymGrid, new TabItemConfig("Synonyms", false));
		this.add(tabPanel);
	}
}
