package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;

/**
 * Removes edge + destination node
 * if recursive: remove everything below destination node too
 * else: remove only edge + destination node. Add edges from source to all of destinations child nodes.
 *  * @author rodenhausen
 */
public class RemoveRelationEvent extends GwtEvent<Handler> implements Serializable {

	public interface Handler extends EventHandler {
		void onRemove(RemoveRelationEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Edge[] relations = new Edge[] { };
	private boolean isEffectiveInModel = false;
	private boolean recursive = true;
    
	public RemoveRelationEvent() { }
	
    public RemoveRelationEvent(boolean recursive, Edge... relations) { 
    	this.relations = relations;
    	this.recursive = recursive;
    }
    
	public RemoveRelationEvent(boolean recursive, List<Edge> relations) {
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

	public Edge[] getRelations() {
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
