package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import edu.arizona.biosemantics.common.ontology.graph.Reader;
import edu.arizona.biosemantics.oto2.ontologize2.server.Configuration;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class OWLWriter {
	
	private OWLAnnotationProperty labelProperty;
	
	private Collection c;
	private OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	private OntologyReasoner or = new OntologyReasoner(om);
	private OWLOntologyRetriever oret;
	private OWLReasonerFactory orf;
	private AxiomManager axm = new AxiomManager(om);
	private OWLOntology o;
	private Map<Ontology, edu.arizona.biosemantics.common.ontology.graph.OntologyGraph> graphs = 
			new HashMap<Ontology, edu.arizona.biosemantics.common.ontology.graph.OntologyGraph>();
	private Map<Vertex, String> iriMap = new HashMap<Vertex, String>();
	private String outputDirectory;
	
	public OWLWriter(Collection c) throws Exception {
		this.c = c;
		this.outputDirectory = Configuration.collectionOntologyDirectory + File.separator + c.getId();
		this.o = om.createOntology(IRI.create(Configuration.etcOntologyBaseIRI + c.getId()));
		this.orf = new StructuralReasonerFactory();
		this.oret = new OWLOntologyRetriever(om, c);
		
		this.labelProperty =
				om.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		
		List<Ontology> relevantOntologies = Ontology.getRelevantOntologies(c.getTaxonGroup());
		for(Ontology ro : relevantOntologies) {
			om.loadOntologyFromOntologyDocument(new File(Configuration.permanentOntologyDirectory + File.separator + ro.getName() + ".owl"));
			Reader reader = new Reader(Configuration.ontologyGraphs + File.separator + ro.getName() + ".graph");
			edu.arizona.biosemantics.common.ontology.graph.OntologyGraph g = reader.read();
			graphs.put(ro, g);
		}
	}
	
	public void write() throws Exception {
		OntologyGraph g = c.getGraph();
		addDefaultImportOntologies();
		axm.addDefaultAxioms(o);
		
		for(Vertex v : g.getVertices()) { 
			Map<Ontology, Set<edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex>> matches = new 
					HashMap<Ontology, Set<edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex>>();
			int matchCount = 0;
			for(Ontology ro : Ontology.getRelevantOntologies(c.getTaxonGroup())) {
				edu.arizona.biosemantics.common.ontology.graph.OntologyGraph rg = graphs.get(ro);
				Set<edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex> oMatches = rg.getVerticesByName(v.getValue());
				Set<edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex> oMatchesWithIri = 
						new HashSet<edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex>();
				for(edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex match : oMatches) {
					if(match.hasIri())
						oMatchesWithIri.add(match);
				}
				if(!oMatchesWithIri.isEmpty()) {
					matches.put(ro, oMatchesWithIri);
					matchCount += oMatchesWithIri.size();
				}
			}
			if(matchCount == 1) {
				for(Ontology ro : matches.keySet()) {
					edu.arizona.biosemantics.common.ontology.graph.OntologyGraph.Vertex match = matches.get(ro).iterator().next();
					iriMap.put(v, match.getIri());
					break;
				}
			} else if(matchCount > 1 || matchCount == 0) {
				iriMap.put(v, Configuration.etcOntologyBaseIRI + c.getId() + "#" + v.getValue());
			}
		}
		
		for(Vertex v : g.getVertices()) {
			System.out.println("Processing " + v.getValue());
			OWLClass oc = om.getOWLDataFactory().getOWLClass(IRI.create(iriMap.get(v)));
			if(!iriMap.get(v).startsWith(Configuration.etcOntologyBaseIRI)) {
				System.out.println("Create module");
				createModule(oc);
			}
			axm.addDeclaration(o, oc);
			axm.addLabel(o, oc, om.getOWLDataFactory().getOWLLiteral(v.getValue(), "en"));
			axm.addCreationDate(o, oc);
		}
		
		for(Vertex v : g.getVertices()) {
			System.out.println("create " + v.getValue());
			OWLClass oc = om.getOWLDataFactory().getOWLClass(IRI.create(iriMap.get(v)));
			
			List<Edge> inRs = g.getInRelations(v, Type.SUBCLASS_OF);
			for(Edge r : inRs) {
				String sourceIri = iriMap.get(r.getSrc());
				OWLClass sc = om.getOWLDataFactory().getOWLClass(IRI.create(sourceIri));
				axm.addSuperclass(o, oc, sc);
			}
			inRs = g.getInRelations(v, Type.PART_OF);
			for(Edge r : inRs) {
				String sourceIri = iriMap.get(r.getSrc());
				OWLClass poc = om.getOWLDataFactory().getOWLClass(IRI.create(sourceIri));
				axm.addPartOf(o, oc, poc);
			}
			inRs = g.getInRelations(v, Type.SYNONYM_OF);
			for(Edge r : inRs) {
				String sourceIri = iriMap.get(r.getSrc());
				OWLClass prefc = om.getOWLDataFactory().getOWLClass(IRI.create(sourceIri));
				axm.addSynonym(o, v.getValue(), prefc);
			}
		}
		or.checkConsistency(o);
		//ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//om.saveOntology(o, baos);
		//return baos.toString("UTF-8");
		try(FileOutputStream fos = new FileOutputStream(outputDirectory + File.separator + c.getId() + ".owl")) {
			om.saveOntology(o, fos);
		}
	}
	
	private OWLOntology createModule(OWLClass oc) throws Exception {
		OWLOntology oco = oret.getOWLOntology(oc);
		Set<OWLEntity> seeds = new HashSet<OWLEntity>();
		seeds.add(oc);
		
		String label = getAnnotation(oco, oc, labelProperty);
		File moduleFile = new File(outputDirectory, 
				"module." + (label == null ? "" : label + ".") + oc.getIRI().getShortForm() + ".owl");
		IRI moduleIRI = IRI.create(moduleFile);
		
		// remove the existing module -- in effect replace the old module with the new one.
		if (moduleFile.exists())
			moduleFile.delete();
		if(om.getOntology(moduleIRI) != null)
			om.removeOntology(om.getOntology(moduleIRI));
			
		SyntacticLocalityModuleExtractor syntacticLocalityModuleExtractor = 
				new SyntacticLocalityModuleExtractor(om, oco, ModuleType.STAR);
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		SimpleConfiguration orc = new SimpleConfiguration(progressMonitor);
		OWLReasoner owlReasoner = orf.createReasoner(o, orc);
		OWLOntology moduleOntology = syntacticLocalityModuleExtractor.extractAsOntology(seeds, moduleIRI, -1, 0, owlReasoner); //take all superclass and no subclass into the seeds.
		om.saveOntology(moduleOntology, moduleIRI);
		OWLImportsDeclaration importDeclaration = om.getOWLDataFactory().getOWLImportsDeclaration(moduleIRI);
		om.applyChange(new AddImport(o, importDeclaration));
		om.loadOntology(moduleIRI);
		return moduleOntology;
	}
	
	private String getAnnotation(OWLOntology owlOntology, OWLClass owlClass, OWLAnnotationProperty annotationProperty) throws Exception {
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

	private void addDefaultImportOntologies() {
		List<Ontology> relevantOntologies = Ontology.getRelevantOntologies(c.getTaxonGroup());
		for(Ontology relevantOntology : relevantOntologies) {
			//only import RO per default at this time
			if(o.equals(Ontology.RO)) {
				addImportDeclaration(relevantOntology);
			}
			//if(!relevantOntology.hasCollectionId()) {
			//	addImportDeclaration(owlOntology, relevantOntology);
			//}
		}
	}

	private void addImportDeclaration(Ontology relevantOntology) {
		IRI relevantIRI = IRI.create(relevantOntology.getIri());
		OWLImportsDeclaration importDeclaraton = om.getOWLDataFactory().getOWLImportsDeclaration(relevantIRI);
		om.applyChange(new AddImport(o, importDeclaraton));	
	}

}
