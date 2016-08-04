package edu.arizona.biosemantics.oto2.ontologize2.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.ontologize2.client.relations.RelationsView;
import edu.arizona.biosemantics.oto2.ontologize2.client.candidate.CandidateView;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.info.ContextView;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.TreeView;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.VisualizationView;

public class Ontologize extends SimpleContainer {

	public static String user;
	
	private EventBus eventBus = new SimpleEventBus();
	private ModelController modelController;
	
	public Ontologize() {
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
		
		
		this.add(blc);
		modelController = new ModelController(eventBus);
	}

	public EventBus getEventBus() {
		return eventBus;
	}

}
