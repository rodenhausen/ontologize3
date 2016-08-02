package edu.arizona.biosemantics.oto2.ontologize2.server;

//import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.gwt.logging.server.RemoteLoggingServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;


public class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CollectionService.class).in(Scopes.SINGLETON);
		bind(RemoteLoggingServiceImpl.class).in(Scopes.SINGLETON);		
	}

}
