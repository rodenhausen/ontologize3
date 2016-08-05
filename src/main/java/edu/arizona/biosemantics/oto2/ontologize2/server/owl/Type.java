package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

public enum Type {

	ENTITY("http://purl.obolibrary.org/obo/CARO_0000006", "material anatomical entity"), //material anatomical entity
	QUALITY("http://purl.obolibrary.org/obo/PATO_0000001", "quality"); //quality
	
	private String iri;
	private String label;
	
	Type(String iri, String label) {
		this.iri = iri;
		this.label = label;
	}
	
	public String getIRI() {
		return iri;
	}

	public String getLabel() {
		return label;
	}
}
