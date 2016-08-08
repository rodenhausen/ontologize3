package edu.arizona.biosemantics.oto2.ontologize2.client.common;

import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.form.TextArea;

public class TextAreaMessageBox extends Dialog {

	private Label messageLabel;
	private TextArea textArea;

	public TextAreaMessageBox(String title, String message) {
		super();
		messageLabel = new Label(message);
		textArea = new TextArea();
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		vlc.add(messageLabel, new VerticalLayoutContainer.VerticalLayoutData(1, -1));
		vlc.add(textArea, new VerticalLayoutContainer.VerticalLayoutData(1, 1));
		this.setWidget(vlc);
		this.setSize("600", "400");
		this.setMaximizable(true);
	}
	
	public TextArea getTextArea() {
		return textArea;
	}

	public String getValue() {
		return textArea.getText();
	}
}
