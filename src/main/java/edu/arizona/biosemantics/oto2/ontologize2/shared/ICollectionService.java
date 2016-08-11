package edu.arizona.biosemantics.oto2.ontologize2.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

@RemoteServiceRelativePath("ontologize2_collection")
public interface ICollectionService extends RemoteService {
	
	public Collection insert(Collection collection) throws Exception;;
	
	public Collection get(int id, String secret) throws Exception;
	
	public void update(Collection collection) throws Exception;
	
	public boolean add(int collectionId, String secret, Relation relation) throws Exception;
	
	public void remove(int collectionId, String secret, Relation relation) throws Exception;
		
	public AddCandidateResult add(int id, String secret, List<Candidate> candidates) throws Exception;
	
	public void remove(int id, String secret, List<Candidate> candidates) throws Exception;
	
	public String[][] getOWL(int id, String secret) throws Exception;
}
