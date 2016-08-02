package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface TextTreeNodeProperties extends PropertyAccess<TextTreeNode> {

	  @Path("id")
	  ModelKeyProvider<TextTreeNode> key();
	   
	  @Path("text")
	  LabelProvider<TextTreeNode> nameLabel();
	 
	  ValueProvider<TextTreeNode, String> text();
	
}