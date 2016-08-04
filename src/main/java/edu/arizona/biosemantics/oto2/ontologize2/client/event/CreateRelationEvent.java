package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class CreateRelationEvent extends GwtEvent<Handler> implements Serializable {

	public interface Handler extends EventHandler {
		void onCreate(CreateRelationEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Relation[] relations = new Relation[] { };
	private boolean isEffectiveInModel = false;
	
	private CreateRelationEvent() { }
	
    public CreateRelationEvent(Relation... relations) { 
    	this.relations = relations;
    }
    
	public CreateRelationEvent(List<Relation> relations) {
		this.relations = relations.toArray(this.relations);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		System.out.println("dispatch to " + handler + " " + this.getRelations());
		handler.onCreate(this);
	}

	public Relation[] getRelations() {
		return relations;
	}

	public boolean isEffectiveInModel() {
		return isEffectiveInModel;
	}

	public void setEffectiveInModel(boolean isEffectiveInModel) {
		this.isEffectiveInModel = isEffectiveInModel;
	}
}
