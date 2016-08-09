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
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
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
			SUBCLASS_OF("category", "superclass", "subclass", "subclasses", "category hierarchy", "Class-Thing"), 
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

		public static enum Source {
			USER, IMPORT;
		}

		private static final long serialVersionUID = 1L;
		private Type type;
		private Source source;

		public Edge() {
		}

		public Edge(Type type, Source source) {
			this.type = type;
			this.source = source;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public Source getSource() {
			return source;
		}

		public void setSource(Source source) {
			this.source = source;
		}

		@Override
		public String toString() {
			return type + " (" + source + ")";
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

	public boolean addRelation(Relation relation) throws Exception {
		switch(relation.getEdge().getType()) {
			case PART_OF:
				return addPartOf(relation);
			case SUBCLASS_OF:
				return addSubclass(relation);
			case SYNONYM_OF:
				return addSynonym(relation);
		}
		return false;
	}
	
	private boolean doAddRelation(Relation relation) {
		if(!graph.containsVertex(relation.getSource()))
			this.addVertex(relation.getSource());
		if(!graph.containsVertex(relation.getDestination()))
			this.addVertex(relation.getDestination());
		
		return graph.addEdge(relation.getEdge(), relation.getSource(), relation.getDestination(), EdgeType.DIRECTED);
	}
	
	public boolean isCreatesCircular(Relation potentialRelation) {
		Set<Vertex> visited = new HashSet<Vertex>();
		visited.add(potentialRelation.getSource());
		return isCreatesCircular(potentialRelation, visited);
	}
	
	private boolean isCreatesCircular(Relation r, Set<Vertex> visited) {
		if(visited.contains(r.getDestination()))
			return true;
		else
			visited.add(r.getDestination());
		for(Relation next : this.getOutRelations(r.getDestination(), r.getEdge().getType())) {
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
	public boolean isValidSynonym(Relation relation) throws Exception {
		if(this.existsRelation(relation))
			throw new Exception("This relation already exists");
		Vertex src = relation.getSource();
		Vertex dest = relation.getDestination();
		Vertex root = this.getRoot(Type.SYNONYM_OF);
		List<Relation> srcIn = this.getInRelations(src, Type.SYNONYM_OF);
		List<Relation> srcOut = this.getInRelations(src, Type.SYNONYM_OF);
		List<Relation> destIn = this.getInRelations(dest, Type.SYNONYM_OF);
		List<Relation> destOut = this.getInRelations(dest, Type.SYNONYM_OF);
		
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
	
	private boolean addSynonym(Relation relation) throws Exception {
		if(isValidSynonym(relation))
			return doAddRelation(relation);
		return false;
	}

	/**
	 * - No circular relationships allowed
	 */	
	public boolean isValidSubclass(Relation relation) throws Exception {
		if(this.existsRelation(relation))
			throw new Exception("This relation already exists");
		if(isCreatesCircular(relation))
			throw new Exception("This relation would create a circular relationship");
		
		return true;
	}

	private boolean existsRelation(Relation r) {
		for(Edge e : graph.getOutEdges(r.getSource())) {
            if(graph.getOpposite(r.getSource(), e).equals(r.getDestination()))
            	if(e.getType().equals(r.getEdge().getType()))
            		return true;
        }
		return false;
	}

	private boolean addSubclass(Relation relation) throws Exception {
		if(isValidSubclass(relation))
			return doAddRelation(relation);
		return false;
	}

	/**
	 * - No circular relationships allowed
	 */
	public boolean isValidPartOf(Relation relation) throws Exception {
		if(this.existsRelation(relation))
			throw new Exception("This relation already exists");
		if(isCreatesCircular(relation))
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
	private boolean addPartOf(Relation relation) throws Exception {
		if(isValidPartOf(relation)) {
			Vertex src = relation.getSource();
			Vertex dest = relation.getDestination();
			String newValue = src + " " + dest;
			List<Relation> parentRelations = this.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				for(Relation parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSource();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					this.addRelation(new Relation(dest, disambiguatedDest, 
							new Edge(Type.SUBCLASS_OF, relation.getEdge().getSource())));
					renameVertex(dest, newValue, Type.PART_OF);
				}
				relation.getDestination().setValue(newValue);
			}
			return doAddRelation(relation);
		}
		return false;
	}

	private void renameVertex(Vertex v, String newValue, Type... types) throws Exception {
		Vertex newV = new Vertex(newValue);
		List<Relation> inRelations = new LinkedList<Relation>();
		List<Relation> outRelations = new LinkedList<Relation>();
		for(Type type : types) {
			inRelations.addAll(this.getInRelations(v, type));
			outRelations.addAll(this.getOutRelations(v, type));
		}
		
		this.removeVertex(v);
		for(Relation inRelation : inRelations) {
			inRelation.setDestination(newV);
			this.addRelation(inRelation);
		}
		for(Relation outRelation : outRelations) {
			outRelation.setSource(newV);
			this.addRelation(outRelation);
			Vertex dest = outRelation.getDestination();
			
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
	
	public Relation getRelation(Edge edge) {
		return new Relation(graph.getSource(edge), graph.getDest(edge), edge);
	}
	
	public List<Relation> getOutRelations(Vertex vertex, Type type) {
		List<Relation> result = new LinkedList<Relation>();
		if(graph.containsVertex(vertex)) {
			java.util.Collection<Edge> edges = graph.getOutEdges(vertex);
			for(Edge edge : edges) {
				if(edge.getType().equals(type))
					result.add(new Relation(vertex, graph.getDest(edge), edge));
			}
		}
		return result;
	}
	
	public List<Relation> getInRelations(Vertex vertex, Type type) {
		List<Relation> result = new LinkedList<Relation>();
		if(graph.containsVertex(vertex)) {
			java.util.Collection<Edge> edges = graph.getInEdges(vertex);
			for(Edge edge : edges) {
				if(edge.getType().equals(type))
					result.add(new Relation(graph.getSource(edge), vertex, edge));
			}
		}
		return result;
	}

	public void removeRelation(Relation relation) {
		graph.removeEdge(relation.getEdge());
		graph.removeVertex(relation.getDestination());
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
		for(Relation r : this.getOutRelations(source, type)) {
			this.addRelation(r);
			this.addRelationsRecursively(g, r.getDestination(), type);
		}
	}

	public Collection<Vertex> getVertices() {
		return graph.getVertices();
	}
}
