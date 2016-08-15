package edu.arizona.biosemantics.oto2.ontologize2.client.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.TreeStore.TreeNode;
import com.sencha.gxt.dnd.core.client.DND.Operation;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent.DndDropHandler;
import com.sencha.gxt.dnd.core.client.DropTarget;
import com.sencha.gxt.state.client.GridFilterStateHandler;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.ReplaceRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell.AttachedCell;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell.DefaultMenuCreator;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell.LeadCell;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Origin;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;

public class TermsGrid implements IsWidget {

	public static class Row {
		
		private static int currentId = 0;
		
		private int id = currentId++;
		private Vertex lead;
		private List<Edge> attached = new ArrayList<Edge>();
		
		public Row(Vertex lead) {
			this.lead = lead;
		}
		
		public Row(Vertex lead, List<Edge> attached) {
			this(lead);
			this.attached.addAll(attached);
		}
		
		public Vertex getLead() {
			return lead;
		}
		
		public List<Edge> getAttached() {
			return new ArrayList<Edge>(this.attached);
		}

		public int getId() {
			return id;
		}
		
		public void add(int index, Edge relation) throws Exception {
			if(contains(relation))
				throw new Exception("Edge already exists.");
			this.attached.add(index, relation);
		}

		public void add(Edge relation) throws Exception {
			int index = attached.size();
			this.add(index, relation);
		}
		
		public void add(Collection<Edge> relations) throws Exception {
			for(Edge relation : relations) 
				this.add(relation);
		}
		
		public void remove(int i) {
			Edge relation = attached.remove(i);
		}
		
		public void remove(String attached) {
			for(Edge r : this.attached) {
				if(r.getDest().getValue().equals(attached))
					this.remove(r);
			}
		}
		
		public void remove(Edge relation) {
			for(int i=0; i < attached.size(); i++) {
				if(attached.get(i).getDest().equals(relation.getDest()))
					this.remove(i);
			}
		}
		
		public void remove(Collection<Edge> relations) {
			for(Edge relation : relations)
				this.remove(relation);
		}

		public void setLead(Vertex lead) {
			this.lead = lead;
		}

		public int getAttachedCount() {
			return this.attached.size();
		}

		public boolean hasAttacheds() {
			return !this.attached.isEmpty();
		}
		
		public boolean containsAttached(Vertex v) {
			for(Edge relation : attached) {
				if(relation.getDest().equals(v))
					return true;
			}
			return false;
		}
		
		public boolean contains(Edge relation) {
			for(Edge r : attached) {
				if(r.getDest().equals(relation.getDest())) {
					return true;
				}
			}
			return false;
		}

		
		public int size() {
			return this.getAttachedCount() + 1;
		}

		public List<Vertex> getAll() {
			List<Vertex> result = new ArrayList<Vertex>(attached.size() + 1);
			result.add(this.lead);
			for(Edge relation : this.attached)
				result.add(relation.getDest());
			return result;
		}

		public void replaceAttachedDest(Vertex dest, Vertex disambiguatedDest) {
			for(int i=0; i<attached.size(); i++) {
				Edge r = attached.get(i);
				if(r.getDest().equals(dest))
					attached.set(i, new Edge(r.getSrc(), disambiguatedDest, r.getType(), r.getOrigin()));
			}
		}
		
		@Override
		public String toString() {
			String result = this.lead.getValue();
			for(Edge a : attached)
				result += ", " + a.getDest().getValue();
			return result;
		}
	}
	
	public static interface RowProperties extends PropertyAccess<Row> {

		@Path("id")
		ModelKeyProvider<Row> key();

		@Path("id")
		ValueProvider<Row, Integer> id();

		@Path("all")
		ValueProvider<Row, List<Vertex>> all();
	}
	
	protected EventBus eventBus;
	private RowProperties rowProperties = GWT.create(RowProperties.class);
	protected ListStore<Row> store;
	protected Map<Vertex, Row> leadRowMap = new HashMap<Vertex, Row>();
	//protected Map<Vertex, Set<Row>> vertexRowMap = new HashMap<Vertex, Set<Row>>();
	protected Grid<Row> grid;
	protected SimpleContainer createRowContainer;
	private final int colWidth = 100;
	protected Type type;
	private VerticalLayoutContainer vlc;
	private SimpleContainer simpleContainer;
	protected DropTarget dropTarget;
	
	public TermsGrid(final EventBus eventBus, final Type type) {
		this.eventBus = eventBus;
		this.type = type;
		store = new ListStore<Row>(rowProperties.key());
		store.setAutoCommit(true);
		this.grid = new Grid<Row>(store, createColumnModel(new LinkedList<Row>()));// createColumnModel(1));//createColumnModel(new LinkedList<Row>()));
		
		createRowContainer = createCreateRowContainer();

		dropTarget = new DropTarget(grid);
		dropTarget.addDropHandler(new DndDropHandler() {
			@Override
			public void onDrop(DndDropEvent event) {
				Element element = event.getDragEndEvent().getNativeEvent().getEventTarget().<Element> cast();
				int targetRowIndex = grid.getView().findRowIndex(element);
				Row row = store.get(targetRowIndex);
				
				if(event.getData() instanceof List<?>) {
					List<Edge> add = new LinkedList<Edge>();
					List<?> list = (List<?>)event.getData();
					for(Object item : list) {
						if(item instanceof Candidate) {
							Candidate c = (Candidate)item;
							add.add(new Edge(row.getLead(), new Vertex(c.getText()), type, Origin.USER));
						}
					}
					CreateRelationEvent createRelationEvent = new CreateRelationEvent(add);
					fire(createRelationEvent);
				}
			}
		});
		dropTarget.setOperation(Operation.COPY);
		if(createRowContainer != null) {
			DropTarget dropTargetNewRow = new DropTarget(createRowContainer);
			dropTargetNewRow.addDropHandler(new DndDropHandler() {
				@Override
				public void onDrop(DndDropEvent event) {
					if(event.getData() instanceof List<?>) {
						List<?> list = (List<?>)event.getData();
						if(list.size() == 1) {
							Object item = list.get(0);
							if(item instanceof Candidate) {
								Vertex source = new Vertex(type.getRootLabel());
								Vertex target = new Vertex(((Candidate)item).getText());
								Edge relation = new Edge(source, target, type, Origin.USER);
								CreateRelationEvent createRelationEvent = new CreateRelationEvent(relation);
								fire(createRelationEvent);
							}
						}
					}
					if(event.getData() instanceof Edge) {
						createRowFromEdgeDrop((Edge)event.getData());
					}
				}
			});
			dropTargetNewRow.setOperation(Operation.COPY);
		}
		vlc = new VerticalLayoutContainer();
		vlc.add(grid);
		if(createRowContainer != null)
			vlc.add(createRowContainer);
		vlc.getScrollSupport().setScrollMode(ScrollMode.AUTO);
		simpleContainer = new SimpleContainer();
		simpleContainer.add(vlc);
		
		bindEvents();
	}

	protected void createRowFromEdgeDrop(Edge edge) {
		// TODO Auto-generated method stub
		
	}

	public void fire(GwtEvent<? extends EventHandler> e) {
		eventBus.fireEvent(e);
	}

	protected void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {			
			@Override
			public void onLoad(LoadCollectionEvent event) {
				if(!event.isEffectiveInModel()) {
					clearGrid();
					OntologyGraph g = event.getCollection().getGraph();
					TermsGrid.this.onLoad(g);
				} else {
					onLoadCollectionEffectiveInModel();
				}
			}
		}); 
		eventBus.addHandler(CreateRelationEvent.TYPE, new CreateRelationEvent.Handler() {
			@Override
			public void onCreate(CreateRelationEvent event) {
				if(!event.isEffectiveInModel())
					for(Edge r : event.getRelations())
						createRelation(r);
				else
					for(Edge r : event.getRelations())
						onCreateRelationEffectiveInModel(r);
			}
		});
		eventBus.addHandler(RemoveRelationEvent.TYPE, new RemoveRelationEvent.Handler() {
			@Override
			public void onRemove(RemoveRelationEvent event) {
				if(!event.isEffectiveInModel())
					for(Edge r : event.getRelations())
						removeRelation(r, event.isRecursive());
				else 
					for(Edge r : event.getRelations())
						onRemoveRelationEffectiveInModel(r);
			}
		});
		eventBus.addHandler(ReplaceRelationEvent.TYPE, new ReplaceRelationEvent.Handler() {
			@Override
			public void onReplace(ReplaceRelationEvent event) {
				if(!event.isEffectiveInModel()) {
					replaceRelation(event.getOldRelation(), event.getNewSource());
				} else {
					onReplaceRelationEffectiveInModel(event.getOldRelation(), event.getNewSource());
				}
			}
		});
		/*eventBus.addHandler(RemoveCandidateEvent.TYPE, new RemoveCandidateEvent.Handler() {
			@Override
			public void onRemove(RemoveCandidateEvent event) {
				//removeCandidates(event.getCandidates());
			}
		});*/
	}

	protected void replaceRelation(Edge oldRelation, Vertex newSource) {
		if(oldRelation.getType().equals(type)) {
			if(leadRowMap.containsKey(oldRelation.getSrc()) && leadRowMap.containsKey(newSource)) {
				Row oldRow = leadRowMap.get(oldRelation.getSrc());
				oldRow.remove(oldRelation);
				updateRow(oldRow);
				
				Row newRow = leadRowMap.get(newSource);
				try {
					addAttached(newRow, new Edge(newSource, oldRelation.getDest(), oldRelation.getType(), oldRelation.getOrigin()));
				} catch (Exception e) {
					Alerter.showAlert("Failed to replace relation", "Failed to replace relation");
					return;
				}
				
			} else {
				Alerter.showAlert("Failed to replace relation", "Failed to replace relation");
			}
		}
	}

	protected void onReplaceRelationEffectiveInModel(Edge oldRelation, Vertex newSource) {
		// TODO Auto-generated method stub
		
	}

	protected void onRemoveRelationEffectiveInModel(Edge r) {
		// TODO Auto-generated method stub
		
	}

	protected void onLoadCollectionEffectiveInModel() {
		// TODO Auto-generated method stub
		
	}

	protected void onCreateRelationEffectiveInModel(Edge r) {
		// TODO Auto-generated method stub
		
	}

	protected void clearGrid() {
		store.clear();
		leadRowMap.clear();
		//vertexRowMap.clear();
	}
	
	protected void onLoad(OntologyGraph g) {
		Row rootRow = new Row(g.getRoot(type));
		addRow(rootRow);
		
		createEdges(g, g.getRoot(type), new HashSet<String>());
		//grid.reconfigure(store, createColumnModel(this.getAll()));
		//grid.getView().refresh(true);
	}
	
	protected void createEdges(OntologyGraph g, Vertex source, Set<String> createdRelations) {
		for(Edge r : g.getOutRelations(source, type)) {
			String relationIdentifier = r.getSrc().getValue() + " - " + r.getDest().getValue() + " " + r.getType().toString();
			if(!createdRelations.contains(relationIdentifier)) {
				createdRelations.add(relationIdentifier);
				createRelation(r);
				createEdges(g, r.getDest(), createdRelations);
			}
		}
	}

	protected void removeRelation(Edge r, boolean recursive) {
		if(r.getType().equals(type)) {
			if(leadRowMap.containsKey(r.getSrc())) {
				Row row = leadRowMap.get(r.getSrc());
				removeAttached(row, r, recursive);
			} else {
				Alerter.showAlert("Failed to remove relation", "Failed to remove relation");
			}
		}
	}

	protected void createRelation(Edge r) {
		if(r.getType().equals(type)) {
			Row row = null;
			if(leadRowMap.containsKey(r.getSrc())) {
				row = leadRowMap.get(r.getSrc());
			} else {
				row = new Row(r.getSrc());
				this.addRow(row);
			}	
			try {
				addAttached(row, new Edge(r.getSrc(), r.getDest(), r.getType(), r.getOrigin()));
			} catch (Exception e) {
				Alerter.showAlert("Failed to create relation", "Failed to create relation");
				return;
			}
		}
	}

	protected void addRow(Row row) {
		store.add(row);
		leadRowMap.put(row.getLead(), row);
		/*(addVertexRowMap(row.getLead(), row);
		for(Relation r : row.getAttached())
			addVertexRowMap(r.getDestination(), row);*/
	}
		
	/*private void addVertexRowMap(Vertex v, Row r) {
		if(!vertexRowMap.containsKey(v))
			vertexRowMap.put(v, new HashSet<Row>());
		vertexRowMap.get(v).add(r);
	}*/
	
	protected void removeRow(Row row, boolean recursive) {
		if(recursive) {
			for(Edge relation : row.getAttached()) {
				if(leadRowMap.containsKey(relation.getDest())) {
					Row attachedRow = leadRowMap.get(relation.getDest());
					removeRow(attachedRow, true);
				}
			}
		} else {
			OntologyGraph graph = ModelController.getCollection().getGraph();
			Vertex lead = row.getLead();
			for(Edge r : graph.getInRelations(lead, type)) {
				try {
					Row targetRow = leadRowMap.get(r.getSrc());
					if(!row.getAttached().isEmpty()) {
						targetRow.add(row.getAttached());
						updateRow(targetRow);
					}
				} catch (Exception e) {
					Alerter.showAlert("Failed to reattach", "Failed to reattach");
				}
			}
		}
		store.remove(row);
		leadRowMap.remove(row.getLead());
		/*this.removeVertexRowMap(row.getLead(), row);
		for(Relation r : row.getAttached())
			this.removeVertexRowMap(r.getDestination(), row);*/
	}
	
	/*private void removeVertexRowMap(Vertex v, Row r) {
		if(vertexRowMap.containsKey(v))
			vertexRowMap.get(v).remove(r);
		if(vertexRowMap.get(v).isEmpty())
			vertexRowMap.remove(v);
	}*/

	protected void addAttached(Row row, Edge... add) throws Exception {
		row.add(Arrays.asList(add));
		updateRow(row);
		for(Edge r : add) {
			//this.addVertexRowMap(r.getDestination(), row);
			if(!leadRowMap.containsKey(r.getDest())) {
				Row addRow = new Row(r.getDest());
				this.addRow(addRow);
			}
		}
	}
	
	protected void removeAttached(Row row, Edge r, boolean recursive) {
		row.remove(r);
		updateRow(row);
		
		Row targetRow = leadRowMap.get(r.getDest());
		if(targetRow != null)
			removeRow(targetRow, recursive);
	}
	
	protected void updateRow(Row row) {
		if(row.size() <= grid.getColumnModel().getColumnCount()) 
			store.update(row);
		else {
			this.reconfigureForAttachedTerms(row.getAttachedCount());
			store.update(row);
		}
	}
	
	protected List<Row> getAttachedRows(Vertex v) {
		List<Row> result = new LinkedList<Row>();
		if(leadRowMap.containsKey(v))
			result.add(leadRowMap.get(v));
		OntologyGraph g = ModelController.getCollection().getGraph();
		for(Edge inRelations : g.getInRelations(v, type)) {
			if(leadRowMap.containsKey(inRelations.getSrc())) {
				result.add(leadRowMap.get(inRelations.getSrc()));
			}
		}
		return result;
	}
	
	public Row getRow(int index) {
		return store.get(index);
	}
	
	protected List<Row> getSelection() {
		return new ArrayList<Row>(grid.getSelectionModel().getSelectedItems());
	}

	protected List<Row> getAll() {
		return new ArrayList<Row>(grid.getStore().getAll());
	}

	private void reconfigureForAttachedTerms(int attachedTermsCount) {
		grid.reconfigure(store, createColumnModel(attachedTermsCount));
	}
	
	private ColumnModel<Row> createColumnModel(int attachedTermsCount) {
		List<ColumnConfig<Row, ?>> columns = new ArrayList<ColumnConfig<Row, ?>>();
		ColumnConfig<Row, Vertex> column1 = new ColumnConfig<Row, Vertex>(new ValueProvider<Row, Vertex>() {
			@Override
			public Vertex getValue(Row object) {
				return object.getLead();
			}
			@Override
			public void setValue(Row object, Vertex value) { }
			@Override
			public String getPath() {
				return "lead";
			}
		}, colWidth, SafeHtmlUtils.fromTrustedString("<b>" + type.getSourceLabel() + "</b>"));
		column1.setSortable(false);
		column1.setHideable(false);
		column1.setGroupable(false);
		column1.setMenuDisabled(true);
		LeadCell cell = createLeadCell();
		column1.setCell(cell);
		columns.add(column1);
		for(int i = 1; i <= attachedTermsCount; i++)
			columns.add(createColumnI(i));
		return new ColumnModel<Row>(columns);
	}
	
	protected LeadCell createLeadCell() {
		LeadCell leadCell = new LeadCell(new ValueProvider<Vertex, String>() {
			@Override
			public String getValue(Vertex object) {
				return object.getValue();
			}
			@Override
			public void setValue(Vertex object, String value) { }
			@Override
			public String getPath() {
				return "lead";
			}
		}, new DefaultMenuCreator(eventBus, this));
		return leadCell;
	}

	private ColumnModel<Row> createColumnModel(Collection<Row> rows) {	
		return createColumnModel(getMaxAttachedTermsCount(rows));
	}
	
	/**
	 * create the i-th column, its path is term-i
	 * @param i
	 * @return
	 */
	private ColumnConfig<Row, ?> createColumnI(final int i) {
		ColumnConfig<Row, Row> config = new ColumnConfig<Row, Row>(new ValueProvider<Row, Row>() {
			@Override
			public Row getValue(Row object) {
				return object;
			}
			@Override
			public void setValue(Row object, Row value) { }
			@Override
			public String getPath() {
				return "attached-" + i;
			}
		}, colWidth, SafeHtmlUtils.fromTrustedString("<b>" + type.getTargetLabel() + "-" + i + "</b>"));
		AttachedCell cell = createAttachedCell(i - 1);
		config.setCell(cell);
		config.setSortable(false);
		config.setHideable(false);
		config.setGroupable(false);
		config.setMenuDisabled(true);
		return config;
	}
	
	protected AttachedCell createAttachedCell(int i) {
		AttachedCell attachedCell = new AttachedCell(eventBus, this, i);
		return attachedCell;
	}
		
	private int getMaxAttachedTermsCount(Collection<Row> rows) {
		if(rows.isEmpty())
			return 0;
		Row maxRow = rows.iterator().next();
		for(Row row : rows) {
			int size = row.getAttachedCount();
			if(size > maxRow.getAttachedCount())
				maxRow = row;
		}
		return maxRow.getAttachedCount();
	}

	protected SimpleContainer createCreateRowContainer() {
		createRowContainer = new SimpleContainer();
		createRowContainer.setTitle("Drop here to create new row");
		com.google.gwt.user.client.ui.Label dropLabel = new com.google.gwt.user.client.ui.Label("Drop here to create new row");
		dropLabel.getElement().getStyle().setLineHeight(30, Unit.PX);
		createRowContainer.setWidget(dropLabel);
		createRowContainer.setHeight(30);
		createRowContainer.getElement().getStyle().setBorderWidth(1, Unit.PX);
		createRowContainer.getElement().getStyle().setBorderStyle(BorderStyle.DASHED);
		createRowContainer.getElement().getStyle().setBorderColor("gray");
		createRowContainer.getElement().getStyle().setProperty("mozMorderMadius", "7px");
		createRowContainer.getElement().getStyle().setProperty("webkitBorderRadius", "7px");
		createRowContainer.getElement().getStyle().setProperty("borderRadius", "7px");
		createRowContainer.getElement().getStyle().setBackgroundColor("#ffffcc");
		return createRowContainer;
	}

	@Override
	public Widget asWidget() {
		return simpleContainer;
	}

	public Type getType() {
		return type;
	}

	public void refreshHeader() {
        grid.getView().refresh(true);
	}
}
