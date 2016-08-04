package edu.arizona.biosemantics.oto2.ontologize2.server;

import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;

import edu.arizona.biosemantics.common.context.shared.Context;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.IContextService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.ExtractContext;

public class ContextService extends RemoteServiceServlet implements IContextService {

	private ICollectionService collectionService;
	private ContextDAO contextDAO;

	@Inject
	public ContextService(ICollectionService collectionService, ContextDAO contextDAO) {
		this.collectionService = collectionService;
		this.contextDAO = contextDAO;
	}
	
	@Override
	public List<ExtractContext> getContexts(int collectionId, String secret, String term) throws Exception {
		Collection c = collectionService.get(collectionId, secret);
		return contextDAO.get(collectionId, secret, term);
	}
	
	@Override
	public List<Context> insert(int collectionId, String secret, List<Context> contexts) throws Exception {
		Collection c = collectionService.get(collectionId, secret);
		contextDAO.insert(collectionId, contexts);
		return contexts;
	}

}
