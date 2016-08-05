package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto.model.lite.Synonym;
import edu.arizona.biosemantics.oto2.oto.server.db.OntologyDAO;
import edu.arizona.biosemantics.oto2.oto.server.db.Query.QueryException;

public class OntologyFileDAO extends PermanentOntologyFileDAO {
	
	private Collection collection;

	public OntologyFileDAO(Collection collection, OntologyDAO ontologyDBDAO) throws Exception {
		super(ontologyDBDAO);
		this.collection = collection;
	}

	public void insertOntology(Ontology ontology) throws Exception {	
		createAndCleanOntologyDirectory(ontology);
		OWLOntology owlOntology = null;
		try {
			owlOntology = owlOntologyManager.createOntology(IRI.create(ontology.getIri()));
		} catch (OWLOntologyCreationException e) {
			log(LogLevel.ERROR, "Couldn't create ontology", e);
			throw e;
		}
		addDefaultImportOntologies(collection, owlOntology);
		axiomManager.addDefaultAxioms(owlOntology);
		try {
			owlOntologyManager.saveOntology(owlOntology, getLocalOntologyIRI(ontology));
		} catch (OWLOntologyStorageException e) {
			log(LogLevel.ERROR, "Couldn't save ontology", e);
			throw e;
		}
	}
	
	private void createAndCleanOntologyDirectory(Ontology ontology) {
		File collectionOntologyDirectory = getCollectionOntologyDirectory(ontology);
		if(collectionOntologyDirectory.exists()) {
			for(File child : collectionOntologyDirectory.listFiles())
				child.delete();
			collectionOntologyDirectory.delete();
		}
		collectionOntologyDirectory.mkdir();
	}

	private void addDefaultImportOntologies(Collection collection, OWLOntology owlOntology) {
		List<Ontology> relevantOntologies = new LinkedList<Ontology>();
		try {
			relevantOntologies = ontologyDBDAO.getRelevantOntologiesForCollection(collection);
		} catch (QueryException e) {
			log(LogLevel.ERROR, "Could not add relevant ontologies", e);
		}
		for(Ontology relevantOntology : relevantOntologies) {
			//only import RO per default at this time
			if(relevantOntology.getIri().equals("http://purl.bioontology.org/obo/OBOREL")) {
				addImportDeclaration(owlOntology, relevantOntology);
			}
			//if(!relevantOntology.hasCollectionId()) {
			//	addImportDeclaration(owlOntology, relevantOntology);
			//}
		}
	}

	private void addImportDeclaration(OWLOntology owlOntology, Ontology ontology) {
		IRI relevantIRI = IRI.create(ontology.getIri());
		OWLImportsDeclaration importDeclaraton = owlOntologyManager.getOWLDataFactory().getOWLImportsDeclaration(relevantIRI);
		owlOntologyManager.applyChange(new AddImport(owlOntology, importDeclaraton));
	}
	
	public String insertClassSubmission(OntologyClassSubmission submission) throws Exception {	
		OWLOntology owlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		OWLClass owlClass = createOwlClass(submission);
		
		boolean foreignClass = submission.hasClassIRI() ? !isEtcOntologyIRI(submission.getClassIRI()) : false;
		if(!foreignClass) {
			axiomManager.addDeclaration(owlOntology, owlClass);
			axiomManager.addDefinition(owlOntology, owlClass, submission.getDefinition());
			axiomManager.addLabel(owlOntology, owlClass, owlOntologyManager.getOWLDataFactory().getOWLLiteral(
					submission.getSubmissionTerm(), "en"));
			axiomManager.addCreatedBy(owlOntology, owlClass);
			axiomManager.addCreationDate(owlOntology, owlClass);
			axiomManager.addSourceSampleComment(owlOntology, submission, owlClass);
		}
		
		addSuperclasses(submission, owlClass);
		axiomManager.addSynonyms(owlOntology, owlClass, submission.getSynonyms());
		axiomManager.addPartOfs(collection, owlOntology, submission.getOntology(), owlClass, submission.getPartOfs());
		ontologyReasoner.checkConsistency(owlOntology);
		owlOntologyManager.saveOntology(owlOntology, getLocalOntologyIRI(submission.getOntology()));
		return owlClass.getIRI().toString();
	}
	
	private OWLClass createOwlClass(OntologyClassSubmission submission) throws Exception {
		boolean foreignClass = submission.hasClassIRI() ? !isEtcOntologyIRI(submission.getClassIRI()) : false;
		if(foreignClass) {
			return createForeignClassModule(collection, submission);
		} else {
			return owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(submission.getClassIRI()));
		}
	}

	private void addSuperclasses(OntologyClassSubmission submission, OWLClass owlClass) throws Exception {
		boolean foreignClass = submission.hasClassIRI() ? !isEtcOntologyIRI(submission.getClassIRI()) : false;
		OWLOntology owlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		
		if(submission.hasSuperclasses() && !foreignClass){
			checkConsistentSuperclassHierarchyWithQualityEntity(submission, owlOntology);
			
			List<Superclass> superclasses = new LinkedList<Superclass>(submission.getSuperclasses());			
			axiomManager.addSuperclasses(collection, owlOntology, submission.getOntology(), owlClass, 
					superclasses, submission.getType());
		}
	}

	private void checkConsistentSuperclassHierarchyWithQualityEntity(OntologyClassSubmission submission, OWLOntology owlOntology) throws Exception {
		List<Superclass> superclasses = submission.getSuperclasses();
		for(Superclass superclass : superclasses) {
			OWLClass superOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(superclass.getIri())); 
			if(submission.getType().equals(Type.QUALITY)) 
				if(ontologyReasoner.isSubclass(owlOntology, superOwlClass, entityClass)) 
					throw new Exception("Can not add the quality term '" + submission.getSubmissionTerm() + 
							"' as a child to entity term '" + superclass + "'.");
			if(submission.getType().equals(Type.ENTITY)) 
				if(ontologyReasoner.isSubclass(owlOntology, superOwlClass, qualityClass)) 
					throw new Exception("Can not add the entity term '" + submission.getSubmissionTerm() + 
							"' as a child to quality term '" + superclass + "'.");
		}
	}

	private OWLClass createForeignClassModule(Collection collection, OntologySubmission submission) throws Exception {
		OWLClass newOwlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(submission.getClassIRI()));
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		OWLOntology moduleOwlOntology = moduleCreator.create(collection, newOwlClass, submission.getOntology());
		if (submission.getType().equals(Type.ENTITY)) {
			if (!ontologyReasoner.isSubclass(targetOwlOntology, newOwlClass, entityClass)) {
				for (OWLClass owlClass : moduleOwlOntology.getClassesInSignature()) {
					axiomManager.addEntitySubclass(targetOwlOntology, owlClass);
				}	
			}
		}
		if (submission.getType().equals(Type.QUALITY)) {
			if (!ontologyReasoner.isSubclass(targetOwlOntology, newOwlClass, qualityClass)) {
				for (OWLClass owlClass : moduleOwlOntology.getClassesInSignature()) {
					axiomManager.addQualitySubclass(targetOwlOntology, owlClass);
				}
			}
		}
		return newOwlClass;
	}

	public String insertSynonymSubmission(OntologySynonymSubmission submission) throws Exception { 
		OWLOntology targetOwlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(submission.getClassIRI()));
		determineAndSetSubmissionType(submission);
		boolean isContained = containsOwlClass(targetOwlOntology, owlClass);
		String label = annotationsManager.get(collection, owlClass, labelProperty);
		if(isContained && label != null && !label.equals(submission.getSubmissionTerm())) {
			axiomManager.addSynonym(targetOwlOntology, owlClass, new Synonym(submission.getSubmissionTerm()));
		} else if(!isContained) {
			owlClass = createForeignClassModule(collection, submission);  
			axiomManager.addSynonym(targetOwlOntology, owlClass, new Synonym(submission.getSubmissionTerm()));
		}
		
		if(owlClass != null) 
			addAdditionalSynonyms(targetOwlOntology, owlClass, submission);
		
		for(Synonym synonym : submission.getSynonyms()){
			axiomManager.addSynonym(targetOwlOntology, owlClass, synonym);
			owlOntologyManager.saveOntology(targetOwlOntology, getLocalOntologyIRI(submission.getOntology()));
		}
		
		return submission.getClassIRI().toString();
	}	
	
	private void addAdditionalSynonyms(OWLOntology targetOwlOntology, 
			OWLClass targetClass, HasSynonym hasSynonym) {
		for(Synonym synonym : hasSynonym.getSynonyms()){
			axiomManager.addSynonym(targetOwlOntology, targetClass, synonym);
		}
	}

	private void determineAndSetSubmissionType(OntologySynonymSubmission submission) throws Exception {
		OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(submission.getClassIRI()));
		OWLOntology classOwlOntology = owlOntologyRetriever.getOWLOntology(collection, owlClass);
		if(ontologyReasoner.isSubclass(classOwlOntology, owlClass, qualityClass)) {
			submission.setType(Type.QUALITY);
		} else if(ontologyReasoner.isSubclass(classOwlOntology, owlClass, entityClass)) {
			submission.setType(Type.ENTITY);
		} else {
			throw new Exception("Class IRI has to be a subclass of either quality or entity.");
		}
	}
	
	@Override
	public String getClassLabel(String classIRI) throws Exception {
		OWLClass owlClass = owlOntologyManager.getOWLDataFactory().getOWLClass(IRI.create(classIRI));
		String label = annotationsManager.get(collection, owlClass, labelProperty);
		return label;
	}
	
	public Collection getCollection() {
		return collection;
	}
	
	private void updateOwlOntologyIRI(File file, Ontology ontology, Collection collection) throws JDOMException, IOException {
		SAXBuilder sax = new SAXBuilder();
		Document doc = sax.build(file);
		Element root = doc.getRootElement();
		String etcNamespacePrefix = "http://www.etc-project.org/owl/ontologies/";
		
		Namespace xmlNamespace = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace");
		Namespace owlNamespace = Namespace.getNamespace("owl", "http://www.w3.org/2002/07/owl#");
		Namespace rdfNamespace = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		Namespace rdfsNamespace = Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		List<Namespace> toRemove = new LinkedList<Namespace>();
		for(Namespace namespace : root.getAdditionalNamespaces()) 
			if(namespace.getURI().startsWith(etcNamespacePrefix))
				toRemove.add(namespace);
		for(Namespace remove : toRemove)
			root.removeNamespaceDeclaration(remove);
		
		String newNamespaceUrl = etcNamespacePrefix + collection.getId() + "/" + 
				ontology.getAcronym();
		Namespace newNamespace = Namespace.getNamespace(newNamespaceUrl + "#");
		root.addNamespaceDeclaration(newNamespace);
		Attribute baseAttribute = root.getAttribute("base", xmlNamespace);
		if(baseAttribute != null)
			baseAttribute.setValue(newNamespaceUrl);
		Element ontologyElement = root.getChild("Ontology", owlNamespace);
		if(ontologyElement != null) {
			Attribute aboutAttribute = ontologyElement.getAttribute("about", rdfNamespace);
			if(aboutAttribute != null)
				aboutAttribute.setValue(newNamespaceUrl);
		}
		List<Element> classElements = root.getChildren("Class", owlNamespace);
		for(Element classElement : classElements) {
			Attribute aboutAttribute = classElement.getAttribute("about", rdfNamespace);
			if(aboutAttribute != null && aboutAttribute.getValue().startsWith(etcNamespacePrefix)) {
				Element labelElement = classElement.getChild("label", rdfsNamespace);
				if(labelElement != null) {
					String label = labelElement.getValue();
					aboutAttribute.setValue(newNamespaceUrl + "#" + label);
				}
			}
		}
		
		XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
		xout.output(doc, new FileOutputStream(file));
	}

}