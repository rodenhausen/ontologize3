package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class VertexTreeNode extends TextTreeNode {

	private static int ID;
	private int id = ID++;
	private Vertex vertex;

	public VertexTreeNode(Vertex vertex) {
		this.vertex = vertex;
	}
	
	@Override
	public String getText() {
		return vertex.getValue();
	}

	@Override
	public String getId() {
		//use modelcontroller.getcollection to traverse along parents to create unique id
		//return term;
		return String.valueOf(id);
	}
	
	public Vertex getVertex() {
		return vertex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VertexTreeNode other = (VertexTreeNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
