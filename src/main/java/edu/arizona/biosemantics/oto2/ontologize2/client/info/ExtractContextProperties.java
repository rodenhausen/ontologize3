package edu.arizona.biosemantics.oto2.ontologize2.client.info;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.ExtractContext;

public interface ExtractContextProperties extends PropertyAccess<ExtractContext> {

	@Path("id")
	ModelKeyProvider<ExtractContext> key();

	@Path("source")
	LabelProvider<ExtractContext> nameLabel();

	ValueProvider<ExtractContext, String> source();

	ValueProvider<ExtractContext, String> text();
	
}