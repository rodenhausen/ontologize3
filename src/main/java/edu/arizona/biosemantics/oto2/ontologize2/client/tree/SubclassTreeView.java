package edu.arizona.biosemantics.oto2.ontologize2.client.tree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.gwt.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Menu;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Origin;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class SubclassTreeView extends TreeView {

	private Stack<Vertex> rootStack = new Stack<Vertex>();
	private TextButton backButton;
	
	public SubclassTreeView(EventBus eventBus) {
		super(eventBus, Type.SUBCLASS_OF);
		
		Menu menu = new Menu();
		TextButton resetButton = new TextButton("Reset");
		resetButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				Vertex root = g.getRoot(type);
				createFromRoot(g, root);
				rootStack.removeAllElements();
				backButton.setEnabled(false);
			}
		});
		backButton = new TextButton("Back");
		backButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				OntologyGraph g = ModelController.getCollection().getGraph();
				createFromRoot(g, rootStack.pop());
				if(rootStack.isEmpty())
					backButton.setEnabled(false);
			}
		});
		menu.add(backButton);
		menu.add(resetButton);
		
		TextButton menuButton = new TextButton("Navigate");
		menuButton.setMenu(menu);
		
		buttonBar.add(menuButton);
	}

	@Override
	protected void createRelation(Edge r) {
		if(r.getType().equals(type)) {
			if(!isVisible(r))
				return;
			
			VertexTreeNode sourceNode = null;
	 		if(vertexNodeMap.containsKey(r.getSrc())) {
				sourceNode = vertexNodeMap.get(r.getSrc()).iterator().next();
			} else {
				sourceNode = new VertexTreeNode(r.getSrc());
				add(null, sourceNode);
			}
	 		//create either way, to get a new id
	 		VertexTreeNode destinationNode = new VertexTreeNode(r.getDest());
	 		add(sourceNode, destinationNode);
	 		treeGrid.setExpanded(sourceNode, true);
			
	 		if(vertexNodeMap.get(r.getDest()).size() > 1) {
				//remove child nodes below already existings
				for(VertexTreeNode n : vertexNodeMap.get(r.getDest())) {
					removeAllChildren(n);
				}
			}
		}
		
		if(r.getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDest();
			Vertex src = r.getSrc();
			String newValue = src + " " + dest;
			
			List<Edge> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				createRelation(new Edge(g.getRoot(Type.SUBCLASS_OF), dest, Type.SUBCLASS_OF, Origin.USER));
				for(Edge parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSrc();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					createRelation(new Edge(dest, disambiguatedDest, Type.SUBCLASS_OF, Origin.USER));
				}
				createRelation(new Edge(dest, new Vertex(newValue), Type.SUBCLASS_OF, Origin.USER));
			}
		}
	}

	private boolean isVisible(Edge r) {
		OntologyGraph g = ModelController.getCollection().getGraph();
		Vertex currentRoot = getRoot();
		Vertex source = r.getSrc();
		if(currentRoot.equals(source))
			return true;
		if(g.getInRelations(source, Type.SUBCLASS_OF).size() > 1) 
			return false;
		for(Edge in : g.getInRelations(r.getSrc(), type)) {
			if(!isVisible(in))
				return false;
		}
		return true;
	}

	private void refreshNodes(Set<VertexTreeNode> nodes) {
		for(VertexTreeNode n : nodes) {
			treeGrid.getStore().update(n);
		}
	}

	@Override
	protected void createFromVertex(OntologyGraph g, Vertex source) {
		Vertex currentRoot = getRoot();
		if(!currentRoot.equals(source) && g.getInRelations(source, Type.SUBCLASS_OF).size() > 1) {
			return;
		} else {
			for(Edge r : g.getOutRelations(source, type)) {
				createRelation(r);
				createFromVertex(g, r.getDest());
			}
		}
	}

	@Override
	protected void onDoubleClick(VertexTreeNode node) {
		rootStack.push(this.getRoot());
		backButton.setEnabled(true);
		Vertex v = node.getVertex();
		OntologyGraph g = ModelController.getCollection().getGraph();
		if(g.getInRelations(v, Type.SUBCLASS_OF).size() > 1) {
			this.createFromRoot(g, v);
		}
	}

	@Override
	protected void createFromRoot(OntologyGraph g, Vertex root) {
		super.createFromRoot(g, root);
	}
	
	@Override
	protected void onCreateRelationEffectiveInModel(Edge r) {
		if(r.getType().equals(type)) {
			if(!isVisible(r))
				return;
			if(vertexNodeMap.containsKey(r.getDest()))
				refreshNodes(vertexNodeMap.get(r.getDest()));
		}
	}
	
	@Override
	protected void onRemoveRelationEffectiveInModel(Edge r) {
		if(r.getType().equals(type)) {
			if(!isVisible(r))
				return;
			if(vertexNodeMap.containsKey(r.getDest()))
				refreshNodes(vertexNodeMap.get(r.getDest()));
		}
	}
	
	@Override
	protected void onLoadCollectionEffectiveInModel() {
		OntologyGraph g = ModelController.getCollection().getGraph();
		for(Vertex v : g.getVertices()) {
			List<Edge> inRelations = g.getInRelations(v, type);
			if(inRelations.size() > 1) {
				refreshNodes(vertexNodeMap.get(v));
			}
		}
	}
}
