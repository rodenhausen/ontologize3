package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class ModuleCreator  {
	
	private OWLOntologyManager owlOntologyManager;
	private OWLOntologyRetriever owlOntologyRetriever;
	private AnnotationsManager annotationsManager;
	private StructuralReasonerFactory owlReasonerFactory;
	private ConsoleProgressMonitor progressMonitor;
	private SimpleConfiguration owlReasonerConfig;
	private OWLAnnotationProperty labelProperty;

	public ModuleCreator(OWLOntologyManager owlOntologyManager, OWLOntologyRetriever owlOntologyRetriever, 
			AnnotationsManager annotationsManager) {
		this.owlOntologyManager = owlOntologyManager;
		this.owlOntologyRetriever = owlOntologyRetriever;
		this.annotationsManager = annotationsManager;
		owlReasonerFactory = new StructuralReasonerFactory();
		progressMonitor = new ConsoleProgressMonitor();
		owlReasonerConfig = new SimpleConfiguration(progressMonitor);
		labelProperty = owlOntologyManager.getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	}
	
	public OWLOntology create(Collection collection, OWLClass owlClass, OWLOntology targetOntology, 
			String collectionOntologyDirectory) throws Exception {
		OWLOntology owlClassOntology = owlOntologyRetriever.getOWLOntology(collection, owlClass);
		Set<OWLEntity> seeds = new HashSet<OWLEntity>();
		seeds.add(owlClass);
		
		String label = annotationsManager.get(collection, owlClass, labelProperty);
		File moduleFile = new File(collectionOntologyDirectory, 
				"module." + (label == null ? "" : label + ".") + owlClass.getIRI().getShortForm() + ".owl");
		IRI moduleIRI = IRI.create(moduleFile);
		
		// remove the existing module -- in effect replace the old module with the new one.
		if (moduleFile.exists())
			moduleFile.delete();
		if(owlOntologyManager.getOntology(moduleIRI) != null)
			owlOntologyManager.removeOntology(owlOntologyManager.getOntology(moduleIRI));
			
		SyntacticLocalityModuleExtractor syntacticLocalityModuleExtractor = new SyntacticLocalityModuleExtractor(
				owlOntologyManager, owlClassOntology, ModuleType.STAR);
		OWLReasoner owlReasoner = owlReasonerFactory.createReasoner(targetOntology, owlReasonerConfig);
		OWLOntology moduleOntology = syntacticLocalityModuleExtractor.extractAsOntology(seeds, moduleIRI, -1, 0, owlReasoner); //take all superclass and no subclass into the seeds.
		owlOntologyManager.saveOntology(moduleOntology, moduleIRI);
		OWLImportsDeclaration importDeclaration = owlOntologyManager.getOWLDataFactory().getOWLImportsDeclaration(moduleIRI);
		owlOntologyManager.applyChange(new AddImport(targetOntology, importDeclaration));
		owlOntologyManager.loadOntology(moduleIRI);
		return moduleOntology;
	}
}