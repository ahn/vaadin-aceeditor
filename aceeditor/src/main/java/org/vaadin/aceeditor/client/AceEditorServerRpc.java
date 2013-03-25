package org.vaadin.aceeditor.client;


import java.util.List;
import java.util.Set;

import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;

public interface AceEditorServerRpc extends ServerRpc {
	
	@Delayed(lastOnly=true)
	public void changedDelayed(String text, AceClientRange selection, boolean focus);
	
	// TODO: isn't there other way to send now, other than creating this useless method.
	public void sendNow();

	@Delayed(lastOnly=true)
	public void markersChanged(List<AceMarker> markers);

	@Delayed(lastOnly=true)
	public void annotationsChanged(Set<AceClientAnnotation> markerAnnotations);
	
}
