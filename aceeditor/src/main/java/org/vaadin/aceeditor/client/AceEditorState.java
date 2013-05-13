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
	
	/**
	 * 0 if no selection change on server
	 * > 0 if selection changed on server and should be set on client
	 * 
	 * int instead of boolean because we need to make sure that the value
	 * changes (increments), and thus the client receives the changed value.
	 * If selectionFromServer "changed" from true to true, that might
	 * not be the case.
	 */
	public int selectionFromServer = 0;
	
	public boolean listenToSelectionChanges = false;
	
	@DelegateToWidget("setUseWorker")
	public boolean useWorker = true;
	
	@DelegateToWidget("setWordwrap")
	public boolean wordwrap = false;
	
	public Map<String,String> config = new HashMap<String,String>();
	
	
	public TransportDoc initialValue = null;
	
}
