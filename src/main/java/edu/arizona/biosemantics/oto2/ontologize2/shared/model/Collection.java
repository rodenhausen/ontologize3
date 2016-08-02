package edu.arizona.biosemantics.oto2.ontologize2.shared.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;

public class Collection implements Serializable, Comparable<Collection> {

	private static final long serialVersionUID = 1L;
	private int id = -1;
	private String name = "";
	private TaxonGroup taxonGroup;
	private String secret = "";
	
	private Candidates candidates;	
	private OntologyGraph ontologyGraph;
	
	public Collection() { 
		taxonGroup = TaxonGroup.PLANT;
		ontologyGraph = new OntologyGraph(Type.values());
		candidates = new Candidates();
	}
	
	public Collection(String name, TaxonGroup taxonGroup, String secret) {
		this();
		this.name = name;
		this.taxonGroup = taxonGroup;
		this.secret = secret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaxonGroup getTaxonGroup() {
		return taxonGroup;
	}

	public void setTaxonGroup(TaxonGroup taxonGroup) {
		this.taxonGroup = taxonGroup;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public OntologyGraph getGraph() {
		return ontologyGraph;
	}

	public void setGraph(OntologyGraph ontologyGraph) {
		this.ontologyGraph = ontologyGraph;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@Override
	public int compareTo(Collection o) {
		return this.getId() - o.getId();
	}

	public boolean contains(String term) {
		return candidates.contains(term);
	}
	
	public void add(Candidate candidate) {
		candidates.add(candidate.getText(), candidate.getPath());
	}
	
	public void add(List<Candidate> candidates) {
		for(Candidate candidate : candidates)
			this.add(candidate);
	}
	
	public void remove(String term) {
		candidates.remove(term);
	}

	public Candidates getCandidates() {
		return candidates;
	}

}
