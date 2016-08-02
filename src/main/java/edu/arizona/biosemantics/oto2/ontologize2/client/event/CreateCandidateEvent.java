package edu.arizona.biosemantics.oto2.ontologize2.client.event;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateCandidateEvent.Handler;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;

public class CreateCandidateEvent extends GwtEvent<Handler> implements Serializable {

	public interface Handler extends EventHandler {
		void onCreate(CreateCandidateEvent event);
	}
	
    public static Type<Handler> TYPE = new Type<Handler>();
	private Candidate[] candidates = new Candidate[] { };
	
	private CreateCandidateEvent() { }

    public CreateCandidateEvent(Candidate... candidates) {
    	this.candidates = candidates;
    }
    
	public CreateCandidateEvent(List<Candidate> terms) {
		this.candidates = terms.toArray(this.candidates);
	}
	
	@Override
	public Type<Handler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(Handler handler) {
		handler.onCreate(this);
	}

	public Candidate[] getCandidates() {
		return candidates;
	}
}
