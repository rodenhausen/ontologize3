package edu.arizona.biosemantics.oto2.ontologize2.client.candidate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.sencha.gxt.data.shared.IconProvider;

import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.CandidateTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNode;

public class TermTreeNodeIconProvider implements IconProvider<TextTreeNode> {

	private static TermStatusImages termStatusImages = GWT.create(TermStatusImages.class);

	public TermTreeNodeIconProvider() {
	}

	@Override
	public ImageResource getIcon(TextTreeNode node) {
		if(node instanceof CandidateTreeNode) {
			CandidateTreeNode candidateTreeNode = (CandidateTreeNode)node;
			/*if(ModelController.getCollection() != null && ModelController.getCollection().isUsed(termTreeNode.getTerm())) {
				return termStatusImages.green();
			} else if(ModelController.getCollection() != null && ModelController.getCollection().hasExistingIRI(termTreeNode.getTerm())) {
				return termStatusImages.yellow();
			} else if(termTreeNode.getTerm().isRemoved()) {
				return termStatusImages.gray();
			}*/
			return null;
		}
		return null;
	}

}
