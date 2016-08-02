package edu.arizona.biosemantics.oto2.ontologize2.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;

public class AddCandidateResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<Candidate> successfully = new LinkedList<Candidate>();
	private List<Candidate> unsuccessfully = new LinkedList<Candidate>();

	public AddCandidateResult() { }
	
	public AddCandidateResult(List<Candidate> successfully, List<Candidate> unsuccessfully) {
		this.successfully = successfully;
		this.unsuccessfully = unsuccessfully;
	}
	
	public List<Candidate> getSuccessfullyAdded() {
		return successfully;
	}

	public List<Candidate> getUnsucessfullyAdded() {
		return unsuccessfully;
	}

}
