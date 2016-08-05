package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyInputSourceException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.oto.server.db.OntologyDAO;
import edu.arizona.biosemantics.oto2.oto.server.db.Query.QueryException;

public class PermanentOntologyFileDAO {

	protected static Map<Ontology, OWLOntology> permanentOntologies = new HashMap<Ontology, OWLOntology>();
	
	public static void loadPermanentOntologies() {
		OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
		try {
			for(Ontology ontology : new edu.arizona.biosemantics.oto2.ontologize.server.persist.db.OntologyDAO().getBioportalOntologies()) {
				File file = getPermanentOntologyFile(ontology);
				Logger.getLogger(OntologyFileDAO.class).info("Loading " + file.getAbsolutePath() + " ...");
				try {
					owlOntologyManager.getIRIMappers().add(createMapper(ontology));
					OWLOntology owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(file);
					permanentOntologies.put(ontology, owlOntology);
				} catch (OWLOntologyCreationException | OWLOntologyInputSourceException | UnloadableImportException e) {
					Logger.getLogger(OntologyFileDAO.class).error("Could not load ontology", e);
				}
			}
		} catch(QueryException e) {
			Logger.getLogger(OntologyFileDAO.class).error("Could not get permanent ontologies", e);
		}
	}
	
	protected static OWLOntologyIRIMapper createMapper(Ontology ontology) {
		if(!ontology.isBioportalOntology())
			return new SimpleIRIMapper(createOntologyIRI(ontology), getLocalOntologyIRI(ontology));
		else
			return new SimpleIRIMapper(createOntologyIRI(ontology), getLocalOntologyIRI(ontology));
	}
	
	protected static IRI createOntologyIRI(OntologySubmission submission) {
		return IRI.create(submission.getOntology().getIri());
	}
	
	protected static IRI createOntologyIRI(Ontology ontology) {
		return IRI.create(ontology.getIri());
	}
		
	protected static IRI getLocalOntologyIRI(Ontology ontology) {
		if(!ontology.isBioportalOntology()) {
			return IRI.create(getCollectionOntologyFile(ontology));
		} else 
			return IRI.create(getPermanentOntologyFile(ontology));
	}
	
	protected static File getPermanentOntologyFile(Ontology ontology) {
		return new File(Configuration.permanentOntologyDirectory, ontology.getAcronym().toLowerCase() + ".owl");
	}
	
	protected static File getCollectionOntologyFile(Ontology ontology) {
		return new File(getCollectionOntologyDirectory(ontology), ontology.getAcronym().toLowerCase() + ".owl");
	}
	
	protected static File getCollectionOntologyDirectory(Ontology ontology) {
		return new File(Configuration.collectionOntologyDirectory + File.separator + ontology.getCreatedInCollectionId() + File.separator + ontology.getAcronym());
	}
	
	protected static boolean isEtcOntologyIRI(String iri) {
		if(iri == null)
			return false;
		return iri.trim().startsWith(Configuration.etcOntologyBaseIRI);
	}
	
	protected static File getCollectionDirectory(Collection collection) {
		return new File(Configuration.collectionOntologyDirectory + File.separator + collection.getId());
	}
	
	protected static boolean containsOwlClass(OWLOntology owlOntology, OWLClass owlClass) {
		return owlOntology.containsEntityInSignature(owlClass);
	}
	
	protected OntologyDAO ontologyDBDAO;
	protected OWLOntologyManager owlOntologyManager;

	protected AxiomManager axiomManager;
	protected ModuleCreator moduleCreator;
	protected OWLOntologyRetriever owlOntologyRetriever;
	protected AnnotationsManager annotationsManager;
	protected OntologyReasoner ontologyReasoner;
	
	//OWL entities
	protected OWLClass entityClass;
	protected OWLClass qualityClass;
	protected OWLAnnotationProperty labelProperty;
	protected OWLAnnotationProperty definitionProperty;

	public PermanentOntologyFileDAO(OntologyDAO ontologyDBDAO) throws OWLOntologyCreationException {
		this.ontologyDBDAO = ontologyDBDAO;
		this.owlOntologyManager = OWLManager.createOWLOntologyManager();
		try {
			for(Ontology ontology : permanentOntologies.keySet()) {
				owlOntologyManager.getIRIMappers().add(createMapper(ontology));
				//OWLOntology clonedOwlOntology = owlOntologyManager.createOntology(createOntologyIRI(ontology));
				OWLOntology clonedOwlOntology = clone(permanentOntologies.get(ontology));
			}
		} catch (OWLOntologyCreationException e) {
			log(LogLevel.ERROR, "Failed to initialize ontology manager. Relevant ontologies could not be retrieved or created.", e);
			throw e;
		}
		
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		entityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/CARO_0000006")); //material anatomical entity
		qualityClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create("http://purl.obolibrary.org/obo/PATO_0000001")); //quality
		definitionProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		ontologyReasoner = new OntologyReasoner(owlOntologyManager);
		owlOntologyRetriever = new OWLOntologyRetriever(owlOntologyManager, ontologyDBDAO);
		annotationsManager = new AnnotationsManager(owlOntologyRetriever);
		moduleCreator = new ModuleCreator(owlOntologyManager, owlOntologyRetriever, annotationsManager);
		axiomManager = new AxiomManager(owlOntologyManager, moduleCreator, ontologyReasoner);
	}
	
	//according to http://answers.semanticweb.com/questions/25651/how-to-clone-a-loaded-owl-ontology
	private OWLOntology clone(OWLOntology originalOwlOntology) throws OWLOntologyCreationException {
		return owlOntologyManager.copyOntology(originalOwlOntology, OntologyCopy.DEEP);
		/*try {
			OWLOntology clonedOwlOntology = owlOntologyManager.createOntology(originalOwlOntology.getOntologyID().getOntologyIRI());
			owlOntologyManager.addAxioms(clonedOwlOntology, originalOwlOntology.getAxioms());
			
			for(OWLImportsDeclaration owlImportsDeclaration : originalOwlOntology.getImportsDeclarations()) {
				owlOntologyManager.applyChange(new AddImport(clonedOwlOntology, owlImportsDeclaration));
			}
			
			for(OWLOntology importedOntology : originalOwlOntology.getImports()) {
				clone(importedOntology);
			}
			return clonedOwlOntology;
		} catch(OWLOntologyAlreadyExistsException e) { }
		return null;*/
		
	}

	public String getClassLabel(String classIRI) throws Exception {
		OWLClass owlClass = getOWLClass(classIRI);
		String label = annotationsManager.get(owlClass, labelProperty);
		return label;
	}
	
	public OWLClass getOWLClass(String classIRI) {
		return owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(classIRI));
	}
	
	public OWLOntology getOWLOntology(Collection collection, String classIRI) throws Exception {
		OWLClass owlClass = this.getOWLClass(classIRI);
		if(owlClass != null)
			return owlOntologyRetriever.getOWLOntology(collection, owlClass);
		return null;
	}

	public boolean containsInPermanentOntologies(String iri) {
		try {
			OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(iri));
			if(owlClass == null)
				return false;
			OWLOntology owlOntology = owlOntologyRetriever.getPermanentOWLOntology(owlClass);
			if(owlOntology == null)
				return false;
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public OWLClass getQualityClass() {
		return qualityClass;
	}
	
	public OWLClass getEntityClass() {
		return entityClass;
	}

	public OntologyReasoner getReasoner() {
		return ontologyReasoner;
	}
}