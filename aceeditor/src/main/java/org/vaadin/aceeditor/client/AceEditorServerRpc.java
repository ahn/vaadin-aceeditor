package org.vaadin.aceeditor.client;


import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;

public interface AceEditorServerRpc extends ServerRpc {
	
	@Delayed(lastOnly=true)
	public void changedDelayed(AceDocument doc, AceClientRange selection, boolean focus);
	
	// TODO: isn't there other way to send now, other than creating this useless method.
	public void sendNow();
	
}
