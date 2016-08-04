package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent.Handler;

public class SelectTermEvent extends GwtEvent<Handler> implements Serializable {

	public interface Handler extends EventHandler {
		void onSelect(SelectTermEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private String term;
    
	public SelectTermEvent(String term) { 
		this.term = term;
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onSelect(this);
	}

	public String getTerm() {
		return term;
	}
}
