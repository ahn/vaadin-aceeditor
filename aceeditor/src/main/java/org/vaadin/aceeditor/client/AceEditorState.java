package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.shared.AbstractFieldState;
import com.vaadin.shared.annotations.DelegateToWidget;

@SuppressWarnings("serial")
public class AceEditorState extends AbstractFieldState {
	
	public String changeMode = "LAZY";
	public int changeTimeout = 400;
	
	@DelegateToWidget("setText")
	public String text = "";
	
	@DelegateToWidget("setMode")
	public String mode = "text";
	
	@DelegateToWidget("setTheme")
	public String theme = "textmate";
	
	@DelegateToWidget("setMarkers")
	public List<AceClientMarker> markers = Collections.emptyList();
	
	@DelegateToWidget("setRowAnnotations")
	public Set<AceClientAnnotation> rowAnnotations = null;
	
	@DelegateToWidget("setMarkerAnnotations")
	public Set<AceClientAnnotation> markerAnnotations = null;
	
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
