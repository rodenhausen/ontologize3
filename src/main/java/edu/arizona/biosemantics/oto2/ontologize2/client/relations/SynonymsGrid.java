package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.Arrays;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class SynonymsGrid extends MenuTermsGrid {

	public SynonymsGrid(EventBus eventBus) {
		super(eventBus, Type.SYNONYM_OF);
	}
	
	@Override
	protected void fire(GwtEvent<? extends EventHandler> e) {
		if(e instanceof CreateRelationEvent) {
			final CreateRelationEvent createRelationEvent = (CreateRelationEvent)e;
			OntologyGraph g = ModelController.getCollection().getGraph();
			for(Relation r : createRelationEvent.getRelations()) {
				try {
					g.isValidSynonym(r);
					eventBus.fireEvent(createRelationEvent);
				} catch(Exception ex) {
					final MessageBox box = Alerter.showAlert("Create synonym", ex.getMessage());
					box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
						@Override
						public void onSelect(SelectEvent event) {
							box.hide();
						}
					});
				}
			}
		} else if(e instanceof RemoveRelationEvent) {
			eventBus.fireEvent(e);
		} else {
			eventBus.fireEvent(e);
		}
	}
	
	@Override
	protected void onLoad(OntologyGraph g) {
		createEdges(g, g.getRoot(type));
	}
	
	@Override
	protected void createRelation(Relation r) {
		if(r.getEdge().getType().equals(type)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			if(r.getSource().equals(g.getRoot(type))) {
				if(!leadRowMap.containsKey(r.getDestination()))
					this.addRow(new Row(r.getDestination()));
			} else {
				super.createRelation(r);
			}
		}
	}
	
	@Override
	protected void addAttached(Row row, Relation... add) throws Exception {
		row.add(Arrays.asList(add));
		updateRow(row);
	}
}