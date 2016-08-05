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

	private OWLOntologyManager owlOntologyManager;
	private ModuleCreator moduleCreator;
	
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
	private OntologyReasoner ontologyReasoner;

	public AxiomManager(OWLOntologyManager owlOntologyManager, ModuleCreator moduleCreator, OntologyReasoner ontologyReasoner) {
		this.owlOntologyManager = owlOntologyManager;
		this.moduleCreator = moduleCreator;
		this.ontologyReasoner = ontologyReasoner;
		
		entityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(Type.ENTITY.getIRI())); //material anatomical entity
		qualityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(Type.QUALITY.getIRI())); //quality
		partOfProperty = owlOntologyManager.getOWLDataFactory().getOWLObjectProperty(IRI.create(AnnotationProperty.PART_OF.getIRI()));
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.LABEL.getIRI()));
		synonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.SYNONYM.getIRI()));
		definitionProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.DEFINITION.getIRI()));
		creationDateProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.CREATION_DATE.getIRI()));
		createdByProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.CREATED_BY.getIRI()));
		relatedSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.RELATED_SYNONYM.getIRI()));
		narrowSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.NARROW_SYNONYM.getIRI()));
		exactSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.EXACT_SYNONYM.getIRI()));
		broadSynonymProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(AnnotationProperty.BROAD_SYNONYM.getIRI()));
	}
			
	public void addSuperclasses(Collection collection, OWLOntology owlOntology, Ontology ontology,
			OWLClass owlClass, List<Superclass> superclasses, Type type) throws Exception {		
		for(Superclass superclass : superclasses) {
			OWLClass superOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(superclass.getIri())); 
			OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, superOwlClass);
			owlOntologyManager.addAxiom(owlOntology, subclassAxiom);

			if(type != null) {
				Set<OWLClass> introducedClasses = new HashSet<OWLClass> ();
				OWLOntology moduleOntology = moduleCreator.create(collection, superOwlClass, ontology);
				introducedClasses.addAll(moduleOntology.getClassesInSignature());

				OWLClass owlSuperclass = null;
				switch(type) {
				case ENTITY:
					owlSuperclass = entityClass;
					break;
				case QUALITY:
					owlSuperclass = qualityClass;
					break;
				}	
				if(superclass != null) {
					for(OWLClass introducedClass : introducedClasses){
						subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(introducedClass, owlSuperclass);
						owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
					}
				}
			}
		}
	}
	
	public void removeSuperclasses(OWLOntology owlOntology, OWLClass owlClass) {
		Set<OWLClassAxiom> classAxioms = owlOntology.getAxioms(owlClass, Imports.EXCLUDED);
		Set<OWLSubClassOfAxiom> toRemoveAxioms = new HashSet<OWLSubClassOfAxiom>();
		for(OWLClassAxiom axiom : classAxioms) {
			if(axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom owlSubClassOfAxiom = (OWLSubClassOfAxiom)axiom;
				OWLClassExpression superclass = owlSubClassOfAxiom.getSuperClass();
				if(owlSubClassOfAxiom.getSubClass().equals(owlClass) && !superclass.getObjectPropertiesInSignature().contains(partOfProperty)) {
					toRemoveAxioms.add(owlSubClassOfAxiom);
				}
			}
		}
		for(OWLSubClassOfAxiom axiom : toRemoveAxioms)
			owlOntologyManager.removeAxiom(owlOntology, axiom);
	}
	
	public void addPartOfs(Collection collection, OWLOntology owlOntology, Ontology ontology, OWLClass owlClass, List<PartOf> partOfs) throws Exception {
		for(PartOf partOf : partOfs) {			
			Set<OWLClass> introducedClasses = new HashSet<OWLClass> ();

			IRI partOfIRI = IRI.create(partOf.getIri());
			OWLClass wholeOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(partOfIRI);
			if(!partOf.getIri().startsWith(Configuration.etcOntologyBaseIRI)) {
				wholeOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(partOfIRI);
				OWLOntology moduleOntology = moduleCreator.create(collection, wholeOwlClass, ontology);
				introducedClasses.addAll(moduleOntology.getClassesInSignature());
			}
			if(!ontologyReasoner.isSubclass(owlOntology, wholeOwlClass, qualityClass)) {
				OWLClassExpression partOfExpression = owlOntologyManager.getOWLDataFactory().getOWLObjectSomeValuesFrom(partOfProperty, wholeOwlClass);
				OWLAxiom partOfAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, partOfExpression);
				owlOntologyManager.addAxiom(owlOntology, partOfAxiom);
				for(OWLClass introducedClass : introducedClasses) {
					addEntitySubclass(owlOntology, introducedClass);
				}
			} else {
				throw new Exception("Can't create part of quality");
			}
		}
	}
		
	public void removePartOfs(OWLOntology owlOntology, OWLClass owlClass) {
		Set<OWLClassAxiom> classAxioms = owlOntology.getAxioms(owlClass, Imports.EXCLUDED);
		Set<OWLSubClassOfAxiom> toRemoveAxioms = new HashSet<OWLSubClassOfAxiom>();
		for(OWLClassAxiom axiom : classAxioms) 
			if(axiom instanceof OWLSubClassOfAxiom) {
				OWLSubClassOfAxiom owlSubClassOfAxiom = (OWLSubClassOfAxiom)axiom;
				OWLClassExpression superclass = owlSubClassOfAxiom.getSuperClass();
				if(owlSubClassOfAxiom.getSubClass().equals(owlClass) && superclass.getObjectPropertiesInSignature().contains(partOfProperty)) 
					toRemoveAxioms.add(owlSubClassOfAxiom);
			}
		for(OWLSubClassOfAxiom axiom : toRemoveAxioms)
			owlOntologyManager.removeAxiom(owlOntology, axiom);
	}
	
	public void addSynonyms(OWLOntology owlOntology, OWLClass owlClass, List<Synonym> synonyms) {
		for(Synonym synonym : synonyms) 
			addSynonym(owlOntology, owlClass, synonym);
	}
	
	public void addSynonym(OWLOntology owlOntology, OWLClass owlClass, Synonym synonym) {
		OWLAnnotation synonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(exactSynonymProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(synonym.getSynonym(), "en"));
		OWLAxiom synonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), synonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, synonymAxiom);
	}

	
	public void removeSynonyms(OWLOntology targetOwlOntology, OWLClass owlClass) {
		removeAnnotationAssertionAxiom(targetOwlOntology, owlClass, exactSynonymProperty);
	}
	
	public void removeSynonym(OWLOntology targetOwlOntology, OWLClass owlClass, Synonym synonym) {
		Set<OWLAnnotationAssertionAxiom> axioms = targetOwlOntology.getAnnotationAssertionAxioms(owlClass.getIRI());
		OWLAnnotationAssertionAxiom toRemoveAxiom = null;
		for(OWLAnnotationAssertionAxiom axiom : axioms) 
			if(axiom.getProperty().equals(exactSynonymProperty)) {
				Optional<OWLLiteral> literal = axiom.getValue().asLiteral();
				if(literal.isPresent() && literal.get().getLiteral().equals(synonym.getSynonym())) {
					toRemoveAxiom = axiom;
					break;
				}
			}
		owlOntologyManager.removeAxiom(targetOwlOntology, toRemoveAxiom);
	}
	
	public void deprecate(IRI iri, OWLOntology owlOntology) {
		OWLAxiom depreceatedAxiom = owlOntologyManager.getOWLDataFactory().getDeprecatedOWLAnnotationAssertionAxiom(iri);
		owlOntologyManager.applyChange(new AddAxiom(owlOntology, depreceatedAxiom));
	}

	private void removeAnnotationAssertionAxiom(OWLOntology targetOwlOntology,	OWLClass owlClass, OWLAnnotationProperty owlAnnotationProperty) {
		Set<OWLAnnotationAssertionAxiom> axioms = targetOwlOntology.getAnnotationAssertionAxioms(owlClass.getIRI());
		Set<OWLAnnotationAssertionAxiom> toRemoveAxioms = new HashSet<OWLAnnotationAssertionAxiom>();
		for(OWLAnnotationAssertionAxiom axiom : axioms) 
			if(axiom.getProperty().equals(owlAnnotationProperty)) 
				toRemoveAxioms.add(axiom);
		for(OWLAnnotationAssertionAxiom axiom : toRemoveAxioms)
			owlOntologyManager.removeAxiom(targetOwlOntology, axiom);
	}
	

	public void addDeclaration(OWLOntology owlOntology, OWLClass newOwlClass) {
		OWLAxiom declarationAxiom = owlOntologyManager.getOWLDataFactory().getOWLDeclarationAxiom(newOwlClass);
		owlOntologyManager.addAxiom(owlOntology, declarationAxiom);
	}

	public void addDefinition(OWLOntology owlOntology, OWLClass owlClass, String definition) {
		OWLAnnotation definitionAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(definitionProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(definition, "en")); 
		OWLAxiom definitionAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), definitionAnnotation); 
		owlOntologyManager.addAxiom(owlOntology, definitionAxiom);
	}
		
	public void removeDefinition(OWLOntology targetOwlOntology, OWLClass owlClass) {
		removeAnnotationAssertionAxiom(targetOwlOntology, owlClass, definitionProperty);
	}
	
	public void addSourceSampleComment(OWLOntology owlOntology, IRITerm it, OWLClass owlClass) {
		if(it.hasSource() || it.hasSampleSentence()){
			OWLAnnotation commentAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(owlOntologyManager.getOWLDataFactory().getRDFSComment(), 
					owlOntologyManager.getOWLDataFactory().getOWLLiteral("source: " + it.sampleSentence + "[taken from: " + 
							it.source + "]", "en"));
			OWLAxiom commentAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), commentAnnotation);
			owlOntologyManager.addAxiom(owlOntology, commentAxiom);
		}
	}
	
	public void removeSourceSampleComment(OWLOntology targetOwlOntology, OWLClass owlClass) {
		OWLAnnotationProperty commentAnnotationProperty = owlOntologyManager.getOWLDataFactory().getRDFSComment();
		Set<OWLAnnotationAssertionAxiom> axioms = targetOwlOntology.getAnnotationAssertionAxioms(owlClass.getIRI());
		
		OWLAnnotationAssertionAxiom oldCommentAxiom = null;
		for(OWLAnnotationAssertionAxiom axiom : axioms) {
			if(axiom.getProperty().equals(commentAnnotationProperty)) {
				Optional<OWLLiteral> literal = axiom.getValue().asLiteral();
				if(literal.isPresent() && literal.get().getLiteral().startsWith("source:")) {
					oldCommentAxiom = axiom;
					break;
				}
			}
		}
		if(oldCommentAxiom != null)
			owlOntologyManager.removeAxiom(targetOwlOntology, oldCommentAxiom);
	}
	
	public void addLabel(OWLOntology owlOntology, OWLClass owlClass, OWLLiteral classLabelLiteral) {
		OWLAnnotation labelAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, classLabelLiteral);
		OWLAxiom labelAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), labelAnnotation);
		owlOntologyManager.addAxiom(owlOntology, labelAxiom);
	}
	
	public void addCreationDate(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation creationDateAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(creationDateProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(dateFormat.format(new Date())));
		OWLAxiom creationDateAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), creationDateAnnotation);
		owlOntologyManager.addAxiom(owlOntology, creationDateAxiom);
	}

	public void addCreatedBy(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAnnotation createdByAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(createdByProperty, owlOntologyManager.getOWLDataFactory().getOWLLiteral(Ontologize.user));
		OWLAxiom createdByAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(owlClass.getIRI(), createdByAnnotation);		
		owlOntologyManager.addAxiom(owlOntology, createdByAxiom);
	}
	
	public void addQualitySubclass(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, qualityClass);
		owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
	}
	
	public void addEntitySubclass(OWLOntology owlOntology, OWLClass owlClass) {
		OWLAxiom subclassAxiom = owlOntologyManager.getOWLDataFactory().getOWLSubClassOfAxiom(owlClass, entityClass);
		owlOntologyManager.addAxiom(owlOntology, subclassAxiom);
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
		OWLLiteral definitionLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("definition");
		OWLAnnotation definitionAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, definitionLiteral);
		OWLAxiom definitionAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(definitionProperty.getIRI(), definitionAnnotation);
		owlOntologyManager.addAxiom(owlOntology, definitionAxiom);

		/*<owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasBroadSynonym">
        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_broad_synonym</rdfs:label>
    	</owl:AnnotationProperty>*/
		
		OWLLiteral hasBroadSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_broad_synonym");
		OWLAnnotation broadSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasBroadSynonymLiteral);
		OWLAxiom broadSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(broadSynonymProperty.getIRI(), broadSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, broadSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasExactSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_exact_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasExactSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_exact_synonym");
		OWLAnnotation exactSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasExactSynonymLiteral);
		OWLAxiom exactSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(exactSynonymProperty.getIRI(), exactSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, exactSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasNarrowSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_narrow_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasNarrowSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_narrow_synonym");
		OWLAnnotation narrowSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasNarrowSynonymLiteral);
		OWLAxiom narrowSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(narrowSynonymProperty.getIRI(), narrowSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, narrowSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#hasRelatedSynonym">
	        <rdfs:label rdf:datatype="http://www.w3.org/2001/XMLSchema#string">has_related_synonym</rdfs:label>
	    </owl:AnnotationProperty>*/
		OWLLiteral hasRelatedSynonymLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("has_related_synonym");
		OWLAnnotation relatedSynonymAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, hasRelatedSynonymLiteral);
		OWLAxiom relatedSynonymAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(relatedSynonymProperty.getIRI(), relatedSynonymAnnotation);
		owlOntologyManager.addAxiom(owlOntology, relatedSynonymAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#created_by"/>*/
		OWLLiteral createdByLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("created_by");
		OWLAnnotation createdByAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, createdByLiteral);
		OWLAxiom createdByAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(createdByProperty.getIRI(), createdByAnnotation);
		owlOntologyManager.addAxiom(owlOntology, createdByAxiom);

		/*
	    <owl:AnnotationProperty rdf:about="http://www.geneontology.org/formats/oboInOwl#creation_date"/>*/
		OWLLiteral creationDateLiteral = owlOntologyManager.getOWLDataFactory().getOWLLiteral("creation_date");
		OWLAnnotation createionDateAnnotation = owlOntologyManager.getOWLDataFactory().getOWLAnnotation(labelProperty, creationDateLiteral);
		OWLAxiom createionDateAxiom = owlOntologyManager.getOWLDataFactory().getOWLAnnotationAssertionAxiom(creationDateProperty.getIRI(), createionDateAnnotation);
		owlOntologyManager.addAxiom(owlOntology, createionDateAxiom);

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
		OWLAxiom disjointClassesAxiom = owlOntologyManager.getOWLDataFactory().getOWLDisjointClassesAxiom(entityClass, qualityClass);
		owlOntologyManager.addAxiom(owlOntology, disjointClassesAxiom);
	}
	
}