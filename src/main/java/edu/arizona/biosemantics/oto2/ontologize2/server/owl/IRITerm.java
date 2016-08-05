package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

public class IRITerm {
	
	public String term;
	public String iri;
	public String definition;
	public String source;
	public String sampleSentence;
	
	public boolean hasIRI() {
		return iri != null && !iri.isEmpty();
	}
	public boolean hasSource() {
		return source != null && !source.isEmpty();
	}
	public boolean hasSampleSentence() {
		return sampleSentence != null && !sampleSentence.isEmpty();
	}
}