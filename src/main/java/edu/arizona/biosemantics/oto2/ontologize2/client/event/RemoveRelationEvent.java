package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class RemoveRelationEvent extends GwtEvent<Handler> implements Serializable {

	public interface Handler extends EventHandler {
		void onRemove(RemoveRelationEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Relation[] relations = new Relation[] { };
	private boolean isEffectiveInModel = false;
	private boolean recursive = true;
    
	private RemoveRelationEvent() { }
	
    public RemoveRelationEvent(boolean recursive, Relation... relations) { 
    	this.relations = relations;
    	this.recursive = recursive;
    }
    
	public RemoveRelationEvent(boolean recursive, List<Relation> relations) {
		this.relations = relations.toArray(this.relations);
		this.recursive = recursive;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onRemove(this);
	}

	public Relation[] getRelations() {
		return relations;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public boolean isEffectiveInModel() {
		return isEffectiveInModel;
	}

	public void setEffectiveInModel(boolean isEffectiveInModel) {
		this.isEffectiveInModel = isEffectiveInModel;
	}
	
}
