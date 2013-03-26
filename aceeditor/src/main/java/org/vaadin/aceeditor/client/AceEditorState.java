package org.vaadin.aceeditor.client;

import java.util.HashMap;
import java.util.Map;

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
	
	public AceDocument document = new AceDocument();
	
	@DelegateToWidget("setListenToSelectionChanges")
	public boolean listenToSelectionChanges = false;
	
	@DelegateToWidget("setSelection")
	public AceClientRange selection = new AceClientRange(0,0,0,0);
	
	@DelegateToWidget("setUseWorker")
	public boolean useWorker = true;
	
	@DelegateToWidget("setWordwrap")
	public boolean wordwrap = false;
	
	public Map<String,String> config = new HashMap<String,String>();
	
	
}
