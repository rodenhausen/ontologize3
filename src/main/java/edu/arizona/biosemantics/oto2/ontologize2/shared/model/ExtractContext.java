package edu.arizona.biosemantics.oto2.ontologize2.shared.model;

import java.io.Serializable;

public class ExtractContext implements Serializable {
	
	private String id = "";
	private String source;
	private String text;
	private String fullText;
	
	public ExtractContext() { }
	
	public ExtractContext(String id, String source, String text, String fullText) { 
		this.id = id;
		this.source = source;
		this.text = text;
		this.fullText = fullText;
	}
	
	public String getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public String getText() {
		return text;
	}
	
	public String getFullText() {
		return fullText;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtractContext other = (ExtractContext) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}
}
