package edu.arizona.biosemantics.oto2.ontologize2.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Origin;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class OntologyGraph implements Serializable {

	private static final long serialVersionUID = 1L;

	public static class Vertex implements Serializable, Comparable<Vertex> {

		private static final long serialVersionUID = 1L;
		private String value;

		public Vertex() {
		}

		public Vertex(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			Vertex other = (Vertex) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public int compareTo(Vertex o) {
			return this.value.compareTo(o.value);
		}
	}

	public static class Edge implements Serializable {

		public static enum Type {
			SUBCLASS_OF("category", "superclass", "subclass", "subclasses", "category hierarchy", "Thing"), 
			PART_OF("part", "parent", "part", "parts", "part-of hierarchy", "Whole Organism"), 
			SYNONYM_OF("synonym", "preferred term", "synonym", "synonyms", "synonym-hierarchy", "Synonym-Root");

			private String displayLabel;
			private String sourceLabel;
			private String targetLabel;
			private String treeLabel;
			private String rootLabel;
			private String targetLabelPlural;

			private Type(String displayLabel, String sourceLabel,
					String targetLabel, String targetLabelPlural, String treeLabel, String rootLabel) {
				this.displayLabel = displayLabel;
				this.sourceLabel = sourceLabel;
				this.targetLabel = targetLabel;
				this.targetLabelPlural = targetLabelPlural;
				this.treeLabel = treeLabel;
				this.rootLabel = rootLabel;
			}

			public String getDisplayLabel() {
				return displayLabel;
			}

			public String getSourceLabel() {
				return sourceLabel;
			}

			public String getTargetLabel() {
				return targetLabel;
			}

			public String getTreeLabel() {
				return treeLabel;
			}

			public String getRootLabel() {
				return rootLabel;
			}

			public String getTargetLabelPlural() {
				return targetLabelPlural;
			}
		}

		public static enum Origin {
			USER, IMPORT;
		}

		private static final long serialVersionUID = 1L;
		private Type type;
		private Origin origin;
		private Vertex src;
		private Vertex dest;

		public Edge() {
		}

		public Edge(Vertex src, Vertex dest, Type type, Origin origin) {
			this.src = src;
			this.dest = dest;
			this.type = type;
			this.origin = origin;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public Origin getOrigin() {
			return origin;
		}

		@Override
		public String toString() {
			return type + " (" + origin + ")";
		}
		
		public Vertex getSrc() {
			return src;
		}

		public Vertex getDest() {
			return dest;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dest == null) ? 0 : dest.hashCode());
			result = prime * result
					+ ((origin == null) ? 0 : origin.hashCode());
			result = prime * result + ((src == null) ? 0 : src.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			Edge other = (Edge) obj;
			if (dest == null) {
				if (other.dest != null)
					return false;
			} else if (!dest.equals(other.dest))
				return false;
			if (origin != other.origin)
				return false;
			if (src == null) {
				if (other.src != null)
					return false;
			} else if (!src.equals(other.src))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
	}

	private DirectedSparseMultigraph<Vertex, Edge> graph;
	private Map<String, Vertex> index;

	public OntologyGraph() { }
	
	public OntologyGraph(Type... types) {
		graph = new DirectedSparseMultigraph<Vertex, Edge>();
		index = new HashMap<String, Vertex>();
		for (Type type : types)
			this.addVertex(new Vertex(type.getRootLabel()));
	}
	
	private boolean addVertex(Vertex vertex) {
		boolean result = graph.addVertex(vertex);
		if (result)
			index.put(vertex.getValue(), vertex);
		return result;
	}

	private boolean removeVertex(Vertex vertex) {
		boolean result = graph.removeVertex(vertex);
		if (result)
			index.remove(vertex.getValue());
		return result;
	}

	public boolean addRelation(Edge edge) throws Exception {
		switch(edge.getType()) {
			case PART_OF:
				return addPartOf(edge);
			case SUBCLASS_OF:
				return addSubclass(edge);
			case SYNONYM_OF:
				return addSynonym(edge);
		}
		return false;
	}
	
	private boolean doAddRelation(Edge edge) {
		if(!graph.containsVertex(edge.getSrc()))
			this.addVertex(edge.getSrc());
		if(!graph.containsVertex(edge.getDest()))
			this.addVertex(edge.getDest());
		
		return graph.addEdge(edge, edge.getSrc(), edge.getDest(), EdgeType.DIRECTED);
	}
	
	public boolean isCreatesCircular(Edge potentialRelation) {
		Set<Vertex> visited = new HashSet<Vertex>();
		visited.add(potentialRelation.getSrc());
		return isCreatesCircular(potentialRelation, visited);
	}
	
	private boolean isCreatesCircular(Edge e, Set<Vertex> visited) {
		if(visited.contains(e.getDest()))
			return true;
		else
			visited.add(e.getDest());
		for(Edge next : this.getOutRelations(e.getDest(), e.getType())) {
			boolean result = isCreatesCircular(next, visited);
			if(result)
				return true;
		}
		return false;
	}
	

	/**
	 * A node is either 
	 * - root node, has indegree == 0 and outdegree >= 0
	 * - indegree == 1 && in = { root } and outdegree >= 0
	 * - indegree == 1 && in = { != root } and outdegree == 0
	 */
	public boolean isValidSynonym(Edge e) throws Exception {
		if(this.existsRelation(e))
			throw new Exception("This relation already exists");
		Vertex src = e.getSrc();
		Vertex dest = e.getDest();
		Vertex root = this.getRoot(Type.SYNONYM_OF);
		List<Edge> srcIn = this.getInRelations(src, Type.SYNONYM_OF);
		List<Edge> srcOut = this.getInRelations(src, Type.SYNONYM_OF);
		List<Edge> destIn = this.getInRelations(dest, Type.SYNONYM_OF);
		List<Edge> destOut = this.getInRelations(dest, Type.SYNONYM_OF);
		
		if(dest.equals(root))
			throw new Exception("<i>" + root + "</i> can not be used as synonym");
		if(src.equals(root) && !destIn.isEmpty() && destIn.contains(root))
			throw new Exception("<i>" + dest + "</i> is already used as preferred term");
		if(src.equals(root) && !destIn.isEmpty() && !destIn.contains(root))
			throw new Exception("<i>" + dest + "</i> is already used as synonym");
		if(!src.equals(root) && srcIn.isEmpty())
			throw new Exception("<i>" + src + "</i> is not attached to \"" + root + "\"");
		if(!src.equals(root) && !destIn.isEmpty() && destIn.contains(root))
			throw new Exception("<i>" + dest + "</i> is already used as preferred term");
		if(!src.equals(root) && !destIn.isEmpty() && !destIn.contains(root))
			throw new Exception("<i>" + dest + "</i> is already used as synonym");
		return true;
	}
	
	private boolean addSynonym(Edge e) throws Exception {
		if(isValidSynonym(e))
			return doAddRelation(e);
		return false;
	}

	/**
	 * - No circular relationships allowed
	 */	
	public boolean isValidSubclass(Edge e) throws Exception {
		if(this.existsRelation(e))
			throw new Exception("This relation already exists");
		if(isCreatesCircular(e))
			throw new Exception("This relation would create a circular relationship");
		
		return true;
	}

	private boolean existsRelation(Edge r) {
		for(Edge e : graph.getOutEdges(r.getSrc())) {
            if(graph.getOpposite(r.getSrc(), e).equals(r.getDest()))
            	if(e.getType().equals(r.getType()))
            		return true;
        }
		return false;
	}

	private boolean addSubclass(Edge e) throws Exception {
		if(isValidSubclass(e))
			return doAddRelation(e);
		return false;
	}

	/**
	 * - No circular relationships allowed
	 */
	public boolean isValidPartOf(Edge e) throws Exception {
		if(this.existsRelation(e))
			throw new Exception("This relation already exists");
		if(isCreatesCircular(e))
			throw new Exception("This relation would create a circular relationship");
		return true;
	}
	
	/**
	 * - No multiple parents allowed, but allowed as input with permission to disambiguate as follows: 
	 * Disambiguate by parent name and create subclass relationships to original term, e.g.
	 * leaf, leaflet (part)
	 * stem, leaflet (part)
	 * =>
	 * leaf, leaf leaflet (part)
	 * stem, stem leaflet (part)
	 * leaf, leaf leaflet, stem leaflet (subclass)
	 */
	private boolean addPartOf(Edge e) throws Exception {
		if(isValidPartOf(e)) {
			Vertex src = e.getSrc();
			Vertex dest = e.getDest();
			String newValue = src + " " + dest;
			List<Edge> parentRelations = this.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				for(Edge parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSrc();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					this.addRelation(new Edge(dest, disambiguatedDest, Type.SUBCLASS_OF, e.getOrigin()));
					renameVertex(dest, newValue, Type.PART_OF);
				}
				e.getDest().setValue(newValue);
			}
			return doAddRelation(e);
		}
		return false;
	}

	private void renameVertex(Vertex v, String newValue, Type... types) throws Exception {
		Vertex newV = new Vertex(newValue);
		List<Edge> inRelations = new LinkedList<Edge>();
		List<Edge> outRelations = new LinkedList<Edge>();
		for(Type type : types) {
			inRelations.addAll(this.getInRelations(v, type));
			outRelations.addAll(this.getOutRelations(v, type));
		}
		
		this.removeVertex(v);
		for(Edge inRelation : inRelations) {
			Edge newEdge = new Edge(inRelation.getSrc(), newV, inRelation.getType(), inRelation.getOrigin());
			this.addRelation(inRelation);
		}
		for(Edge outRelation : outRelations) {
			Edge newEdge = new Edge(newV, outRelation.getDest(), outRelation.getType(), outRelation.getOrigin());
			this.addRelation(outRelation);
			Vertex dest = outRelation.getDest();
			
			//on prefix-match with old parent name, propagate rename to children
			if(dest.getValue().startsWith(v.getValue() + " ")) {
				renameVertex(dest, 
						dest.getValue().replaceFirst(v.getValue() + " ", newV.getValue() + " "), types);
			}
		}
	}

	public Vertex getVertex(String value) {
		return index.get(value);
	}
	
	public Vertex getRoot(Type type) {
		return index.get(type.getRootLabel());
	}
	
	public List<Edge> getOutRelations(Vertex vertex, Type type) {
		List<Edge> result = new LinkedList<Edge>();
		if(graph.containsVertex(vertex)) {
			java.util.Collection<Edge> edges = graph.getOutEdges(vertex);
			for(Edge edge : edges) {
				if(edge.getType().equals(type))
					result.add(edge);
			}
		}
		return result;
	}
	
	public List<Edge> getInRelations(Vertex vertex, Type type) {
		List<Edge> result = new LinkedList<Edge>();
		if(graph.containsVertex(vertex)) {
			java.util.Collection<Edge> edges = graph.getInEdges(vertex);
			for(Edge edge : edges) {
				if(edge.getType().equals(type))
					result.add(edge);
			}
		}
		return result;
	}

	public void removeRelation(Edge e, boolean recursive) {
		if(recursive) {
			graph.removeEdge(e);
			for(Edge outRelation : this.getOutRelations(e.getDest(), e.getType())) {
				this.removeRelation(outRelation, recursive);
			}
			if(this.getInRelations(e.getDest(), e.getType()).isEmpty()) {
				this.removeVertex(e.getDest());
			}
		} else {
			graph.removeEdge(e);
			for(Edge outRelation : this.getOutRelations(e.getDest(), e.getType())) {
				try {
					Edge newEdge = new Edge(e.getSrc(), outRelation.getDest(), e.getType(), Origin.USER);
					this.addRelation(newEdge);
				} catch(Exception ex) {
					//This should never happen
					System.out.println("Failed to reattach child nodes");
					ex.printStackTrace();
				}
			}
			if(this.getInRelations(e.getDest(), e.getType()).isEmpty()) {
				this.removeVertex(e.getDest());
			}
		}
	}
	
	public OntologyGraph getSubGraph(Type... types) throws Exception {
		OntologyGraph result = new OntologyGraph(types);
		for(Type type : types) {
			Vertex root = result.getRoot(type);
			addRelationsRecursively(result, root, type);
		}
		return result;
	}

	private void addRelationsRecursively(OntologyGraph g, Vertex source, Type type) throws Exception {
		for(Edge e : this.getOutRelations(source, type)) {
			this.addRelation(e);
			this.addRelationsRecursively(g, e.getDest(), type);
		}
	}

	public Collection<Vertex> getVertices() {
		return graph.getVertices();
	}

	public void replaceRelation(Edge oldRelation, Vertex newSource) throws Exception {
		graph.removeEdge(oldRelation);
		try {
			Edge newEdge = new Edge(newSource, oldRelation.getDest(), oldRelation.getType(), oldRelation.getOrigin());
			this.addRelation(newEdge);			
		} catch(Exception e) {
			this.addRelation(oldRelation);
			throw e;
		}
		
		if(oldRelation.getType().equals(Type.SYNONYM_OF)) {
			for(Edge e : this.getOutRelations(oldRelation.getDest(), Type.SYNONYM_OF)) {
				this.removeRelation(e, true);
				this.addRelation(new Edge(newSource, e.getDest(), oldRelation.getType(), oldRelation.getOrigin()));
			}
		}
	}
}
