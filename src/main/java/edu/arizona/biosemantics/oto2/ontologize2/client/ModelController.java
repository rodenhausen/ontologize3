package edu.arizona.biosemantics.oto2.ontologize2.client;

import java.util.Arrays;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.shared.AddCandidateResult;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class ModelController {

	private static Collection collection;
	private ICollectionServiceAsync collectionService = GWT.create(ICollectionService.class);
	private EventBus eventBus;

	public ModelController(EventBus eventBus) {
		this.eventBus = eventBus;
		bindEvents();
	}
	
	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				collection = event.getCollection();
				
			}
		});
		eventBus.addHandler(CreateRelationEvent.TYPE, new CreateRelationEvent.Handler() {
			@Override
			public void onCreate(CreateRelationEvent event) {
				if(!event.isEffectiveInModel()) {
					for(Relation relation : event.getRelations()) {
						collectionService.add(collection.getId(), collection.getSecret(), relation, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								Alerter.showAlert("Data out of sync", "The data became out of sync with the server. Please reload the window.");
							}
							@Override
							public void onSuccess(Boolean result) {	}
						});
						try {
							collection.getGraph().addRelation(relation);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					event.setEffectiveInModel(true);
					eventBus.fireEvent(event);
				}
			}
		});	
		eventBus.addHandler(RemoveRelationEvent.TYPE, new RemoveRelationEvent.Handler() {
			@Override
			public void onRemove(RemoveRelationEvent event) {
				if(!event.isEffectiveInModel()) {
					for(Relation relation : event.getRelations()) {
						collectionService.remove(collection.getId(), collection.getSecret(), relation, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
								Alerter.showAlert("Data out of sync", "The data became out of sync with the server. Please reload the window.");
							}
							@Override
							public void onSuccess(Void result) {	}
						});
						collection.getGraph().removeRelation(relation);
					}
					event.setEffectiveInModel(true);
					eventBus.fireEvent(event);
				}
			}
		});
		eventBus.addHandler(CreateCandidateEvent.TYPE, new CreateCandidateEvent.Handler() {
			@Override
			public void onCreate(CreateCandidateEvent event) {
				collectionService.add(collection.getId(), collection.getSecret(), Arrays.asList(event.getCandidates()), 
						new AsyncCallback<AddCandidateResult>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.showAlert("Data out of sync", "The data became out of sync with the server. Please reload the window.");
					}
					@Override
					public void onSuccess(AddCandidateResult result) {	}
				});
				for(Candidate candidate : event.getCandidates()) {
					collection.add(candidate);
				}
			}
		});
		eventBus.addHandler(RemoveCandidateEvent.TYPE, new RemoveCandidateEvent.Handler() {
			@Override
			public void onRemove(RemoveCandidateEvent event) {
				collectionService.remove(collection.getId(), collection.getSecret(), Arrays.asList(event.getCandidates()), 
						new AsyncCallback<Void>() {
					@Override
					public void onFailure(Throwable caught) {
						Alerter.showAlert("Data out of sync", "The data became out of sync with the server. Please reload the window.");
					}
					@Override
					public void onSuccess(Void result) {	}
				});
				for(Candidate candidate : event.getCandidates())
					collection.remove(candidate.getText());
			}
		});
	}
	
	public static Collection getCollection() {
		if(collection == null)
			return new Collection();
		return collection;
	}
}
