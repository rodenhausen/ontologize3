package edu.arizona.biosemantics.oto2.ontologize2.shared.model;

import java.io.Serializable;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class Relation implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Vertex source;
	private Vertex destination;
	private Edge edge;
	
	public Relation() { }
	
	public Relation(Vertex source, Vertex destination, Edge edge) {
		this.source = source;
		this.destination = destination;
		this.edge = edge;
	}
	
	public Vertex getSource() {
		return source;
	}
	public void setSource(Vertex source) {
		this.source = source;
	}
	public Vertex getDestination() {
		return destination;
	}
	public void setDestination(Vertex destination) {
		this.destination = destination;
	}
	public Edge getEdge() {
		return edge;
	}
	public void setEdge(Edge edge) {
		this.edge = edge;
	}
	
	@Override
	public String toString() {
		return source.toString() + " --- " +  edge.toString() + " --> " + destination.toString();
	}


}
