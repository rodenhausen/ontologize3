package edu.arizona.biosemantics.oto2.ontologize2.client.tree.node;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class VertexCell extends AbstractCell<Vertex> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div qtip=\"{2}\"" +
				"style=\"background-color:{1};\"" +
				">{0}</div>")
		SafeHtml cell(String value, String background, String quickTipText);
	}

	protected static Templates templates = GWT.create(Templates.class);
	private Type type;
	
	public VertexCell(Type type) {
		this.type = type;
	}

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, Vertex value, SafeHtmlBuilder sb) {
		String background = "";
		OntologyGraph g = ModelController.getCollection().getGraph();
		List<Relation> inRelations = g.getInRelations(value, type);
		if(inRelations.size() > 1)
			background = "#dddddd";
		SafeHtml rendered = templates.cell(value.getValue(), background, "");
		sb.append(rendered);
	}
}