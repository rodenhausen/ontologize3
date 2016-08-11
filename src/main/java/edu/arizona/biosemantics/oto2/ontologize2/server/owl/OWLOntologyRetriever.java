package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;

public class OWLOntologyRetriever  {
	
	private OWLOntologyManager om;
	private Collection c;
	
	public OWLOntologyRetriever(OWLOntologyManager om, Collection c) {
		this.om = om;
		this.c = c;
	}
	
	/**
	 * A OWLClass can be contained in a set of ontologies. It is however defined in a single ontology.
	 * The single ontology gives the class it's record and also it's IRI, e.g. is has a prefix of the OWLOntology IRI
	 * 
	 * Given a owlClass getting the OWLOntology is thus not a simple call. The OWLOntology it stems from may not be loaded at all.
	 * Hence, maintain referencedOntologies that maps ontology IRIs to OWLOntologies that we have in memory.
	 * Map OWLClass IRI to ontology IRI to retrieve OWLOntology from referenced ontologies.
	 */
	//this is still very hacky, have to think about a better/more robust way of doing this
	//IRIs come in different formats, is there a standard?
	//http://purl.bioontology.org/obo/hao_01234
	//http://purl.obolibrary.org/obo/hao
	//http://www.etc-project.org/owl/ontologies/1/my_ontology#term
	/**
	 * There are 3 ways an ontology can reference the origin of a class' definition
	 * (1) By importing the ontology that contains the class definition as a whole using an import declaration
	 * (2) Using RDFS:defined_by
	 * (3) Using obo:IAO_0000412 ("imported from"): may still be used in some OBO originated ontologies
	 * */	
	public OWLOntology getOWLOntology(OWLClass owlClass) throws Exception {
		System.out.println("find ontology for " + owlClass.getIRI());
		for(Ontology ontology : Ontology.getRelevantOntologies(TaxonGroup.PLANT)) {
			IRI iri = IRI.create(ontology.getIri());
			OWLOntology owlOntology = om.getOntology(iri);
			if(isOWLOntologyOfClass(owlClass, owlOntology)) {
				return owlOntology;
			}
			
			java.util.Collection<OWLOntology> referencedOntologies = om.getImportsClosure(owlOntology);//getReferencedOntologies(owlOntology);
			for(OWLOntology referencedOntology : referencedOntologies) {
				if(isOWLOntologyOfClass(owlClass, referencedOntology)) {
					return referencedOntology;
				}
			}
		}
		throw new Exception("Could not find ontology for class " + owlClass.getIRI().toString());
	}
	
	private boolean isOWLOntologyOfClass(OWLClass owlClass, OWLOntology owlOntology) {
		String owlClassIRI = owlClass.getIRI().toString().toLowerCase();
		String hackyOwlClassIdentifier = owlClassIRI;
		if(hackyOwlClassIdentifier.contains("_"))
			hackyOwlClassIdentifier = hackyOwlClassIdentifier.substring(0, hackyOwlClassIdentifier.indexOf("_"));
		else if(hackyOwlClassIdentifier.contains("#"))
			hackyOwlClassIdentifier = hackyOwlClassIdentifier.substring(0, hackyOwlClassIdentifier.indexOf("#"));
		
		String ontologyIRI = owlOntology.getOntologyID().getOntologyIRI().get().toString();
		String hackyOntologyIdentifier = ontologyIRI;
		
		//caro ontology iri is http://purl.obolibrary.org/obo/caro/src/caro.owl, 
		//while classes would have hacky id http://purl.obolibrary.org/obo/caro
		// use startswith as sufficient indicator?
		if(hackyOntologyIdentifier.equals("http://purl.obolibrary.org/obo/caro/src/caro.obo.owl") && 
				hackyOwlClassIdentifier.equals("http://purl.obolibrary.org/obo/caro"))
			return true;
		if(hackyOntologyIdentifier.endsWith(".owl"))
			hackyOntologyIdentifier = hackyOntologyIdentifier.replace(".owl", "");
		if(hackyOntologyIdentifier.equals(hackyOwlClassIdentifier))
			return true;
		return false;
	}

	private java.util.Collection<OWLOntology> getReferencedOntologies(OWLOntology owlOntology) {
		java.util.Collection<OWLOntology> result = new LinkedList<OWLOntology>();
		
		// (1)
		Set<OWLOntology> closureOntologies = om.getImportsClosure(owlOntology);
		result.addAll(closureOntologies);	
		
		Set <OWLClass> classes = owlOntology.getClassesInSignature();
		for(OWLClass clazz : classes){

			// (2) rdfs:isDefinedBy
			java.util.Collection<OWLAnnotation> annotations = EntitySearcher.getAnnotations(clazz, owlOntology, 
					om.getOWLDataFactory().getRDFSIsDefinedBy());
			for(OWLAnnotation annotation : annotations){
				if(annotation.getValue() instanceof IRI){
					IRI iri = (IRI)annotation.getValue();
					OWLOntology definedByOwlOntology;
					try {
						definedByOwlOntology = om.loadOntology(iri);
						result.add(definedByOwlOntology);
					} catch (OWLOntologyCreationException e) {
						e.printStackTrace();
						log(LogLevel.ERROR, "Could not load ontology " + iri.toString() + ". Will give up.");
					}
				}
			}
			
			// (3) obo:IAO_0000412
			annotations = EntitySearcher.getAnnotations(clazz, owlOntology);
			for(OWLAnnotation annotation : annotations){
				if(annotation.getValue() instanceof IRI){
					if(annotation.getProperty().getIRI().equals(IRI.create("http://purl.obolibrary.org/obo/IAO_0000412"))) {
						if(annotation.getValue() instanceof IRI) {
							IRI iri = (IRI) annotation.getValue();
							OWLOntology definedByOwlOntology;
							try {
								definedByOwlOntology = om.loadOntology(iri);
								result.add(definedByOwlOntology);
							} catch (OWLOntologyCreationException e) {
								e.printStackTrace();
								log(LogLevel.ERROR, "Could not load ontology " + iri.toString() + ". Will give up.");
							}
						}
					}
				}
			}
		}
		return result;
	}
}