package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.arizona.biosemantics.oto2.ontologize2.server.Configuration;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class OWLWriter {
	
	private Collection c;
	private OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	private OntologyReasoner or = new OntologyReasoner(om);
	//private OWLOntologyRetriever oret = new OWLOntologyRetriever(om, null);
	//private AnnotationsManager am = new AnnotationsManager(oret);
	//private ModuleCreator mc = new ModuleCreator(om, oret, am);
	private AxiomManager axm = new AxiomManager(om);
	private OWLOntology o;
	
	public OWLWriter(Collection c) throws OWLOntologyCreationException {
		this.c = c;
		this.o = om.createOntology(IRI.create(Configuration.etcOntologyBaseIRI + c.getId()));
	}
	
	public String write() throws Exception {
		OntologyGraph g = c.getGraph();
		addDefaultImportOntologies();
		axm.addDefaultAxioms(o);
		
		for(Vertex v : g.getVertices()) {
			IRITerm it = new IRITerm();
			it.term = v.getValue();
			OWLClass oc = om.getOWLDataFactory().getOWLClass(IRI.create(Configuration.etcOntologyBaseIRI + 
					c.getId() + "#" + it.term));
			axm.addDeclaration(o, oc);
			axm.addLabel(o, oc, om.getOWLDataFactory().getOWLLiteral(
					it.term, "en"));
			axm.addCreationDate(o, oc);
		}
		
		for(Vertex v : g.getVertices()) {
			OWLClass oc = om.getOWLDataFactory().getOWLClass(IRI.create(Configuration.etcOntologyBaseIRI + 
					c.getId() + "#" + v.getValue()));
			
			List<Relation> inRs = g.getInRelations(v, Type.SUBCLASS_OF);
			for(Relation r : inRs) {
				OWLClass sc = om.getOWLDataFactory().getOWLClass(IRI.create(Configuration.etcOntologyBaseIRI + 
					c.getId() + "#" + r.getSource().getValue()));
				axm.addSuperclass(o, oc, sc);
			}
			inRs = g.getInRelations(v, Type.PART_OF);
			for(Relation r : inRs) {
				OWLClass poc = om.getOWLDataFactory().getOWLClass(IRI.create(Configuration.etcOntologyBaseIRI + 
					c.getId() + "#" + r.getSource().getValue()));
				axm.addPartOf(o, oc, poc);
			}
			inRs = g.getInRelations(v, Type.SYNONYM_OF);
			for(Relation r : inRs) {
				OWLClass prefc = om.getOWLDataFactory().getOWLClass(IRI.create(Configuration.etcOntologyBaseIRI + 
						c.getId() + "#" + r.getSource().getValue()));
				axm.addSynonym(o, v.getValue(), prefc);
			}
		}
		or.checkConsistency(o);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		om.saveOntology(o, baos);
		return baos.toString("UTF-8");
	}
	
	private String getIRI(String value) {
		//TODO: Scan graph for name match to place IRI;
		return null;
	}

	
	/*
	private OWLClass createOwlClass(IRITerm it) throws Exception {
		if(it.hasIRI()) {
			return createForeignClassModule(it);
		} else {
			it.iri = "";//TODO Create local iri here;
			return om.getOWLDataFactory().getOWLClass(IRI.create(it.iri));
		}
	}
	
	private OWLClass createForeignClassModule(IRITerm it) throws Exception {
		OWLClass newOwlClass = om.getOWLDataFactory().getOWLClass(IRI.create(it.iri));
		OWLOntology targetOwlOntology = o;
		String directory = Configuration.collectionOntologyDirectory + 
				File.separator + collection.getId();
		OWLOntology moduleOwlOntology = mc.create(collection, newOwlClass, o, directory);
		/*if (submission.getType().equals(Type.ENTITY)) {
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
		}*/
	/*	return newOwlClass;
	}
	
	private void insertClass(IRITerm it) {
		OWLClass owlClass = createOwlClass(it);
		
		boolean foreignClass = it.hasIRI();
		if(!foreignClass) {
			axm.addDeclaration(o, owlClass);
			axm.addDefinition(o, owlClass, it.definition);
			axm.addLabel(o, owlClass, om.getOWLDataFactory().getOWLLiteral(
					it.term, "en"));
			axm.addCreatedBy(o, owlClass);
			axm.addCreationDate(o, owlClass);
			axm.addSourceSampleComment(o, it, owlClass);
		}
		
		addSuperclasses(it, owlClass);
		axm.addSynonyms(o, owlClass, it.synonyms);
		axm.addPartOfs(collection, o, submission.getOntology(), owlClass, it.partOfs);
		or.checkConsistency(o);
		om.saveOntology(o, getLocalOntologyIRI(submission.getOntology()));
		return owlClass.getIRI().toString();
	}
	
	private void addSuperclasses(IRITerm it, OWLClass owlClass) throws Exception {
		boolean foreignClass = submission.hasClassIRI() ? !isEtcOntologyIRI(submission.getClassIRI()) : false;
		OWLOntology owlOntology = owlOntologyManager.getOntology(createOntologyIRI(submission));
		
		if(submission.hasSuperclasses() && !foreignClass){
			checkConsistentSuperclassHierarchyWithQualityEntity(submission, owlOntology);
			
			List<Superclass> superclasses = new LinkedList<Superclass>(submission.getSuperclasses());			
			axiomManager.addSuperclasses(collection, owlOntology, submission.getOntology(), owlClass, 
					superclasses, submission.getType());
		}
	}*/

	private void addDefaultImportOntologies() {
		List<String> relevantOntologies = getRelevantOntologiesForCollection(c);
		for(String relevantOntology : relevantOntologies) {
			//only import RO per default at this time
			if(relevantOntology.equals("http://purl.bioontology.org/obo/OBOREL")) {
				addImportDeclaration(relevantOntology);
			}
			//if(!relevantOntology.hasCollectionId()) {
			//	addImportDeclaration(owlOntology, relevantOntology);
			//}
		}
	}

	private void addImportDeclaration(String relevantOntology) {
		IRI relevantIRI = IRI.create(relevantOntology);
		OWLImportsDeclaration importDeclaraton = om.getOWLDataFactory().getOWLImportsDeclaration(relevantIRI);
		om.applyChange(new AddImport(o, importDeclaraton));	
	}

	private List<String> getRelevantOntologiesForCollection(Collection collection) {
		List<String> result = new LinkedList<String>();
		/*result.add("http://purl.obolibrary.org/obo/pato.owl");
		result.add("http://purl.obolibrary.org/obo/ro.owl");
		result.add("http://purl.obolibrary.org/obo/bspo.owl");
		result.add("http://purl.obolibrary.org/obo/hao.owl");
		result.add("http://purl.obolibrary.org/obo/po.owl");
		result.add("http://purl.obolibrary.org/obo/poro.owl");
		result.add("http://purl.obolibrary.org/obo/uberon.owl");
		result.add("http://purl.obolibrary.org/obo/caro/src/caro.obo.owl");
		result.add("http://purl.obolibrary.org/obo/cl.owl");
		result.add("http://purl.obolibrary.org/obo/envo.owl");
		result.add("http://purl.obolibrary.org/obo/go.owl");
		result.add("http://purl.obolibrary.org/obo/chebi.owl");*/
		
		result.add("http://purl.obolibrary.org/obo/pato.owl");
		result.add("http://purl.obolibrary.org/obo/ro.owl");
		result.add("http://purl.obolibrary.org/obo/bspo.owl");
		switch(collection.getTaxonGroup()) {
		case ALGAE:
			break;
		case CNIDARIA:
			break;
		case FOSSIL:
			break;
		case GASTROPODS:
			break;
		case HYMENOPTERA:
			result.add("http://purl.obolibrary.org/obo/hao.owl");
			break;
		case PLANT:
			result.add("http://purl.obolibrary.org/obo/po.owl");
			break;
		case PORIFERA:
			result.add("http://purl.obolibrary.org/obo/poro.owl");
			break;
		case SPIDER:
			break;
		default:
			break;
		}
		return result;
	}

}
