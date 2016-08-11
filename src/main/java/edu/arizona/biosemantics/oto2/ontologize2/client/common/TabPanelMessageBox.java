package edu.arizona.biosemantics.oto2.ontologize2.client.common;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.TextArea;

public class TabPanelMessageBox extends Dialog {

	private TabPanel tabPanel;

	public TabPanelMessageBox(String title, String message) {
		super();
		tabPanel = new TabPanel();
		tabPanel.setTabScroll(true);
		tabPanel.setAnimScroll(true);
		this.setWidget(tabPanel);
		this.setSize("600", "400");
		this.setMaximizable(true);
	}
	
	public void addTab(String name, Widget widget) {
		tabPanel.add(widget, new TabItemConfig(name, false));
	}
}
