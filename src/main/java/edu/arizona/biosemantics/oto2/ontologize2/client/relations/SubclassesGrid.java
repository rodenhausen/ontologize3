package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.ArrayList;
import java.util.List;

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
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class SubclassesGrid extends MenuTermsGrid {

	public SubclassesGrid(EventBus eventBus) {
		super(eventBus, Type.SUBCLASS_OF);
	}
	
	@Override
	protected void fire(GwtEvent<? extends EventHandler> e) {
		if(e instanceof CreateRelationEvent) {
			final CreateRelationEvent createRelationEvent = (CreateRelationEvent)e;
			OntologyGraph g = ModelController.getCollection().getGraph();
			for(Relation r : createRelationEvent.getRelations()) {
				try {
					g.isValidSubclass(r);
					
					Vertex source = r.getSource();
					Vertex dest = r.getDestination();
					List<Relation> existingRelations = g.getInRelations(dest, type);
					if(!existingRelations.isEmpty()) {
						List<Vertex> existSources = new ArrayList<Vertex>(existingRelations.size());
						for(Relation exist : existingRelations) 
							existSources.add(exist.getSource());
						final MessageBox box = Alerter.showConfirm("Create Subclass", 
								"\"" + dest + "\" is already a subclass of " + existingRelations.size() + " superclasses: \"" +
										Alerter.collapseTermsAsString(existSources) + "\".</br></br></br>" +
										"Do you still want to create \"" + dest + "\" as subclass of \"" + source + "\"?</br></br>" +
										"If NO, please create a new term then add as a subclass of " + source + ".");
						box.getButton(PredefinedButton.YES).addSelectHandler(new SelectHandler() {
							@Override
							public void onSelect(SelectEvent event) {
								eventBus.fireEvent(createRelationEvent);
								box.hide();
							}
						});
						box.getButton(PredefinedButton.NO).addSelectHandler(new SelectHandler() {
							@Override
							public void onSelect(SelectEvent event) {
								box.hide();
							}
						});
					} else {
						eventBus.fireEvent(createRelationEvent);
					}
				} catch(Exception ex) {
					final MessageBox box = Alerter.showAlert("Create subclass", ex.getMessage());
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
	
	protected void createRelation(Relation r) {
		super.createRelation(r);
		
		if(r.getEdge().getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDestination();
			Vertex src = r.getSource();
			String newValue = src + " " + dest;
			
			List<Relation> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				super.createRelation(new Relation(g.getRoot(Type.SUBCLASS_OF), dest, new Edge(Type.SUBCLASS_OF, Source.USER)));
				for(Relation parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSource();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					super.createRelation(new Relation(dest, disambiguatedDest, new Edge(Type.SUBCLASS_OF, Source.USER)));
				}
				super.createRelation(new Relation(dest, new Vertex(newValue), new Edge(Type.SUBCLASS_OF, Source.USER)));
			}
		}
	}
	
	protected void onCreateRelationEffectiveInModel(Relation r) {
		if(r.getEdge().getType().equals(type)) {
			Vertex dest = r.getDestination();
			for(Row row : getAttachedRows(dest)) 
				grid.getStore().update(row);
		}
	}
}
