package org.vaadin.aceeditor.client;

import java.util.HashMap;
import java.util.Map;

import org.vaadin.aceeditor.client.TransportDoc.TransportRange;

import com.vaadin.shared.AbstractFieldState;
import com.vaadin.shared.annotations.DelegateToWidget;

@SuppressWarnings("serial")
public class AceEditorState extends AbstractFieldState {
	
	public String changeMode = "LAZY";
	public int changeTimeout = 400;
	
	@DelegateToWidget("setMode")
	public String mode = "text";
	
	@DelegateToWidget("setTheme")
	public String theme = "textmate";
	
	public TransportRange selection = null;
	
	public boolean listenToSelectionChanges = false;
	
	@DelegateToWidget("setUseWorker")
	public boolean useWorker = true;
	
	@DelegateToWidget("setWordwrap")
	public boolean wordwrap = false;
	
	public Map<String,String> config = new HashMap<String,String>();
	
	public int diff_editCost = 4;
	
	public TransportDoc initialValue = null;
	
	public int scrollToRow = -1;
	
}
