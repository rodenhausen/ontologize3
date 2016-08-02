package edu.arizona.biosemantics.oto2.ontologize2.client.tree;

import com.google.gwt.event.shared.EventBus;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class SynonymsTreeView extends TreeView {

	public SynonymsTreeView(EventBus eventBus) {
		super(eventBus, Type.SYNONYM_OF);
	}

}
