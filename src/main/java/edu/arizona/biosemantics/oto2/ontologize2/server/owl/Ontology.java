package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.util.LinkedList;
import java.util.List;

import edu.arizona.biosemantics.common.biology.TaxonGroup;

public enum Ontology {
		
	BSPO("bspo", "http://purl.obolibrary.org/obo/bspo.owl"), 
	CARO("caro", "http://purl.obolibrary.org/obo/caro/src/caro.obo.owl"),
	CHEBI("chebi", "http://purl.obolibrary.org/obo/chebi.owl"),
	CL("cl", "http://purl.obolibrary.org/obo/cl.owl"), 
	RO("ro", "http://purl.obolibrary.org/obo/ro.owl"),
	PATO("pato", "http://purl.obolibrary.org/obo/pato.owl"),
	PO("po", "http://purl.obolibrary.org/obo/po.owl"),
	HAO("hao", "http://purl.obolibrary.org/obo/hao.owl"),
	ENVO("envo", "http://purl.obolibrary.org/obo/envo.owl"),
	GO("go", "http://purl.obolibrary.org/obo/go.owl"),
	UBERON("uberon", "http://purl.obolibrary.org/obo/uberon.owl"),
	PORO("poro", "http://purl.obolibrary.org/obo/poro.owl");
	
	private String name;
	private String iri;
	
	private Ontology(String name, String iri) {
		this.name = name;
		this.iri = iri;
	}

	public String getName() {
		return name;
	}

	public String getIri() {
		return iri;
	}
	
	public static List<Ontology> getRelevantOntologies(TaxonGroup taxonGroup) {
		List<Ontology> result = new LinkedList<Ontology>();
		
		result.add(Ontology.PATO);
		result.add(Ontology.RO);
		result.add(Ontology.BSPO);
		switch(taxonGroup) {
		case ALGAE:
			break;
		case CNIDARIA:
			break;
		case FOSSIL:
			break;
		case GASTROPODS:
			break;
		case HYMENOPTERA:
			result.add(Ontology.HAO);
			break;
		case PLANT:
			result.add(Ontology.PO);
			break;
		case PORIFERA:
			result.add(Ontology.PORO);
			break;
		case SPIDER:
			break;
		default:
			break;
		}
		return result;
	}
}