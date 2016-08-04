package edu.arizona.biosemantics.oto2.ontologize2.shared;

import java.io.IOException;
import java.util.List;




import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.ExtractContext;
import edu.arizona.biosemantics.common.context.shared.Context;


/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("ontologize2_context")
public interface IContextService extends RemoteService {
		
	public List<ExtractContext> getContexts(int collectionId, String secret, String term) throws Exception;
	
	public List<Context> insert(int collectionId, String secret, List<Context> contexts) throws Exception;
	
}
