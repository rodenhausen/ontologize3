package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.core.client.util.DelayedTask;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.Store.StoreFilter;
import com.sencha.gxt.messages.client.DefaultMessages;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MultiLinePromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.BeforeHideEvent.BeforeHideHandler;
import com.sencha.gxt.widget.core.client.event.CheckChangeEvent.CheckChangeHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.filters.AbstractGridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.menu.CheckMenuItem;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.common.TextAreaMessageBox;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Source;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;

public class MenuTermsGrid extends TermsGrid {
	
	protected ToolBar buttonBar;
	private TextField filterField;
	private CheckMenuItem checkFilterItem;
	private DelayedTask updateTask = new DelayedTask() {		
		@Override
		public void onExecute() {
			if(filterField.getText() == null)
				setFilter(false);
			else
				setFilter(!filterField.getText().isEmpty());
		}
	};
	private VerticalLayoutContainer vlc;
	private SimpleContainer simpleContainer;
	
	public MenuTermsGrid(final EventBus eventBus, final Type type) {
		super(eventBus, type);
		
		buttonBar = new ToolBar();
		
		TextButton filterButton = new TextButton("Filter");
		final Menu menu = new Menu();
		checkFilterItem = new CheckMenuItem(DefaultMessages.getMessages().gridFilters_filterText());
		menu.add(checkFilterItem);
		
		final Menu filterMenu = new Menu();
		filterField = new TextField() {
			protected void onKeyUp(Event event) {
				super.onKeyUp(event);
				int key = event.getKeyCode();
				if (key == KeyCodes.KEY_ENTER) {
					event.stopPropagation();
					event.preventDefault();
					filterMenu.hide(true);
						setFilter(!filterField.getText().isEmpty());
				}
				updateTask.delay(500);
			}
		};
		filterMenu.add(filterField);
		checkFilterItem.setSubMenu(filterMenu);
		checkFilterItem.addCheckChangeHandler(new CheckChangeHandler<CheckMenuItem>() {
	        @Override
	        public void onCheckChange(CheckChangeEvent<CheckMenuItem> event) {
	        	if(event.getItem().isChecked())
	        		setFilter(true);
	        	else
	        		setFilter(false);
	        }
	    });
		filterButton.setMenu(menu);
		buttonBar.add(filterButton);
		
		
		TextButton importButton = new TextButton("Import");
		importButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final TextAreaMessageBox box = new TextAreaMessageBox("Import " + type.getDisplayLabel(), "");
				box.getTextArea().setEmptyText(getDefaultImportText());
				/*box.setResizable(true);
				box.setResize(true);
				box.setMaximizable(true);*/
				box.setModal(true);
				box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						Vertex root = new Vertex(type.getRootLabel());
						String input = box.getValue();
						String[] lines = input.split("\\n");						
						for(String line : lines) {
							String[] terms = line.split(",");
							Set<Vertex> alreadyAttached = new HashSet<Vertex>(leadRowMap.keySet());
							
							if(!terms[0].trim().isEmpty()) {
								Vertex source = new Vertex(terms[0]);
								if(!alreadyAttached.contains(source)) {
									eventBus.fireEvent(new CreateRelationEvent(new Relation(root, source, new Edge(type, Source.USER))));
									alreadyAttached.add(source);
								}
								for(int i=1; i<terms.length; i++) {
									if(terms[i].trim().isEmpty()) 
										continue;
									Vertex target = new Vertex(terms[i]);
									eventBus.fireEvent(new CreateRelationEvent(new Relation(source, target, new Edge(type, Source.USER))));
									alreadyAttached.add(target);
								}
							} else {
								Alerter.showAlert("Import", "Malformed input");
							}
						}
					}
				});
				box.show();
			}
		});
		
		TextButton exportButton = new TextButton("Export");
		exportButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final TextAreaMessageBox box = new TextAreaMessageBox("Export " + type.getDisplayLabel(), "");
				/*box.setResizable(true);
				box.setResize(true);
				box.setMaximizable(true);*/
				box.setModal(true);
				String export = createExport();
				box.getTextArea().setText(export);
				box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						box.hide();
					}
				});
				box.show();
			}
		});
		
		/*
		TextButton consolidateButton = new TextButton("Consolidate");
		consolidateButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				try {
					consolidate();
				} catch(Exception e) {
					Alerter.showAlert("Consolidate rows", "Error occured");
				}
			}
		});
		*/
		/*TextButton removeButton = new TextButton("Remove");
		Menu removeMenu = new Menu();
		MenuItem selectedRemove = new MenuItem("Selected rows");//jin selected entry
		selectedRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				List<Row> selection = getSelection();
				eventBus.fireEvent(event);
				removeRows(getSelection());
			}
		});
		/*MenuItem consolidate = new MenuItem("Consolidate");
		emptyColRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				try {
					consolidate();
				} catch (Exception e) {
					Alerter.showAlert("Remove empty columns", "Error occured");
				}
			}
		});*/
		/*MenuItem allRemove = new MenuItem("All");
		allRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				removeRows(getAll());
			}
		});
		removeMenu.add(selectedRemove);
		//removeMenu.add(consolidate);
		removeMenu.add(allRemove);
		removeButton.setMenu(removeMenu);*/
		
		buttonBar.add(importButton);
		buttonBar.add(exportButton);
		//buttonBar.add(consolidateButton);
		//buttonBar.add(removeButton);
		
		vlc = new VerticalLayoutContainer();
		vlc.add(buttonBar, new VerticalLayoutData(1, -1));
		vlc.add(super.asWidget(), new VerticalLayoutData(1, 1));
		simpleContainer = new SimpleContainer();
		simpleContainer.setWidget(vlc);
		simpleContainer.setHideMode(HideMode.OFFSETS); //bug https://www.sencha.com/forum/showthread.php?285982-Grid-ColumnHeader-Menu-missing
	}
	
	protected String getDefaultImportText() {
		return "";	
	}

	protected void setFilter(boolean activate) {
		checkFilterItem.setChecked(activate, true);
		if(!activate) {
			filterField.setText("");
			store.removeFilters();
			store.setEnableFilters(false);
		} else {
			store.addFilter(new StoreFilter<Row>() {
				@Override
				public boolean select(Store<Row> store, Row parent, Row item) {
					String all = item.getLead().getValue() + " ";
					for(Relation r : item.getAttached())
						all += r.getDestination().getValue() + " ";
					return all.contains(filterField.getText());
				}
			});
			store.setEnableFilters(true);
		}
	}

	protected String createExport() {
		/*OntologyGraph g = ModelController.getCollection().getGraph();
		Vertex root = g.getRoot(type);
		return getRelationsAsString(g, root);*/
		StringBuilder sb = new StringBuilder();
		for(Row row : this.getAll()) {
			if(row.hasAttacheds()) {
				sb.append(row.getLead());
				for(Relation a : row.getAttached())
					sb.append(", " + a.getDestination().getValue());
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/*private String getRelationsAsString(OntologyGraph g, Vertex v) {
		StringBuilder sb = new StringBuilder();
		List<Relation> out = g.getOutRelations(v, type);
		if(!out.isEmpty()) {
			sb.append(v.getValue());
			for(Relation r : out) 
				sb.append(", " + r.getDestination().getValue());
			sb.append("\n");
			for(Relation r : out) 
				sb.append(getRelationsAsString(g, r.getDestination()));
		}
		return sb.toString();
	}*/

	@Override
	public Widget asWidget() {
		return simpleContainer;
	}

}
