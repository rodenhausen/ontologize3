package edu.arizona.biosemantics.oto2.ontologize2.client.tree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.shared.EventBus;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class PartsTreeView extends TreeView {

	public PartsTreeView(EventBus eventBus) {
		super(eventBus, Type.PART_OF);
	}
	
	protected void createRelation(Relation r) {		
		if(r.getEdge().getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDestination();
			Vertex src = r.getSource();
			String newValue = src + " " + dest;
			
			List<Relation> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				for(Relation parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSource();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					replace(parentSrc, dest, disambiguatedDest);
				}
				
				super.createRelation(new Relation(src, new Vertex(newValue), r.getEdge()));
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
			
			for(Relation r : ModelController.getCollection().getGraph().getOutRelations(vertex, Type.PART_OF)) {
				if(r.getDestination().getValue().startsWith(vertex.getValue())) {
					replace(vertex, r.getDestination(), new Vertex(newVertex.getValue() + " " + r.getDestination().getValue()));
				}
			}
		}
	}


}
