package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox.MessageBoxAppearance;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.AccordionLayoutAppearance;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class PartsGrid extends MenuTermsGrid {

	private AccordionLayoutAppearance appearance = GWT.<AccordionLayoutAppearance> create(AccordionLayoutAppearance.class);
	
	public PartsGrid(EventBus eventBus) {
		super(eventBus, Type.PART_OF);
	}
	
	@Override
	public void fire(GwtEvent<? extends EventHandler> e) {
		if(e instanceof CreateRelationEvent) {
			final CreateRelationEvent createRelationEvent = (CreateRelationEvent)e;
			OntologyGraph g = ModelController.getCollection().getGraph();
			for(Relation r : createRelationEvent.getRelations()) {
				try {
					g.isValidSubclass(r);
					
					Vertex source = r.getSource();
					Vertex dest = r.getDestination();
					List<Relation> existingRelations = g.getInRelations(dest, type);
					if(!existingRelations.isEmpty()) {
						Vertex existSource = existingRelations.get(0).getSource();				
						/*final MessageBox box = Alerter.showConfirm("Create Part", 
							"\"" + dest + "\" is already a part of \"" + existSource +  "\".</br></br></br>" + 
								"Do you agree to continue as follows: </br>" + 
								"1) Replace \"" + dest + "\" with \"" + existSource + " " + dest + "\" as part of \"" + existSource + "\"</br>" + 
								"2) Create \"" + source + " " + dest + "\" as part of \"" + source + "\"</br>" + 
								"3) Create \"" + existSource + " " + dest + "\" and \"" + source + " " + dest + "\" as subclass of \"" + dest + "\".</br></br>" +
								"If NO, please create a new term to avoid duplication of " + dest + " as a part of \"" + source + "\".");*/
						final Dialog box = new Dialog();
						box.setHeadingText("Create Part");
						box.setTitle("Create Part");
						box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
						VerticalLayoutContainer vlc = new VerticalLayoutContainer();
						AccordionLayoutContainer alc = new AccordionLayoutContainer();
						vlc.add(new HTML(SafeHtmlUtils.fromTrustedString("We cannot add the part <i>" + dest + "</i> as is to <i>" + 
								source + "</i>. It is already a part of <i>" +  existSource +  "</i>.</br></br>" + 
									"Do you want to apply the <b>non-specific structure pattern</b>?")));
						vlc.add(alc);
						
						ContentPanel cp = new ContentPanel(appearance);
					    cp.setAnimCollapse(false);
					    cp.setHeadingText("We will do the following for you");
					    cp.add(new HTML("1) Replace <i>" + dest + "</i> with <i>" + existSource + " " + dest + "</i> as part of <i>" + existSource + "</i></br>" + 
								"2) Create <i>" + source + " " + dest + "</i> as part of <i>" + source + "</i></br>" + 
								"3) Create <i>" + existSource + " " + dest + "</i> and <i>" + source + " " + dest + "</i> as subclass of <i>" + dest + "</i>."));
					    alc.add(cp);
					    
						box.setWidget(vlc);
						
						
//						final MessageBox box = Alerter.showConfirm("Create Part", 
//								"We cannot add the part <i>" + dest + "</i> as is to <i>" + source + "</i>. It is already a part of <i>" + 
//										existSource +  "</i>.</br></br>" + 
//									"Do you want to apply the <b>non-specific structure pattern</b>?");
						TextButton yesButton = box.getButton(PredefinedButton.YES);
						yesButton.setText("Apply");
						TextButton noButton = box.getButton(PredefinedButton.NO);
						noButton.setText("Do Nothing");
						yesButton.addSelectHandler(new SelectHandler() {
							@Override
							public void onSelect(SelectEvent event) {
								eventBus.fireEvent(createRelationEvent);
								box.hide();
							}
						});
						noButton.addSelectHandler(new SelectHandler() {
							@Override
							public void onSelect(SelectEvent event) {
								box.hide();
							}
						});
						box.show();
					} else {
						eventBus.fireEvent(createRelationEvent);
					}
				} catch(Exception ex) {
					final MessageBox box = Alerter.showAlert("Create subclass", ex.getMessage());
					box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
						@Override
						public void onSelect(SelectEvent event) {
							box.hide();
						}
					});
				}
			}
		} else if(e instanceof RemoveRelationEvent) {
			eventBus.fireEvent(e);
		} else {
			eventBus.fireEvent(e);
		}
	}	

	protected void createRelation(Relation r) {
		if(r.getEdge().getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDestination();
			Vertex src = r.getSource();
			String newValue = src + " " + dest;
			
			List<Relation> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				for(Relation parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSource();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					replace(parentSrc, dest, disambiguatedDest);
				}
				
				super.createRelation(new Relation(src, new Vertex(newValue), r.getEdge()));
			} else {
				super.createRelation(r);
			}
		}
	}

	private void replace(Vertex src, Vertex dest, Vertex newDest) {
		leadRowMap.get(src).replaceAttachedDest(dest, newDest);
		updateRow(leadRowMap.get(src));
		
		leadRowMap.get(dest).setLead(newDest);
		leadRowMap.put(newDest, leadRowMap.get(dest));
		leadRowMap.remove(dest);
		updateRow(leadRowMap.get(newDest));
		
		for(Relation r : leadRowMap.get(newDest).getAttached()) {
			if(r.getDestination().getValue().startsWith(dest.getValue())) {
				replace(dest, r.getDestination(), new Vertex(newDest.getValue() + " " + r.getDestination().getValue()));
			}
		}
	}
	
	@Override
	protected SimpleContainer createCreateRowContainer() {
		return null;
	}
	
	@Override
	protected String getDefaultImportText() {
		return "parent, part 1, part 2, ...[e.g. flower, calyx, corolla]"; 
	}
}
