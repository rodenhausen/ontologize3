package edu.arizona.biosemantics.oto2.ontologize2.client.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.ToStringValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore.TreeNode;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.CellDoubleClickEvent.CellDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.HeaderDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.HeaderDoubleClickEvent.HeaderDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent;
import com.sencha.gxt.widget.core.client.event.RowDoubleClickEvent.RowDoubleClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;
import com.sencha.gxt.widget.core.client.treegrid.TreeGrid;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.candidate.TermTreeNodeIconProvider;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveRelationEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.relations.TermsGrid.Row;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNodeProperties;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexCell;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.VertexTreeNodeProperties;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.ICollectionServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Relation;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Edge.Type;
import edu.arizona.biosemantics.oto2.oto.client.event.LoadEvent;

public class TreeView extends SimpleContainer {
	
	private static final VertexTreeNodeProperties vertexTreeNodeProperties = GWT.create(VertexTreeNodeProperties.class);
	
	protected EventBus eventBus;
	
	protected Type type;	
	
	protected TreeStore<VertexTreeNode> store;
	protected Map<Vertex, Set<VertexTreeNode>> vertexNodeMap = new HashMap<Vertex, Set<VertexTreeNode>>();
	protected TreeGrid<VertexTreeNode> treeGrid;
	
	protected ToolBar buttonBar;
	
	public TreeView(EventBus eventBus, Type type) {
		this.eventBus = eventBus;
		this.type = type;
		
		buttonBar = new ToolBar();
		
		Label titleLabel = new Label(type.getTreeLabel());
		buttonBar.add(titleLabel);
		titleLabel.getElement().getStyle().setFontSize(11, Unit.PX);
		titleLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
		titleLabel.getElement().getStyle().setPaddingLeft(3, Unit.PX);
		titleLabel.getElement().getStyle().setColor("#15428b");
		
		store = new TreeStore<VertexTreeNode>(vertexTreeNodeProperties.key());
		ColumnConfig<VertexTreeNode, Vertex> valueCol = new ColumnConfig<VertexTreeNode, Vertex>(vertexTreeNodeProperties.vertex(), 300, "Tree");
		valueCol.setCell(new VertexCell(eventBus, this, type));
		List<ColumnConfig<VertexTreeNode, ?>> list = new ArrayList<ColumnConfig<VertexTreeNode, ?>>();
		list.add(valueCol);
		ColumnModel<VertexTreeNode> cm = new ColumnModel<VertexTreeNode>(list);
		treeGrid = new TreeGrid<VertexTreeNode>(store, cm, valueCol);
		store.setAutoCommit(true);
		store.addSortInfo(new StoreSortInfo<VertexTreeNode>(new Comparator<VertexTreeNode>() {
			@Override
			public int compare(VertexTreeNode o1, VertexTreeNode o2) {
				return o1.compareTo(o2);
			}
		}, SortDir.ASC));
		//treeGrid.setIconProvider(new TermTreeNodeIconProvider());
		/*tree.setCell(new AbstractCell<PairTermTreeNode>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,	PairTermTreeNode value, SafeHtmlBuilder sb) {
				sb.append(SafeHtmlUtils.fromTrustedString("<div>" + value.getText() +  "</div>"));
			}
		}); */
		treeGrid.getElement().setAttribute("source", "termsview");
		treeGrid.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		treeGrid.setAutoExpand(true);
		treeGrid.addCellDoubleClickHandler(new CellDoubleClickHandler() {
			@Override
			public void onCellClick(CellDoubleClickEvent event) {
				Tree.TreeNode<VertexTreeNode> node = treeGrid.findNode(treeGrid.getTreeView().getRow(event.getRowIndex()));
				VertexTreeNode vertexNode = node.getModel();
				onDoubleClick(vertexNode);
			}
		});
		treeGrid.setContextMenu(createContextMenu());
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		vlc.add(buttonBar, new VerticalLayoutData(1, -1));
		vlc.add(treeGrid, new VerticalLayoutData(1, 1));
		this.setWidget(vlc);
		
		bindEvents();
	}

	private Menu createContextMenu() {
		Menu menu = new Menu();
		MenuItem context = new MenuItem("Show Context");
		context.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				eventBus.fireEvent(new SelectTermEvent(treeGrid.getSelectionModel().getSelectedItem().getText()));
			}
		});
		menu.add(context);
		return menu;
	}

	protected void onDoubleClick(VertexTreeNode vertexNode) {
		// TODO Auto-generated method stub
		
	}

	protected void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				if(!event.isEffectiveInModel()) {
					OntologyGraph g = event.getCollection().getGraph();
					Vertex root = g.getRoot(type);
					createFromRoot(g, root);
				} else {
					onLoadCollectionEffectiveInModel();
				}
			}
		}); 
		eventBus.addHandler(CreateRelationEvent.TYPE, new CreateRelationEvent.Handler() {
			@Override
			public void onCreate(CreateRelationEvent event) {
				if(!event.isEffectiveInModel())
					for(Relation r : event.getRelations()) {
						createRelation(r);
					}
				else
					for(Relation r : event.getRelations())
						onCreateRelationEffectiveInModel(r);
			}
		});
		eventBus.addHandler(RemoveRelationEvent.TYPE, new RemoveRelationEvent.Handler() {
			@Override
			public void onRemove(RemoveRelationEvent event) {
				if(!event.isEffectiveInModel())
					for(Relation r : event.getRelations())
						removeRelation(r, event.isRecursive());
				else
					for(Relation r : event.getRelations()) 
						onRemoveRelationEffectiveInModel(r);
			}
		});
		/*eventBus.addHandler(RemoveCandidateEvent.TYPE, new RemoveCandidateEvent.Handler() {
			@Override
			public void onRemove(RemoveCandidateEvent event) {
				for(Candidate c : event.getCandidates()) 
					removeCandidate(c);
			}
		});*/
	}
	
	protected void onLoadCollectionEffectiveInModel() {
		// TODO Auto-generated method stub
		
	}

	protected void onRemoveRelationEffectiveInModel(Relation r) {
		// TODO Auto-generated method stub
		
	}

	protected void onCreateRelationEffectiveInModel(Relation r) {
		// TODO Auto-generated method stub
		
	}

	protected void createFromRoot(OntologyGraph g, Vertex root) {
		clearTree();
		VertexTreeNode rootNode = new VertexTreeNode(root);
		add(null, rootNode);
		createFromVertex(g, root);
	}
	


	protected void createFromVertex(OntologyGraph g, Vertex source) {
		for(Relation r : g.getOutRelations(source, type)) {
			createRelation(r);
			createFromVertex(g, r.getDestination());
		}
	}

	protected void removeCandidate(Candidate c) {
		/*Vertex possibleVertex = new Vertex(c.getText());
		if(vertexNodeMap.containsKey(possibleVertex)) {
			for(VertexTreeNode node : vertexNodeMap.get(possibleVertex))
				remove(node);
		}*/
	}

	protected void createRelation(Relation r) {
		if(r.getEdge().getType().equals(type)) {
			VertexTreeNode sourceNode = null;
	 		if(vertexNodeMap.containsKey(r.getSource())) {
				sourceNode = vertexNodeMap.get(r.getSource()).iterator().next();
			} else {
				sourceNode = new VertexTreeNode(r.getSource());
				add(null, sourceNode);
			}
			if(vertexNodeMap.containsKey(r.getDestination())) {
				Alerter.showAlert("Failed to create relation", "Failed to create relation");
				return;
			}
			VertexTreeNode destinationNode = new VertexTreeNode(r.getDestination());
			add(sourceNode, destinationNode);
			if(treeGrid.isRendered())
				treeGrid.setExpanded(sourceNode, true);
		}
	}
	
	protected void removeRelation(Relation r, boolean recursive) {
		if(r.getEdge().getType().equals(type)) {
			if(vertexNodeMap.containsKey(r.getSource()) && vertexNodeMap.containsKey(r.getDestination())) {
				VertexTreeNode sourceNode = vertexNodeMap.get(r.getSource()).iterator().next();
				VertexTreeNode targetNode = vertexNodeMap.get(r.getDestination()).iterator().next();
				if(recursive) {
					remove(targetNode);
				} else {
					List<TreeNode<VertexTreeNode>> targetChildNodes = new LinkedList<TreeNode<VertexTreeNode>>();
					for(VertexTreeNode targetChild : store.getChildren(targetNode)) {
						targetChildNodes.add(store.getSubTree(targetChild));
					}
					remove(targetNode);
					store.addSubTree(sourceNode, store.getChildCount(sourceNode), targetChildNodes);
				}
			}
		}
	}

	protected void clearTree() {
		store.clear();
		vertexNodeMap.clear();
	}
	
	protected void replaceNode(VertexTreeNode oldNode, VertexTreeNode newNode) {
		List<TreeNode<VertexTreeNode>> childNodes = new LinkedList<TreeNode<VertexTreeNode>>();
		for(VertexTreeNode childNode : store.getChildren(oldNode)) {
			childNodes.add(store.getSubTree(childNode));
		}
		
		VertexTreeNode parent = store.getParent(oldNode);
		remove(oldNode);
		add(parent, newNode);
		store.addSubTree(newNode, 0, childNodes);
	}
	
	protected void remove(VertexTreeNode node) {
		removeAllChildren(node);
		store.remove(node);
		if(vertexNodeMap.containsKey(node.getVertex())) {
			vertexNodeMap.get(node.getVertex()).remove(node);
			if(vertexNodeMap.get(node.getVertex()).isEmpty())
				vertexNodeMap.remove(node.getVertex());
		}
	}
	
	protected void removeAllChildren(VertexTreeNode frommNode) {
		List<VertexTreeNode> allRemoves = store.getAllChildren(frommNode);
		for(VertexTreeNode remove : allRemoves) {
			Vertex v = remove.getVertex();
			if(vertexNodeMap.containsKey(v)) {
				vertexNodeMap.get(v).remove(remove);
				if(vertexNodeMap.get(v).isEmpty()) 
					vertexNodeMap.remove(v);
			}
		}
		store.removeChildren(frommNode);
	}
	
	protected void add(VertexTreeNode parent, VertexTreeNode child) {
		if(parent == null)
			store.add(child);
		else
			store.add(parent, child);
		if(!vertexNodeMap.containsKey(child.getVertex()))
			vertexNodeMap.put(child.getVertex(), new HashSet<VertexTreeNode>(Arrays.asList(child)));
		else {
			vertexNodeMap.get(child.getVertex()).add(child);
		}
	}

	protected Vertex getRoot() {
		return treeGrid.getTreeStore().getRootItems().get(0).getVertex();
	}
	
}
