package edu.arizona.biosemantics.oto2.ontologize2.client.tree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.shared.EventBus;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class PartsTreeView extends TreeView {

	public PartsTreeView(EventBus eventBus) {
		super(eventBus, Type.PART_OF);
	}
	
	protected void createRelation(Edge r) {		
		if(r.getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDest();
			Vertex src = r.getSrc();
			String newValue = src + " " + dest;
			
			List<Edge> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				for(Edge parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSrc();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					replace(parentSrc, dest, disambiguatedDest);
				}
				
				super.createRelation(new Edge(src, new Vertex(newValue), r.getType(), r.getOrigin()));
			} else {
				super.createRelation(r);
			}
		}
	}

	private void replace(Vertex parent, Vertex vertex, Vertex newVertex) {
		if(vertexNodeMap.containsKey(vertex)) {
			VertexTreeNode destNode = vertexNodeMap.get(vertex).iterator().next();
			VertexTreeNode newDestNode = new VertexTreeNode(newVertex);
			
			replaceNode(destNode, newDestNode);
			
			vertexNodeMap.put(newVertex, new HashSet<VertexTreeNode>(Arrays.asList(newDestNode)));
			vertexNodeMap.remove(vertex);
			
			for(Edge r : ModelController.getCollection().getGraph().getOutRelations(vertex, Type.PART_OF)) {
				if(r.getDest().getValue().startsWith(vertex.getValue())) {
					replace(vertex, r.getDest(), new Vertex(newVertex.getValue() + " " + r.getDest().getValue()));
				}
			}
		}
	}


}
