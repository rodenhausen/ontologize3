package edu.arizona.biosemantics.oto2.ontologize2.server;

//import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;


public class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ICollectionService.class).to(CollectionService.class).in(Scopes.SINGLETON);
		bind(CollectionService.class).in(Scopes.SINGLETON);
		bind(ContextService.class).in(Scopes.SINGLETON);
		bind(RemoteLoggingServiceImpl.class).in(Scopes.SINGLETON);		
	}

}
