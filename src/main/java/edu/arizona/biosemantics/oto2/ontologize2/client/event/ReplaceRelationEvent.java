package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.ReplaceRelationEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

/**
 * Only edge is removed and destination is reattached to another node
 * @author rodenhausen
 *
 */
public class ReplaceRelationEvent extends GwtEvent<Handler> implements Serializable {

	public interface Handler extends EventHandler {
		void onReplace(ReplaceRelationEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private boolean isEffectiveInModel = false;
	private Edge oldRelation;
	private Vertex newSource;
	
	private ReplaceRelationEvent() { }
	
    public ReplaceRelationEvent(Edge oldRelation, Vertex newSource) { 
    	this.oldRelation = oldRelation;
    	this.newSource = newSource;
    }

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onReplace(this);
	}

	public Edge getOldRelation() {
		return oldRelation;
	}

	public Vertex getNewSource() {
		return newSource;
	}

	public boolean isEffectiveInModel() {
		return isEffectiveInModel;
	}

	public void setEffectiveInModel(boolean isEffectiveInModel) {
		this.isEffectiveInModel = isEffectiveInModel;
	}
}
