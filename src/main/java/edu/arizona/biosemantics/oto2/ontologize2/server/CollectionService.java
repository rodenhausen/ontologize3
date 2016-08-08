package edu.arizona.biosemantics.oto2.ontologize2.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.arizona.biosemantics.oto2.ontologize2.server.owl.OWLWriter;
import edu.arizona.biosemantics.oto2.ontologize2.shared.AddCandidateResult;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;
import edu.uci.ics.jung.graph.util.EdgeType;

public class CollectionService extends RemoteServiceServlet implements ICollectionService {

	private int currentCollectionId = 0;
	
	public CollectionService() {
		File file = new File(Configuration.collectionsDirectory);
		if(!file.exists())
			file.mkdirs();
		
		for(File collectionFile : file.listFiles()) {
			try {
				int id = Integer.parseInt(collectionFile.getName());
				if(id >= currentCollectionId)
					currentCollectionId = id + 1;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public synchronized Collection insert(Collection collection) throws Exception {
		collection.setId(currentCollectionId++);
		serializeCollection(collection);
		return collection;
	}

	private void serializeCollection(Collection collection) {
		File collectionDirectory = new File(Configuration.collectionsDirectory + File.separator + collection.getId());
		if(!collectionDirectory.exists())
			collectionDirectory.mkdir();
		
		try(ObjectOutputStream collectionOutput = new ObjectOutputStream(new FileOutputStream(
				Configuration.collectionsDirectory + File.separator + collection.getId() + File.separator + "collection.ser"))) {
			collectionOutput.writeObject(collection);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection get(int collectionId, String secret) throws Exception {
		try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(
				Configuration.collectionsDirectory + File.separator + collectionId + File.separator + "collection.ser"))) {
			Object object = is.readObject();
			if(object instanceof Collection) {
				Collection collection = (Collection)object;
				if(collection.getSecret().equals(secret))
					return collection;
			}
		}
		throw new Exception("Could not read collection");
	}

	@Override
	public void update(Collection collection) throws Exception {
		Collection storedCollection = this.get(collection.getId(), collection.getSecret());
		if(storedCollection.getSecret().equals(collection.getSecret())) {
			serializeCollection(collection);
		}
	}

	@Override
	public boolean add(int collectionId, String secret, Relation relation)	throws Exception {
		Collection collection = this.get(collectionId, secret);
		boolean result = collection.getGraph().addRelation(relation);
		update(collection);
		return result;
	}
	
	@Override
	public void remove(int collectionId, String secret, Relation relation) throws Exception {
		Collection collection = this.get(collectionId, secret);
		collection.getGraph().removeRelation(relation);
		update(collection);
	}

	@Override
	public AddCandidateResult add(int collectionId, String secret, List<Candidate> candidates) throws Exception {
		Collection collection = this.get(collectionId, secret);
		List<Candidate> successfully = new LinkedList<Candidate>();
		List<Candidate> unsuccessfully = new LinkedList<Candidate>();
		for(Candidate candidate : candidates) {
			if(!collection.contains(candidate.getText())) {
				successfully.add(candidate);
				collection.add(candidate);
			} else {
				unsuccessfully.add(candidate);
			}
		}
		
		AddCandidateResult result = new AddCandidateResult(successfully, unsuccessfully);
		update(collection);
		return result;
	}

	@Override
	public void remove(int collectionId, String secret, List<Candidate> candidates) throws Exception {
		Collection collection = this.get(collectionId, secret);
		for(Candidate candidate : candidates) 
			collection.getCandidates().remove(candidate.getText());
		update(collection);
	}

	@Override
	public String getOWL(int collectionId, String secret) throws Exception {
		Collection c = this.get(collectionId, secret);
		OWLWriter ow = new OWLWriter(c);
		return ow.write();
	}
}
