package edu.arizona.biosemantics.oto2.ontologize2.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public interface ICollectionServiceAsync {	
	
	public void insert(Collection collection, AsyncCallback<Collection> callback);
	
	public void get(int id, String secret, AsyncCallback<Collection> callback);
	
	public void update(Collection collection, AsyncCallback<Void> callback);
	
	public void add(int collectionId, String secret, Edge relation, AsyncCallback<Boolean> callback);
	
	public void remove(int collectionId, String secret, Edge relation, boolean recursive, AsyncCallback<Void> callback);
	
	public void replace(int collectionId, String secret, Edge oldRelation, Vertex newSource, AsyncCallback<Void> callback);

	public void add(int id, String secret, List<Candidate> candidates, 
			AsyncCallback<AddCandidateResult> callback);
	
	public void remove(int id, String secret, List<Candidate> candidates, AsyncCallback<Void> callback);

	public void getOWL(int id, String secret, AsyncCallback<String[][]> asyncCallback);
		
}
