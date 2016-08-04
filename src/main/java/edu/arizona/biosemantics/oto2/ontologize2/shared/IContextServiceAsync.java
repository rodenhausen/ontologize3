package edu.arizona.biosemantics.oto2.ontologize2.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.arizona.biosemantics.bioportal.model.Ontology;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.ExtractContext;
import edu.arizona.biosemantics.common.context.shared.Context;

public interface IContextServiceAsync {	

	public void getContexts(int collectionid, String secret, String term, AsyncCallback<List<ExtractContext>> callback);
	
	public void insert(int collectionId, String secret, List<Context> contexts, AsyncCallback<List<Context>> callback);
		
}
