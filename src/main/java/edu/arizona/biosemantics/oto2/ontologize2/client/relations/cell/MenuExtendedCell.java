package edu.arizona.biosemantics.oto2.ontologize2.client.relations.cell;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.Style.Anchor;
import com.sencha.gxt.core.client.Style.AnchorAlignment;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader.ColumnHeaderAppearance;
import com.sencha.gxt.widget.core.client.grid.ColumnHeader.ColumnHeaderStyles;
import com.sencha.gxt.widget.core.client.grid.GridView.GridAppearance;
import com.sencha.gxt.widget.core.client.grid.GridView.GridStyles;
import com.sencha.gxt.widget.core.client.menu.Menu;

import edu.arizona.biosemantics.oto2.ontologize2.client.Alerter;

public abstract class MenuExtendedCell<C> extends AbstractCell<C> {

	protected ColumnHeaderAppearance columnHeaderAppearance;
	protected GridAppearance gridAppearance;
	protected ColumnHeaderStyles columnHeaderStyles;
	protected GridStyles gridStyles;

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div class=\"{0}\" qtip=\"{4}\">" +
				"<div class=\"{1}\" style=\"width: calc(100% - 9px); height:14px\">{3}" +
				"<a class=\"{2}\" style=\"height: 22px;\"></a>" +
				"</div>" +
				"</div>")
		SafeHtml cell(String grandParentStyleClass, String parentStyleClass,
				String aStyleClass, String value, String quickTipText);
	}

	protected static Templates templates = GWT.create(Templates.class);


	public MenuExtendedCell() {
		this(GWT.<ColumnHeaderAppearance> create(ColumnHeaderAppearance.class), GWT.<GridAppearance> create(GridAppearance.class));
	}

	public MenuExtendedCell(ColumnHeaderAppearance columnHeaderAppearance, GridAppearance gridAppearance) {
		super(BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT, BrowserEvents.CLICK);
		
		this.columnHeaderAppearance = columnHeaderAppearance;
		this.gridAppearance = gridAppearance;
		columnHeaderStyles = columnHeaderAppearance.styles();
		gridStyles = gridAppearance.styles();
		
		/*
		System.out.println(styles.headOver());
		System.out.println(styles.columnMoveBottom());
		System.out.println(styles.columnMoveTop());
		System.out.println(styles.head());
		System.out.println(styles.headButton());
		System.out.println(styles.header());
		System.out.println(styles.headInner());
		System.out.println(styles.headMenuOpen());
		System.out.println(styles.headOver());
		System.out.println(styles.headRow());
		System.out.println(styles.sortAsc());
		System.out.println(styles.sortDesc());
		System.out.println(styles.sortIcon());
		System.out.println(styles.headerInner());
		*/
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, C value,
			NativeEvent event, ValueUpdater<C> valueUpdater) {
		// event.preventDefault();
		// event.stopPropagation();
		super.onBrowserEvent(context, parent, value, event, valueUpdater);
		
		//A is the link used for menu; parent is parent of event
		com.google.gwt.user.client.Element aGrandParent = null;
		com.google.gwt.user.client.Element aParent = null;
		if(parent.getChildCount() > 0) { 
			//client.Element is a newer version of dom.Element. It actually only extends it if you look in source
			//http://stackoverflow.com/questions/9024548/gwt-why-is-there-two-element-types
			aGrandParent = (com.google.gwt.user.client.Element)parent.getChild(0);
			if(aGrandParent.getChildCount() > 0) {
				aParent = (com.google.gwt.user.client.Element)aGrandParent.getChild(0);
			} else {
				//System.out.println("no parent " + parent);
			}
		} else {
			//System.out.println("no grand parent " + parent);
		}
		
		if(aParent != null && aGrandParent != null) {
			if(event.getType().equals(BrowserEvents.MOUSEOVER)) {	
				aGrandParent.addClassName(columnHeaderStyles.headOver());
				//aGrandParent.getStyle().setRight(XDOM.getScrollBarWidth(), Unit.PX);
				//aParent.addClassName(styles.headInner());
			}
			if(event.getType().equals(BrowserEvents.MOUSEOUT)) {
				if(!aGrandParent.hasClassName(columnHeaderStyles.headMenuOpen())) {
					aGrandParent.removeClassName(columnHeaderStyles.headOver());
				}
			}
			if (event.getType().equals(BrowserEvents.CLICK)) {
				if (Element.is(event.getEventTarget())) {
					Element clickedElement = Element.as(event.getEventTarget());
					if(clickedElement.getClassName().equals(columnHeaderStyles.headButton())) {
						aGrandParent.addClassName(columnHeaderStyles.headMenuOpen());
						this.showMenu(clickedElement, context.getColumn(), context.getIndex());
					}
			}
			}
		}
	}
	
	public void showMenu(final Element parent, final int column, final int row) {
		Menu menu = createContextMenu(column, row);
		if (menu != null) {
			//menu.setId("cell" + column + "." + row + "-menu");
			//menu.setStateful(false);
			menu.addHideHandler(new HideHandler() {
				@Override
				public void onHide(HideEvent event) {
					Element a = parent;
					Element aGrandParent = a.getParentElement().getParentElement();
					aGrandParent.removeClassName(columnHeaderStyles.headMenuOpen());
					aGrandParent.removeClassName(columnHeaderStyles.headOver());
					//h.activateTrigger(false);
					//if (container instanceof Component) {
					//	((Component) container).focus();
					//}
				}
			});
			menu.show(parent, new AnchorAlignment(Anchor.TOP_LEFT,
					Anchor.BOTTOM_LEFT, true));
		}
	}

	protected abstract Menu createContextMenu(int column, int row);
}