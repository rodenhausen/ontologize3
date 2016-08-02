package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;

import java.util.List;

import com.sencha.gxt.data.shared.TreeStore.TreeNode;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;

/**
 * Assumption: Terms are unique. However they can appear multiple times in a tree (e.g. superclass tree, term has multiple superclasses, duplicate term nodes necessary)
 * @author rodenhausen
 */
public class CandidateTreeNode extends TextTreeNode {

	private Candidate candidate;

	public CandidateTreeNode(Candidate candidate) {
		this.candidate = candidate;
	}
	
	@Override
	public String getText() {
		return candidate.getText();
	}

	public Candidate getCandidate() {
		return candidate;
	}
	
	@Override
	public String getId() {
		return "candidate-" + candidate.getPath() + "/" + candidate.getText();
	}
	
}