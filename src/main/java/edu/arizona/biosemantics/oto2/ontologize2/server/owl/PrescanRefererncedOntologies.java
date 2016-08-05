package edu.arizona.biosemantics.oto2.ontologize2.server.owl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class PrescanRefererncedOntologies {

	/**
	 * This code is specific to the syntax of bioontology.org hosted ontologies 
	 * @param args
	 * @throws OWLOntologyCreationException
	 * @throws IOException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File permanentOntologiesDir = new File(Configuration.permanentOntologyDirectory);
		
		Map<String, OWLOntology> availableOntologies = new HashMap<String, OWLOntology>();
		Set<String> unavailable = new HashSet<String>();
		for(File ontFile : permanentOntologiesDir.listFiles()) {
			if(ontFile.isFile()) {
				System.out.println(ontFile.getName());
				OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(ontFile);
				availableOntologies.put(getAcronym(owlOntology), owlOntology);
			}
		}
		
		for(String acronym : availableOntologies.keySet()) {
			OWLOntology owlOntology = availableOntologies.get(acronym);
			for(OWLClass clazz : owlOntology.getClassesInSignature()) {	
				if(!availableOntologies.containsKey(getAcronym(clazz)))
					unavailable.add(getAcronym(clazz));
			}
		}
		System.out.println(availableOntologies.keySet());
		System.out.println(unavailable);
	}

	private static String getAcronym(OWLClass clazz) {
		String iriUrl = clazz.getIRI().toString();
		try {
			String acronym = iriUrl.substring(iriUrl.lastIndexOf("/") + 1, iriUrl.lastIndexOf("_")).toLowerCase();
			return acronym;
		} catch(Exception e) {
			System.out.println("Exception "  + clazz.getIRI().toString());
			return "";
		}
	}

	private static String getAcronym(OWLOntology owlOntology) {
		String iriUrl = owlOntology.getOntologyID().getOntologyIRI().get().toString();
		try {
			String acronym = iriUrl.substring(iriUrl.lastIndexOf("/") + 1, iriUrl.length());
			acronym = acronym.substring(0, acronym.indexOf(".")).toLowerCase();
			return acronym;
		} catch(Exception e) {
			System.out.println("Exception "  + owlOntology.getOntologyID().getOntologyIRI().get().toString());
			return "";
		}
	}
	
}