package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;

public class AnnotationsManager {
		
	private OWLOntologyRetriever owlOntologyRetriever;

	public AnnotationsManager(OWLOntologyRetriever owlOntologyRetriever) {
		this.owlOntologyRetriever = owlOntologyRetriever;
	}

	public String get(Collection collection, OWLClass owlClass, OWLAnnotationProperty annotationProperty) throws Exception {
		OWLOntology owlOntology = owlOntologyRetriever.getOWLOntology(collection, owlClass);
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, owlOntology, annotationProperty)) {
			if (annotation.getValue() instanceof OWLLiteral) {
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				//if (val.hasLang("en")) {
				return val.getLiteral();
				//}
			}
		}
		return null;
	}
	
	public String get(OWLClass owlClass, OWLAnnotationProperty annotationProperty) throws Exception {
		OWLOntology owlOntology = owlOntologyRetriever.getPermanentOWLOntology(owlClass);
		for (OWLAnnotation annotation : EntitySearcher.getAnnotations(owlClass, owlOntology, annotationProperty)) {
			if (annotation.getValue() instanceof OWLLiteral) {
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				//if (val.hasLang("en")) {
				return val.getLiteral();
				//}
			}
		}
		return null;
	}
	
}