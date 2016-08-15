package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DragSource;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.DndDropEvent.DndDropHandler;
import com.sencha.gxt.dnd.core.client.GridDragSource.GridDragSourceMessages;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox.MessageBoxAppearance;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.AccordionLayoutAppearance;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.ReplaceRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Origin;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class PartsGrid extends MenuTermsGrid {

	private Images images = GWT.create(Images.class);
	private AccordionLayoutAppearance appearance = GWT.<AccordionLayoutAppearance> create(AccordionLayoutAppearance.class);
	
	public PartsGrid(EventBus eventBus) {
		super(eventBus, Type.PART_OF);
		
		GridDragSource<Row> dndSource = new GridDragSource<Row>(grid) {
			@Override
			protected void onDragStart(DndDragStartEvent event) {
				super.onDragStart(event);
				Element element = event.getDragStartEvent().getStartElement();
				int targetRowIndex = grid.getView().findRowIndex(element);
				int targetColIndex = grid.getView().findCellIndex(element, null);
				Row row = store.get(targetRowIndex);
				Vertex v = row.getLead();
				if(targetColIndex > 0) {
					v = row.getAttached().get(targetColIndex - 1).getDest();
				}
				
				OntologyGraph g = ModelController.getCollection().getGraph();
				List<Edge> inRelations = g.getInRelations(v, type);
				if(inRelations.size() > 1) {
					Alerter.showAlert("Moving", "Moving of term with more than one parent is not allowed"); // at this time
					event.setCancelled(true);
				}
				if(inRelations.size() == 1)
					event.setData(inRelations.get(0));
				else {
					Alerter.showAlert("Moving", "Cannot move the root");
					event.setCancelled(true);
				}
			}
		};
		dropTarget.setAllowSelfAsSource(true);
		dropTarget.addDropHandler(new DndDropHandler() {
			@Override
			public void onDrop(DndDropEvent event) {
				Element element = event.getDragEndEvent().getNativeEvent().getEventTarget().<Element> cast();
				int targetRowIndex = grid.getView().findRowIndex(element);
				int targetColIndex = grid.getView().findCellIndex(element, null);
				Row row = store.get(targetRowIndex);
				
				if(event.getData() instanceof Edge) {
					Edge r = (Edge)event.getData();
					fire(new ReplaceRelationEvent(r, row.getLead()));
				}
			}
		});
	}
	
	@Override
	public void fire(GwtEvent<? extends EventHandler> e) {
		if(e instanceof CreateRelationEvent) {
			final CreateRelationEvent createRelationEvent = (CreateRelationEvent)e;
			OntologyGraph g = ModelController.getCollection().getGraph();
			for(Edge r : createRelationEvent.getRelations()) {
				try {
					g.isValidSubclass(r);
					
					Vertex source = r.getSrc();
					Vertex dest = r.getDest();
					List<Edge> existingRelations = g.getInRelations(dest, type);
					if(!existingRelations.isEmpty()) {
						Vertex existSource = existingRelations.get(0).getSrc();				
						/*final MessageBox box = Alerter.showConfirm("Create Part", 
							"\"" + dest + "\" is already a part of \"" + existSource +  "\".</br></br></br>" + 
								"Do you agree to continue as follows: </br>" + 
								"1) Replace \"" + dest + "\" with \"" + existSource + " " + dest + "\" as part of \"" + existSource + "\"</br>" + 
								"2) Create \"" + source + " " + dest + "\" as part of \"" + source + "\"</br>" + 
								"3) Create \"" + existSource + " " + dest + "\" and \"" + source + " " + dest + "\" as subclass of \"" + dest + "\".</br></br>" +
								"If NO, please create a new term to avoid duplication of " + dest + " as a part of \"" + source + "\".");*/
						final Dialog box = new Dialog();
						box.setHeadingText("Create Part");
						box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO);
						VerticalLayoutContainer vlc = new VerticalLayoutContainer();
						AccordionLayoutContainer alc = new AccordionLayoutContainer();
						HorizontalLayoutContainer hlc = new HorizontalLayoutContainer();
						vlc.add(new HTML(SafeHtmlUtils.fromTrustedString("We cannot add the part <i>" + dest + "</i> as is to <i>" + 
								source + "</i>. It is already a part of <i>" +  existSource +  "</i>.")), new VerticalLayoutContainer.VerticalLayoutData(-1, -1, new Margins(0, 0, 10, 0)));
						
						String explanation = "If you apply the non-specific structure pattern we will do the following for you: </br>"
								+ "1) Replace <i>" + dest + "</i> with <i>" + existSource + " " + dest + "</i> as part of <i>" + existSource + "</i></br>" + 
								"2) Make <i>" + source + " " + dest + "</i> a part of <i>" + source + "</i></br>" + 
								"3) Make <i>" + existSource + " " + dest + "</i> and <i>" + source + " " + dest + "</i> a subclass of <i>" + dest + "</i>.";
						hlc.add(new HTML(SafeHtmlUtils.fromTrustedString(
								"Do you want to apply the <b>non-specific structure pattern</b>?")), new HorizontalLayoutData(-1, -1, new Margins(0, 5, 0, 0)));
						Image image = new Image(images.help());
						SimpleContainer sc = new SimpleContainer();
						sc.add(image);
						image.setSize("20px", "20px");
						sc.setToolTip((explanation));
						hlc.add(sc);
						vlc.add(hlc);
						/*vlc.add(alc);
						
						ContentPanel cp = new ContentPanel(appearance);
					    cp.setAnimCollapse(false);
					    cp.setHeadingText("We will do the following for you");
					    cp.add(new HTML("1) Replace <i>" + dest + "</i> with <i>" + existSource + " " + dest + "</i> as part of <i>" + existSource + "</i></br>" + 
								"2) Create <i>" + source + " " + dest + "</i> as part of <i>" + source + "</i></br>" + 
								"3) Create <i>" + existSource + " " + dest + "</i> and <i>" + source + " " + dest + "</i> as subclass of <i>" + dest + "</i>."));
					    alc.add(cp);*/
					    
						box.setWidth(500);
						box.setHeight(150);
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
					final MessageBox box = Alerter.showAlert("Create part", ex.getMessage());
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

	protected void createRelation(Edge r) {
		if(r.getType().equals(Type.PART_OF)) {
			OntologyGraph g = ModelController.getCollection().getGraph();
			Vertex dest = r.getDest();
			Vertex src = r.getSrc();
			String newValue = src + " " + dest;
			
			List<Edge> parentRelations = g.getInRelations(dest, Type.PART_OF);
			if(!parentRelations.isEmpty()) {			
				for(Edge parentRelation : parentRelations) {
					Vertex parentSrc = parentRelation.getSrc();
					Vertex disambiguatedDest = new Vertex(parentSrc + " " + dest);
					
					replace(parentSrc, dest, disambiguatedDest);
				}
				
				super.createRelation(new Edge(src, new Vertex(newValue), r.getType(), r.getOrigin()));
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
		
		for(Edge r : leadRowMap.get(newDest).getAttached()) {
			if(r.getDest().getValue().startsWith(dest.getValue())) {
				replace(dest, r.getDest(), new Vertex(newDest.getValue() + " " + r.getDest().getValue()));
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
