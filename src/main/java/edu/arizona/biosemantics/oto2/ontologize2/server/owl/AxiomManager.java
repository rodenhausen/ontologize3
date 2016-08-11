package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.google.common.base.Optional;

import edu.arizona.biosemantics.common.ontology.AnnotationProperty;
import edu.arizona.biosemantics.oto.model.lite.Synonym;
import edu.arizona.biosemantics.oto2.ontologize2.client.Ontologize;
import edu.arizona.biosemantics.oto2.ontologize2.server.Configuration;

public class AxiomManager  {

	private OWLOntologyManager om;
	
	private OWLClass entityClass;
	private OWLClass qualityClass;
	private OWLObjectProperty partOfProperty;
	private OWLAnnotationProperty labelProperty;
	private OWLAnnotationProperty synonymProperty;
	private OWLAnnotationProperty definitionProperty;
	private OWLAnnotationProperty creationDateProperty;
	private OWLAnnotationProperty createdByProperty;
	private OWLAnnotationProperty relatedSynonymProperty;
	private OWLAnnotationProperty narrowSynonymProperty;
	private OWLAnnotationProperty exactSynonymProperty;
	private OWLAnnotationProperty broadSynonymProperty;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

	public AxiomManager(OWLOntologyManager om) {
		this.om = om;
		
		partOfProperty = om.getOWLDataFactory().getOWLObjectProperty(IRI.create(AnnotationProperty.PART_OF.getIRI()));
		labelProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.LABEL.getIRI()));
		synonymProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.SYNONYM.getIRI()));
		definitionProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.DEFINITION.getIRI()));
		creationDateProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.CREATION_DATE.getIRI()));
		createdByProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.CREATED_BY.getIRI()));
		relatedSynonymProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.RELATED_SYNONYM.getIRI()));
		narrowSynonymProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.NARROW_SYNONYM.getIRI()));
		exactSynonymProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.EXACT_SYNONYM.getIRI()));
		broadSynonymProperty = om.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.BROAD_SYNONYM.getIRI()));
	}
		
	public void addSuperclass(OWLOntology o, OWLClass subclass, OWLClass superclass) {
		OWLAxiom subclassAxiom = om.getOWLDataFactory().getOWLSubClassOfAxiom(subclass, superclass);
		om.addAxiom(o, subclassAxiom);
	}
	
	public void addPartOf(OWLOntology o, OWLClass oc, OWLClass poc) {
		OWLClassExpression partOfExpression = 
				om.getOWLDataFactory().getOWLObjectSomeValuesFrom(partOfProperty, poc);
		OWLAxiom partOfAxiom = om.getOWLDataFactory().getOWLSubClassOfAxiom(oc, partOfExpression);
		om.addAxiom(o, partOfAxiom);
	}
	
	public void addSynonym(OWLOntology o, String synonym, OWLClass prefc) {
		OWLAnnotation synonymAnnotation = om.getOWLDataFactory().getOWLAnnotation(exactSynonymProperty, om.getOWLDataFactory().getOWLLiteral(synonym, "en"));
		OWLAxiom synonymAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(prefc.getIRI(), synonymAnnotation);
		om.addAxiom(o, synonymAxiom);
	}	
	
	
	public void addDeclaration(OWLOntology owlOntology, OWLClass newOwlClass) {
		OWLAxiom declarationAxiom = om.getOWLDataFactory().getOWLDeclarationAxiom(newOwlClass);
		om.addAxiom(owlOntology, declarationAxiom);
	}

	public void addDefinition(OWLOntology owlOntology, OWLClass owlClass, String definition) {
		OWLAnnotation definitionAnnotation = om.getOWLDataFactory().getOWLAnnotation(definitionProperty, om.getOWLDataFactory().getOWLLiteral(definition, "en")); 
		OWLAxiom definitionAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), definitionAnnotation); 
		om.addAxiom(owlOntology, definitionAxiom);
	}
		
	public void addSourceSampleComment(OWLOntology owlOntology, String source, String sample, OWLClass owlClass) {
		OWLAnnotation commentAnnotation = om.getOWLDataFactory().getOWLAnnotation(om.getOWLDataFactory().getRDFSComment(), 
				om.getOWLDataFactory().getOWLLiteral("source: " + sample + "[taken from: " + 
						source + "]", "en"));
		OWLAxiom commentAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), commentAnnotation);
		om.addAxiom(owlOntology, commentAxiom);
	}
	
	public void addLabel(OWLOntology owlOntology, OWLClass owlClass, OWLLiteral classLabelLiteral) {
		OWLAnnotation labelAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, classLabelLiteral);
		OWLAxiom labelAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), labelAnnotation);
		om.addAxiom(owlOntology, labelAxiom);
	}
	
	public void addCreationDate(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation creationDateAnnotation = om.getOWLDataFactory().getOWLAnnotation(creationDateProperty, om.getOWLDataFactory().getOWLLiteral(dateFormat.format(new Date())));
		OWLAxiom creationDateAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), creationDateAnnotation);
		om.addAxiom(owlOntology, creationDateAxiom);
	}

	public void addCreatedBy(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation createdByAnnotation = om.getOWLDataFactory().getOWLAnnotation(createdByProperty, om.getOWLDataFactory().getOWLLiteral(Ontologize.user));
		OWLAxiom createdByAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), createdByAnnotation);		
		om.addAxiom(owlOntology, createdByAxiom);
	}
	
	public void addQualitySubclass(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAxiom subclassAxiom = om.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, qualityClass);
		om.addAxiom(owlOntology, subclassAxiom);
	}
	
	public void addEntitySubclass(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAxiom subclassAxiom = om.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, entityClass);
		om.addAxiom(owlOntology, subclassAxiom);
	}

	public void addDefaultAxioms(OWLOntology owlOntology) {
		//add annotation properties
		//OWLAnnotationProperty label = factory
		//		.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		/*
		    <owl:AnnotationProperty rdf:about="http://purl.obolibrary.org/obo/IAO_0000115">
	        	<rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">definition</rdfs:label>
	    	</owl:AnnotationProperty>
		 */
		//OWLAnnotationProperty annotation = factory.getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		OWLLiteral definitionLiteral = om.getOWLDataFactory().getOWLLiteral("definition");
		OWLAnnotation definitionAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, definitionLiteral);
		OWLAxiom definitionAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(definitionProperty.getIRI(), definitionAnnotation);
		om.addAxiom(owlOntology, definitionAxiom);

		/*<owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym">
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_broad_synonym</rdfs:label>
    	</owl:AnnotationProperty>*/
		
		OWLLiteral hasBroadSynonymLiteral = om.getOWLDataFactory().getOWLLiteral("has_broad_synonym");
		OWLAnnotation broadSynonymAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, hasBroadSynonymLiteral);
		OWLAxiom broadSynonymAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(broadSynonymProperty.getIRI(), broadSynonymAnnotation);
		om.addAxiom(owlOntology, broadSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasExactSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_exact_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasExactSynonymLiteral = om.getOWLDataFactory().getOWLLiteral("has_exact_synonym");
		OWLAnnotation exactSynonymAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, hasExactSynonymLiteral);
		OWLAxiom exactSynonymAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(exactSynonymProperty.getIRI(), exactSynonymAnnotation);
		om.addAxiom(owlOntology, exactSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_narrow_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasNarrowSynonymLiteral = om.getOWLDataFactory().getOWLLiteral("has_narrow_synonym");
		OWLAnnotation narrowSynonymAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, hasNarrowSynonymLiteral);
		OWLAxiom narrowSynonymAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(narrowSynonymProperty.getIRI(), narrowSynonymAnnotation);
		om.addAxiom(owlOntology, narrowSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_related_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasRelatedSynonymLiteral = om.getOWLDataFactory().getOWLLiteral("has_related_synonym");
		OWLAnnotation relatedSynonymAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, hasRelatedSynonymLiteral);
		OWLAxiom relatedSynonymAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(relatedSynonymProperty.getIRI(), relatedSynonymAnnotation);
		om.addAxiom(owlOntology, relatedSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#created_by"/>*/
		OWLLiteral createdByLiteral = om.getOWLDataFactory().getOWLLiteral("created_by");
		OWLAnnotation createdByAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, createdByLiteral);
		OWLAxiom createdByAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(createdByProperty.getIRI(), createdByAnnotation);
		om.addAxiom(owlOntology, createdByAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#creation_date"/>*/
		OWLLiteral creationDateLiteral = om.getOWLDataFactory().getOWLLiteral("creation_date");
		OWLAnnotation createionDateAnnotation = om.getOWLDataFactory().getOWLAnnotation(labelProperty, creationDateLiteral);
		OWLAxiom createionDateAxiom = om.getOWLDataFactory().getOWLAnnotationAssertionAxiom(creationDateProperty.getIRI(), createionDateAnnotation);
		om.addAxiom(owlOntology, createionDateAxiom);

		//entity and quality classes and part_of, has_part relations are imported from ro, a "general" ontology
		//PrefixManager pm = new DefaultPrefixManager(
		//		Configuration.etc_ontology_baseIRI+prefix.toLowerCase()+"#");

		
		/*OWLClass entity = factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		OWLLiteral clabel = factory.getOWLLiteral("material anatomical entity", "en");
		axiom = factory.getOWLDeclarationAxiom(entity);
		manager.addAxiom(ont, axiom);
		axiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), factory.getOWLAnnotation(label, clabel));
		manager.addAxiom(ont, axiom);

		OWLClass quality = factory.getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		clabel = factory.getOWLLiteral("quality", "en");
		axiom = factory.getOWLDeclarationAxiom(entity);
		manager.addAxiom(ont, axiom);
		axiom = factory.getOWLAnnotationAssertionAxiom(entity.getIRI(), factory.getOWLAnnotation(label, clabel));
		manager.addAxiom(ont, axiom);

		//has_part/part_of inverse object properties
		OWLObjectProperty hasPart = factory.getOWLObjectProperty(":has_part", pm);
		OWLObjectProperty partOf = factory.getOWLObjectProperty(":part_of", pm);
		manager.addAxiom(ont,
				factory.getOWLInverseObjectPropertiesAxiom(hasPart, partOf));

		manager.addAxiom(ont, factory.getOWLTransitiveObjectPropertyAxiom(partOf));
		manager.addAxiom(ont, factory.getOWLTransitiveObjectPropertyAxiom(hasPart));
		*/
		
		//disjoint entity and quality classes
		OWLAxiom disjointClassesAxiom = om.getOWLDataFactory().getOWLDisjointClassesAxiom(entityClass, qualityClass);
		om.addAxiom(owlOntology, disjointClassesAxiom);
	}


}