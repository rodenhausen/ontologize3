package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class RelationsView extends SimpleContainer {

	private EventBus eventBus;

	public RelationsView(final EventBus eventBus) {
		this.eventBus = eventBus;
		
		final PartsGrid partsGrid = new PartsGrid(eventBus);
		final SubclassesGrid subclassGrid = new SubclassesGrid(eventBus);
		final SynonymsGrid synonymGrid = new SynonymsGrid(eventBus);
		
		/*VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		vlc.add(subclassGrid.asWidget(), new VerticalLayoutData(1, 0.33));
		vlc.add(partsGrid.asWidget(), new VerticalLayoutData(1, 0.33));
		vlc.add(synonymGrid.asWidget(), new VerticalLayoutData(1, 0.33));
		this.add(vlc);*/
		
		TabPanel tabPanel = new TabPanel();
		tabPanel.setTabScroll(true);
		tabPanel.setAnimScroll(true);
		tabPanel.add(subclassGrid, new TabItemConfig("Is-a", false));
		tabPanel.add(partsGrid, new TabItemConfig("Parts", false));
		tabPanel.add(synonymGrid, new TabItemConfig("Synonyms", false));
		this.add(tabPanel);
	}

}
