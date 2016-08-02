package edu.arizona.biosemantics.oto2.ontologize2.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Candidates implements Serializable, Iterable<Candidate> {
	
	private static final long serialVersionUID = 1L;
	private Set<String> terms = new HashSet<String>();
	private Map<String, String> termPaths = new HashMap<String, String>();
	
	public Candidates() { }
	
	public Candidates(Set<String> terms, Map<String, String> termPaths) {
		this.terms = terms;
		this.termPaths = termPaths;
	}

	public Set<String> getTerms() {
		return terms;
	}

	public void setTerms(Set<String> terms) {
		this.terms = terms;
	}

	public Map<String, String> getTermPaths() {
		return termPaths;
	}

	public void setTermPaths(Map<String, String> termPaths) {
		this.termPaths = termPaths;
	}

	public boolean contains(String term) {
		return terms.contains(term);
	}

	public void add(String term, String path) {
		if(!terms.contains(term)) {
			terms.add(term);
			termPaths.put(term, path);
		}
	}

	public void remove(String term) {
		terms.remove(term);
		termPaths.remove(term);
	}

	@Override
	public Iterator<Candidate> iterator() {
		List<Candidate> list = new LinkedList<Candidate>();
		for(String term : terms) 
			list.add(new Candidate(term, termPaths.containsKey(term) ? termPaths.get(term) : null));
		return list.iterator();
	}

	public String getPath(String term) {
		if(termPaths.containsKey(term))
			return termPaths.get(term);
		return "";
	}
}
