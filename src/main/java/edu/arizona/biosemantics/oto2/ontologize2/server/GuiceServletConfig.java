package edu.arizona.biosemantics.oto2.ontologize2.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class GuiceServletConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {
			/* http://www.gwtproject.org/doc/latest/DevGuideServerCommunication.html#DevGuideImplementingServices 
				-> Common pitfalls: for url-pattern help */
			@Override
			protected void configureServlets() {
				serve("/ontologize2/ontologize2_collection").with(CollectionService.class);
				serve("/ontologize2/ontologize2_context").with(ContextService.class);
			}
			
		}, new GuiceModule());
	}
}