package edu.arizona.biosemantics.oto2.ontologize2.client;

import java.util.List;

import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.box.MessageBox;
import com.sencha.gxt.widget.core.client.box.PromptMessageBox;

import edu.arizona.biosemantics.oto2.ontologize2.shared.model.OntologyGraph.Vertex;

public class Alerter {

	public static class InfoMessageBox extends MessageBox {
		public InfoMessageBox(String title, String message) {
			super(title, message);
			setIcon(ICONS.info());
		}
	}
	
	public static MessageBox startLoading() {
		AutoProgressMessageBox box = new AutoProgressMessageBox("Loading", "Loading your data, please wait...");
        box.setProgressText("Loading...");
        box.auto();
        box.show();
        return box;
	}
	
	public static void stopLoading(MessageBox box) {
		box.hide();
	}
	
	public static MessageBox showAlert(String title, String message, Throwable caught) {
		if(caught != null)
			caught.printStackTrace();
		return showAlert(title, message);
	}
	
	public static MessageBox showAlert(String title, String message) {
		AlertMessageBox alert = new AlertMessageBox(title, message);
		alert.show();
		return alert;
	}

	public static MessageBox showInfo(String title, String message) {
		InfoMessageBox info = new InfoMessageBox(title, message);
		info.show();
		return info;
	}
	
	public static MessageBox showConfirm(String title, String message) {
		 ConfirmMessageBox confirm = new ConfirmMessageBox(title, message);
		 confirm.show();
         return confirm;
	}

	public static MessageBox showYesNoCancelConfirm(String title, String message) {
		MessageBox box = new MessageBox(title, message);
        box.setPredefinedButtons(PredefinedButton.YES, PredefinedButton.NO, PredefinedButton.CANCEL);
        box.setIcon(MessageBox.ICONS.question());
        box.show();
        return box;
	}
	
	public static String collapseTermsAsString(List<Vertex> vertices) {
		String result = "";
		int i = 0;
		for(Vertex v : vertices) {
			result += v.getValue() + ", ";
			if(i > 5)
				break;
		}
		return result.substring(0, result.length() - 2);
	}

	public static PromptMessageBox showPromptMessageBox(String title, String message) {
		PromptMessageBox box = new PromptMessageBox(title, message);
		box.show();
		return box;
	}

}
