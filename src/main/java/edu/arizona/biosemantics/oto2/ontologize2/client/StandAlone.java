package edu.arizona.biosemantics.oto2.ontologize2.client;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.google.gwt.event.shared.EventBus;

import edu.arizona.biosemantics.common.biology.TaxonGroup;
import edu.arizona.biosemantics.common.context.shared.Context;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.IContextService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.IContextServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class StandAlone implements EntryPoint {

	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	private IContextServiceAsync contextService = GWT.create(IContextService.class);
	
	@Override
	public void onModuleLoad() {
		/*List<String> terms = new LinkedList<String>();
		//terms.add("stem");
//		terms.add("leaf");
//		terms.add("leaflet");
//		terms.add("stem");
//		terms.add("fruit");
//		terms.add("flower leaf");
//		terms.add("organ");
//		terms.add("structure");
//		*/
		final Collection c = new Collection("name", TaxonGroup.PLANT, "secret");
		c.add(Arrays.asList(new Candidate("leaf", "/a"), new Candidate("stem", ""), 
				new Candidate("b", ""), new Candidate("c", ""), new Candidate("h", ""), new Candidate("k", ""), 
				new Candidate("f", ""), new Candidate("d", ""), new Candidate("i", ""), new Candidate("l", ""),
				new Candidate("g", ""), new Candidate("e", ""), new Candidate("j", ""), new Candidate("m", "")));
		final Vertex v1 = new Vertex("leaf");
		final Vertex v2 = new Vertex("b");
		try {
			c.getGraph().addRelation(new Relation(c.getGraph().getRoot(Type.PART_OF), v1, new Edge(Type.PART_OF, Source.USER)));
			c.getGraph().addRelation(new Relation(v1, v2, new Edge(Type.PART_OF, Source.IMPORT)));
			
			
			collectionService.insert(c, new AsyncCallback<Collection>() {
				@Override
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
				}
				@Override
				public void onSuccess(final Collection c) {
					System.out.println("success");
					
					List<Context> contexts = new LinkedList<Context>();
					contexts.add(new Context(0, "s1", "a i dont like this"));
					contexts.add(new Context(0, "s2", "a tree with a big stem"));
					contexts.add(new Context(0, "s3", "flowers are green and yellow"));
					contextService.insert(0, "secret", contexts, new AsyncCallback<List<Context>>() {
						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}
						@Override
						public void onSuccess(List<Context> result) {
							System.out.println("success");
						}
					});
					
					/*collectionService.add(c.getId(), c.getSecret(), new Relation(v2, v1, new Edge(Type.SUBCLASS_OF, Source.USER)), 
							new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									
								}
								@Override
								public void onSuccess(Boolean result) {
									System.out.println(result);
								}
					}); */

				}
			});
			
			
			
			Ontologize ontologize = new Ontologize();
			Viewport v = new Viewport();
			v.add(ontologize);
			RootPanel.get().add(v);
			
			final EventBus eventBus = ontologize.getEventBus();		
			Timer timer = new Timer() {
				@Override
				public void run() {
					collectionService.get(0, "secret", new AsyncCallback<Collection>() {
						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}
						@Override
						public void onSuccess(Collection result) {
							eventBus.fireEvent(new LoadCollectionEvent(result));
						}
						
					});
				}
			};
			timer.schedule(1000);

		} catch(Exception e) {
			e.printStackTrace();
		}
			
		
	}

}
