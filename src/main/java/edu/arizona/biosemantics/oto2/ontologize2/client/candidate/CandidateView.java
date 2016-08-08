package edu.arizona.biosemantics.oto2.ontologize2.client.candidate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.TreeDragSource;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.MultiLinePromptMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.common.TextAreaMessageBox;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.CreateCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.RemoveCandidateEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.BucketTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.CandidateTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNode;
import edu.arizona.biosemantics.oto2.ontologize2.client.tree.node.TextTreeNodeProperties;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Candidate;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;

public class CandidateView extends SimpleContainer {

	private static final TextTreeNodeProperties textTreeNodeProperties = GWT.create(TextTreeNodeProperties.class);
	
	private Tree<TextTreeNode, TextTreeNode> tree;
	private Map<String, CandidateTreeNode> candidateNodeMap = new HashMap<String, CandidateTreeNode>();
	private Map<String, BucketTreeNode> bucketNodesMap = new HashMap<String, BucketTreeNode>();	
	private EventBus eventBus;
	private ToolBar buttonBar;
	
	private CandidateView() {
		TreeStore<TextTreeNode> treeStore = new TreeStore<TextTreeNode>(textTreeNodeProperties.key());
		treeStore.setAutoCommit(true);
		treeStore.addSortInfo(new StoreSortInfo<TextTreeNode>(new Comparator<TextTreeNode>() {
			@Override
			public int compare(TextTreeNode o1, TextTreeNode o2) {
				return o1.getText().compareTo(o2.getText());
			}
		}, SortDir.ASC));
		tree = new Tree<TextTreeNode, TextTreeNode>(treeStore, new IdentityValueProvider<TextTreeNode>());
		tree.setIconProvider(new TermTreeNodeIconProvider());
		tree.setCell(new AbstractCell<TextTreeNode>() {
			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context,	TextTreeNode value, SafeHtmlBuilder sb) {
				sb.append(SafeHtmlUtils.fromTrustedString("<div>" + value.getText() +  "</div>"));
			}
		});
		tree.getElement().setAttribute("source", "termsview");
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTI);
		/*tree.getSelectionModel().addSelectionHandler(new SelectionHandler<TextTreeNode>() {
			@Override
			public void onSelection(SelectionEvent<TextTreeNode> event) {
				eventBus.fireEvent(new SelectTermEvent(event.getSelectedItem().getText()));
			}
		});*/
		tree.setAutoExpand(true);
		tree.setContextMenu(createContextMenu());
		
		TreeDragSource<TextTreeNode> dragSource = new TreeDragSource<TextTreeNode>(tree) {
			@Override
			protected void onDragStart(DndDragStartEvent event) {
				super.onDragStart(event);
				List<Candidate> data = new LinkedList<Candidate>();
				for(TextTreeNode node : tree.getSelectionModel().getSelectedItems()) {
					addTermTreeNodes(node, data);
				}
				event.setData(data);
			}
		};
		
		buttonBar = new ToolBar();
		TextButton importButton = new TextButton("Import");
		importButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final TextAreaMessageBox box = new TextAreaMessageBox("Import terms", "");
				/*box.setResizable(true);
				box.setResize(true);
				box.setMaximizable(true);*/
				box.setModal(true);
				box.getButton(PredefinedButton.OK).addSelectHandler(new SelectHandler() {
					@Override
					public void onSelect(SelectEvent event) {
						String input = box.getValue();
						String[] lines = input.split("\\n");
						List<Candidate> candidates = new LinkedList<Candidate>();
						for(String line : lines) {
							String[] candidatePath = line.split(",");
							if(candidatePath.length == 1) {
								String candidate = candidatePath[0];
								if(!ModelController.getCollection().getCandidates().contains(candidate))
									candidates.add(new Candidate(candidate));
								else
									Alerter.showAlert("Candidate exists", "Candidate + \"" + candidate + "\" already exists at \"" +
											ModelController.getCollection().getCandidates().getPath(candidate) + "\"");
							} else if(candidatePath.length >= 2) {
								String candidate = candidatePath[0];
								if(!ModelController.getCollection().getCandidates().contains(candidate))
									candidates.add(new Candidate(candidatePath[0], candidatePath[1]));
								else
									Alerter.showAlert("Candidate exists", "Candidate + \"" + candidate + "\" already exists at \"" +
											ModelController.getCollection().getCandidates().getPath(candidate) + "\"");
							}
						}
						
						eventBus.fireEvent(new CreateCandidateEvent(candidates));
					}
				});
				box.show();
			}
		});
		
		TextButton removeButton = new TextButton("Remove");
		Menu removeMenu = new Menu();
		MenuItem selectedRemove = new MenuItem("Selected");
		selectedRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				removeNodes(tree.getSelectionModel().getSelectedItems());
			}
		});
		
		MenuItem allRemove = new MenuItem("All");
		allRemove.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				removeNodes(tree.getStore().getAll());
			}
		});
		removeMenu.add(selectedRemove);
		removeMenu.add(allRemove);
		removeButton.setMenu(removeMenu);
		
		buttonBar.add(importButton);
		buttonBar.add(removeButton);
		
		HorizontalLayoutContainer hlc = new HorizontalLayoutContainer();
		final TextField termField = new TextField();
		TextButton addButton = new TextButton("Add");
		addButton.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				final String newTerm = termField.getValue().trim();
				if(newTerm.isEmpty()) 
					Alerter.showAlert("Add Term", "Term field is empty");
				else if(ModelController.getCollection().contains(newTerm)) 
					Alerter.showAlert("Add Term", "Term already exists");
				else
					if(!ModelController.getCollection().contains(newTerm)) {
						tree.getSelectionModel().getSelectedItem();
						
						BucketTreeNode bucketNode = getSelectedBucket();
						if(bucketNode != null) {
							eventBus.fireEvent(new CreateCandidateEvent(new Candidate(newTerm, bucketNode.getPath())));
						} else {
							eventBus.fireEvent(new CreateCandidateEvent(new Candidate(newTerm)));
						}
						termField.setValue("");
					} else {
						Alerter.showAlert("Create Term", "Term already exists");
					}
			}
		});
		hlc.add(termField, new HorizontalLayoutData(1, -1));
		hlc.add(addButton);
		
		FieldLabel field = new FieldLabel(hlc, "Add Term");
		
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		vlc.add(buttonBar, new VerticalLayoutData(1, -1));
		vlc.add(tree, new VerticalLayoutData(1, 1));
		vlc.add(field, new VerticalLayoutData(1, 40));
		this.add(vlc);
	}
	
	private Menu createContextMenu() {
		Menu menu = new Menu();
		MenuItem context = new MenuItem("Show Context");
		context.addSelectionHandler(new SelectionHandler<Item>() {
			@Override
			public void onSelection(SelectionEvent<Item> event) {
				eventBus.fireEvent(new SelectTermEvent(tree.getSelectionModel().getSelectedItem().getText()));
			}
		});
		menu.add(context);
		return menu;
	}

	private BucketTreeNode getSelectedBucket() {
		List<TextTreeNode> selection = tree.getSelectionModel().getSelectedItems();
		if(!selection.isEmpty()) {
			TextTreeNode node = selection.get(0);
			if(node instanceof BucketTreeNode) {
				return (BucketTreeNode)node;
			}
			if(node instanceof CandidateTreeNode) {
				TextTreeNode parent = tree.getStore().getParent(node);
				if(parent instanceof BucketTreeNode)
					return (BucketTreeNode) parent;
			}
		}
		return null;
	}
	
	protected void removeNodes(List<TextTreeNode> nodes) {
		final List<Candidate> remove = new LinkedList<Candidate>();
		for(TextTreeNode node : nodes) {
			if(node instanceof BucketTreeNode) {
				addTerms((BucketTreeNode)node, remove);
			}
			if(node instanceof CandidateTreeNode) {
				remove.add(((CandidateTreeNode)node).getCandidate());
			}
		}
		eventBus.fireEvent(new RemoveCandidateEvent(remove));
	}

	private void addTerms(BucketTreeNode node, List<Candidate> list) {
		for(TextTreeNode childNode : tree.getStore().getChildren(node)) {
			if(childNode instanceof CandidateTreeNode) {
				list.add(((CandidateTreeNode)childNode).getCandidate());
			} else if(childNode instanceof BucketTreeNode) {
				this.addTerms((BucketTreeNode)childNode, list);
			}
		}
	}

	protected void addTermTreeNodes(TextTreeNode node, List<Candidate> data) {
		if(node instanceof BucketTreeNode) {
			for(TextTreeNode child : tree.getStore().getChildren(node)) {
				this.addTermTreeNodes(child, data);
			}
		} else if(node instanceof CandidateTreeNode) {
			Candidate candidate = ((CandidateTreeNode)node).getCandidate();
			data.add(candidate);
		}
	}

	public CandidateView(EventBus eventBus) {
		this();
		this.eventBus = eventBus;
		
		bindEvents();
	}
	
	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				if(event.isEffectiveInModel())
					setCollection(event.getCollection());
			}
		}); 
		eventBus.addHandler(CreateCandidateEvent.TYPE, new CreateCandidateEvent.Handler() {
			@Override
			public void onCreate(CreateCandidateEvent event) {
				add(Arrays.asList(event.getCandidates()));
			}
		});
		eventBus.addHandler(RemoveCandidateEvent.TYPE, new RemoveCandidateEvent.Handler() {
			@Override
			public void onRemove(RemoveCandidateEvent event) {
				remove(Arrays.asList(event.getCandidates()));
			}
		});
	}
	
	public void setCollection(Collection collection) {
		tree.getStore().clear();
		//termTermTreeNodeMap.clear();
		//bucketTreeNodesMap.clear();
		add(collection.getCandidates());
		//initializeCollapsing(bucketTreeNodesMap);
	}

	protected void remove(Iterable<Candidate> candidates) {
		for(Candidate candidate : candidates)
			if(candidateNodeMap.containsKey(candidate.getText())) {
				tree.getStore().remove(candidateNodeMap.get(candidate.getText()));
				candidateNodeMap.remove(candidate.getText());
			}
	}

	private void add(Iterable<Candidate> candidates) {
		for(Candidate candidate : candidates) {
			createBucketNodes(bucketNodesMap, candidate.getPath());
			addTermTreeNode(bucketNodesMap.get(candidate.getPath()), new CandidateTreeNode(candidate));
		}
	}

	protected void createBucketNodes(Map<String, BucketTreeNode> bucketsMap, String path) {
		if(path == null) 
			return;
		String[] buckets = path.split("/");
		String cumulativePath = "";
		String parentPath = "";
		for(String bucket : buckets) {
			if(!bucket.isEmpty()) {
				cumulativePath += "/" + bucket;
				if(!bucketsMap.containsKey(cumulativePath)) {
					BucketTreeNode bucketTreeNode = new BucketTreeNode(cumulativePath);
					if(parentPath.isEmpty())
						tree.getStore().add(bucketTreeNode);
					else
						tree.getStore().add(bucketsMap.get(parentPath), bucketTreeNode);
					bucketsMap.put(cumulativePath, bucketTreeNode);
				}
				parentPath = cumulativePath;
			}
		}
	}

	protected void addTermTreeNode(BucketTreeNode bucketNode, CandidateTreeNode candidateTreeNode) {
		this.candidateNodeMap.put(candidateTreeNode.getCandidate().getText(), candidateTreeNode);
		if(bucketNode == null)
			this.tree.getStore().add(candidateTreeNode);
		else
			this.tree.getStore().add(bucketNode, candidateTreeNode);
	}
	
	private void initializeCollapsing(Map<String, BucketTreeNode> bucketTreeNodes) {
//		for(BucketTreeNode node : bucketTreeNodes.values()) {
//			if(tree.getStore().getChildren(node).get(0) instanceof TermTreeNode) {
//				tree.setExpanded(node, false);
//			} else {
//				tree.setExpanded(node, true);
//			}
//		}
	}	
}
