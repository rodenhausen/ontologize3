package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;

public abstract class TextTreeNode implements Comparable<TextTreeNode>{

	public abstract String getText();
	
	public abstract String getId();
	
	@Override
	public int compareTo(TextTreeNode o) {
		return this.getText().compareTo(o.getText());
	}
	
}