package edu.arizona.biosemantics.oto2.ontologize2.client.info;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.core.client.Style.HideMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.state.client.GridStateHandler;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.event.ShowEvent;
import com.sencha.gxt.widget.core.client.event.ShowEvent.ShowHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowExpander;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.grid.filters.GridFilters;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import com.sencha.gxt.widget.core.client.tips.QuickTip;

import edu.arizona.biosemantics.common.log.LogLevel;
import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;
import edu.arizona.biosemantics.oto2.ontologize2.client.ModelController;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.LoadCollectionEvent;
import edu.arizona.biosemantics.oto2.ontologize2.client.event.SelectTermEvent;
import edu.arizona.biosemantics.oto2.ontologize2.shared.IContextService;
import edu.arizona.biosemantics.oto2.ontologize2.shared.IContextServiceAsync;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.Collection;
import edu.arizona.biosemantics.oto2.ontologize2.shared.model.ExtractContext;

public class ContextView extends Composite {

	private static final ExtractContextProperties contextProperties = GWT.create(ExtractContextProperties.class);
	private ListStore<ExtractContext> store = new ListStore<ExtractContext>(contextProperties.key());
	private IContextServiceAsync contextService = GWT.create(IContextService.class);
	
	private EventBus eventBus;
	private Grid<ExtractContext> grid;
	private String currentTerm;
	private AutoProgressMessageBox searchingBox;
	
	public ContextView(EventBus eventBus) {
		this.eventBus = eventBus;
		store.setAutoCommit(true);
		RowNumberer<ExtractContext> numberer = new RowNumberer<ExtractContext>();
	    RowExpander<ExtractContext> expander = new RowExpander<ExtractContext>(new AbstractCell<ExtractContext>() {
	        @Override
	        public void render(Context context, ExtractContext value, SafeHtmlBuilder sb) {
	          sb.appendHtmlConstant("<p style='margin: 5px 5px 10px'><b>Full Text:&nbsp;</b></br>" + value.getFullText() + "</p>");
	          //sb.appendHtmlConstant("<p style='margin: 5px 5px 10px'><b>Summary:</b> " + desc);
	        }
	      });
		ColumnConfig<ExtractContext, String> sourceColumn = new ColumnConfig<ExtractContext, String>(contextProperties.source(), 50, SafeHtmlUtils.fromTrustedString("<b>Source</b>"));
		ColumnConfig<ExtractContext, String> textColumn = new ColumnConfig<ExtractContext, String>(contextProperties.text(), 100, SafeHtmlUtils.fromTrustedString("<b>Text</b>"));
		textColumn.setCell(new AbstractCell<String>() {
			@Override
		    public void render(Context context, String value, SafeHtmlBuilder sb) {
		      SafeHtml safeHtml = SafeHtmlUtils.fromTrustedString(value);
		      sb.append(safeHtml);
		    }
		});
		sourceColumn.setToolTip(SafeHtmlUtils.fromTrustedString("The source of the term"));
		textColumn.setToolTip(SafeHtmlUtils.fromTrustedString("The actual text phrase in which the term occurs in the source"));
		textColumn.setMenuDisabled(false);
		sourceColumn.setMenuDisabled(false);
		List<ColumnConfig<ExtractContext, ?>> columns = new ArrayList<ColumnConfig<ExtractContext, ?>>();
		columns.add(numberer);
		columns.add(expander);
		columns.add(sourceColumn);
		columns.add(textColumn);
		//columns.add(spellingColumn);
		ColumnModel<ExtractContext> columnModel = new ColumnModel<ExtractContext>(columns);
		grid = new Grid<ExtractContext>(store, columnModel);
		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		QuickTip quickTip = new QuickTip(grid);
		//sourceColumn.setWidth(200);
		grid.getView().setAutoExpandColumn(textColumn);
		grid.getView().setStripeRows(true);
		grid.getView().setColumnLines(true);
		grid.getView().setForceFit(true);
		grid.setBorders(false);
		grid.setAllowTextSelection(true);
		grid.setColumnReordering(true);
		/*grid.setStateful(true);
		grid.setStateId("contextsGrid");
		GridStateHandler<ExtractContext> state = new GridStateHandler<ExtractContext>(grid);
		state.loadState();*/
		
		StringFilter<ExtractContext> sourceFilter = new StringFilter<ExtractContext>(contextProperties.source());
		StringFilter<ExtractContext> textFilter = new StringFilter<ExtractContext>(contextProperties.text());
		ListStore<String> spellingStore = new ListStore<String>(new ModelKeyProvider<String>() {
			@Override
			public String getKey(String item) {
				return item;
			}
		});
		GridFilters<ExtractContext> filters = new GridFilters<ExtractContext>();
	    filters.setLocal(true);
	    filters.addFilter(sourceFilter);
	    filters.addFilter(textFilter);
	    filters.initPlugin(grid);
	    expander.initPlugin(grid);
	    numberer.initPlugin(grid);
		
		this.initWidget(grid);
		
		bindEvents();
	}
	
	public void setContexts(List<ExtractContext> contexts) {
		store.clear();
		if(contexts.isEmpty())
			store.add(new ExtractContext("nothing-found", "No match found", "", ""));
		else
			store.addAll(contexts);
		
		//bug: http://www.sencha.com/forum/showthread.php?285982-Grid-ColumnHeader-Menu-missing
		grid.getView().refresh(true);
	}

	private void bindEvents() {
		eventBus.addHandler(LoadCollectionEvent.TYPE, new LoadCollectionEvent.Handler() {
			@Override
			public void onLoad(LoadCollectionEvent event) {
				setCollection(event.getCollection());
			}
		});
		eventBus.addHandler(SelectTermEvent.TYPE, new SelectTermEvent.Handler() {
			@Override
			public void onSelect(SelectTermEvent event) {
				currentTerm = event.getTerm();
				refresh();
			}
		});
		//show would show the box not relative to this widget yet, not ready in 
		//final location yet
		this.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				showSearchingBox();
			}
		});
		this.addShowHandler(new ShowHandler() {
			@Override
			public void onShow(ShowEvent event) {
				showSearchingBox();
			}
		});
	}
	
	protected void refresh() {
		setContexts(currentTerm);
	}

	private void setContexts(String term) {
		createSearchingBox();
		 if(this.isVisible()) {
        	showSearchingBox();
        }
		contextService.getContexts(ModelController.getCollection().getId(), ModelController.getCollection().getSecret(), 
				term, new AsyncCallback<List<ExtractContext>>() {
			@Override
			public void onSuccess(List<ExtractContext> contexts) {
				setContexts(contexts);
				destroySearchingBox();
			}
			@Override
			public void onFailure(Throwable caught) {
				Alerter.showAlert("Context retrieval failed", "Context retrieval failed", caught);
				destroySearchingBox();
			}
		});
	}

	private void setCollection(Collection collection) {
		//bug: http://www.sencha.com/forum/showthread.php?285982-Grid-ColumnHeader-Menu-missing
		grid.getView().refresh(true);
	}
	
	private void createSearchingBox() {
		if(searchingBox == null) {
			searchingBox = new AutoProgressMessageBox("Progress", 
					"Searching contexts, please wait...");
			searchingBox.setProgressText("Searching...");
			searchingBox.auto();
			searchingBox.setClosable(true); // in case user figures search takes too long / some technical problem
			searchingBox.setModal(false);
		}
	}

	protected void destroySearchingBox() {
		if(searchingBox != null) {
			searchingBox.hide();
			searchingBox = null;
		}
	}

	private void showSearchingBox() {
		if(searchingBox != null) {
			searchingBox.getElement().alignTo(this.getElement(), 
	        		 new AnchorAlignment(Anchor.CENTER, Anchor.CENTER), 0, 0);
			searchingBox.show();
		}
	}
}
