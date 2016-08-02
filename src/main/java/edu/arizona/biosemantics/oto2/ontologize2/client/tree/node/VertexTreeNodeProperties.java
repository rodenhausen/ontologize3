package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public interface VertexTreeNodeProperties extends PropertyAccess<TextTreeNode> {

	  @Path("id")
	  ModelKeyProvider<VertexTreeNode> key();
	   
	  @Path("text")
	  LabelProvider<VertexTreeNode> nameLabel();
	 
	  ValueProvider<VertexTreeNode, String> text();
	  
	  ValueProvider<VertexTreeNode, Vertex> vertex();
	
}