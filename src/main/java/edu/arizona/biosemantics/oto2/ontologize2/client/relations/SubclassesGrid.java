package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.DndDropEvent.DndDropHandler;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.ReplaceRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Origin;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;

public class SubclassesGrid extends MenuTermsGrid {

	public SubclassesGrid(EventBus eventBus) {
		super(eventBus, Type.SUBCLASS_OF);
		
		GridDragSource<Row> dndSource = new GridDragSource<Row>(grid) {
			@Override
			protected void onDragStart(DndDragStartEvent event) {
				super.onDragStart(event);
				Element element = event.getDragStartEvent().getStartElement();
				int targetRowIndex = grid.getView().findRowIndex(element);
				int targetColIndex = grid.getView().findCellIndex(element, null);
				Row row = store.get(targetRowIndex);
				Vertex v = row.getLead();
				if(targetColIndex > 0) {
					v = row.getAttached().get(targetColIndex - 1).getDest();
				}
				
				OntologyGraph g = ModelController.getCollection().getGraph();
				List<Edge> inRelations = g.getInRelations(v, type);
				if(inRelations.size() > 1) {
					Alerter.showAlert("Moving", "Moving of term with more than one superclasses is not allowed"); // at this time
					event.setCancelled(true);
				}
				if(inRelations.size() == 1)
					event.setData(inRelations.get(0));
				else {
					Alerter.showAlert("Moving", "Cannot move the root");
					event.setCancelled(true);
				}
			}
		};
		
		dropTarget.setAllowSelfAsSource(true);
		dropTarget.addDropHandler(new DndDropHandler() {
			@Override
			public void onDrop(DndDropEvent event) {
				Element element = event.getDragEndEvent().getNativeEvent().getEventTarget().<Element> cast();
				int targetRowIndex = grid.getView().findRowIndex(element);
				int targetColIndex = grid.getView().findCellIndex(element, null);
				Row row = store.get(targetRowIndex);
				
				if(event.getData() instanceof Edge) {
					Edge r = (Edge)event.getData();
					fire(new ReplaceRelationEvent(r, row.getLead()));
				}
			}
		});
	}
	
	@Override
	public void fire(GwtEvent<? extends EventHandler> e) {
		if(e instanceof CreateRelationEvent) {
			final CreateRelationEvent createRelationEvent = (CreateRelationEvent)e;
			OntologyGraph g = ModelController.getCollection().getGraph();
			for(Edge r : createRelationEvent.getRelations()) {
				try {
					g.isValidSubclass(r);
					
					Vertex source = r.getSrc();
					Vertex dest = r.getDest();
					List<Edge> existingRelations = g.getInRelations(dest, type);
					if(!existingRelations.isEmpty()) {
						List<Vertex> existSources = new ArrayList<Vertex>(existingRelations.size());
						for(Edge exist : existingRelations) 
							existSources.add(exist.getSrc());
						final MessageBox box = Alerter.showConfirm("Create Subclass", 
								"<i>" + dest + "</i> is already a subclass of " + existingRelations.size() + " superclasses: <i>" +
										Alerter.collapseTermsAsString(existSources) + "</i>.</br></br></br>" +
										"Do you still want to make <i>" + dest + "</i> a subclass of <i>" + source + "</i>?</br></br>" +
										"If NO, please create a new term then make it a subclass of <i>" + source + "</i>.");
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
	
	protected void createRelation(Edge r) {
		super.createRelation(r);
		
		if(r.getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDest();
			Vertex src = r.getSrc();
			String newValue = src + " " + dest;
			
			List<Edge> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				super.createRelation(new Edge(g.getRoot(Type.SUBCLASS_OF), dest, Type.SUBCLASS_OF, Origin.USER));
				for(Edge parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSrc();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					super.createRelation(new Edge(dest, disambiguatedDest, Type.SUBCLASS_OF, Origin.USER));
				}
				super.createRelation(new Edge(dest, new Vertex(newValue), Type.SUBCLASS_OF, Origin.USER));
			}
		}
	}
	
	protected void onCreateRelationEffectiveInModel(Edge r) {
		if(r.getType().equals(type)) {
			Vertex dest = r.getDest();
			for(Row row : getAttachedRows(dest)) 
				grid.getStore().update(row);
		}
	}
	
	@Override
	protected void onRemoveRelationEffectiveInModel(Edge r) {
		if(r.getType().equals(type)) {
			Vertex dest = r.getDest();
			for(Row row : getAttachedRows(dest)) 
				grid.getStore().update(row);
		}
	}
	
	@Override
	protected SimpleContainer createCreateRowContainer() {
		return null;
	}
	
	@Override
	protected void onLoadCollectionEffectiveInModel() {
		OntologyGraph g = ModelController.getCollection().getGraph();
		for(Vertex v : g.getVertices()) {
			List<Edge> inRelations = g.getInRelations(v, type);
			if(inRelations.size() > 1) {
				for(Row row : getAttachedRows(v)) 
					grid.getStore().update(row);
			}
		}
	}
	
	@Override
	protected String getDefaultImportText() {
		return "superclass, subclass 1, subclass 2, ...[e.g. fruits, simple fruits, aggregate fruits, composite fruits]"; 
	}

}
