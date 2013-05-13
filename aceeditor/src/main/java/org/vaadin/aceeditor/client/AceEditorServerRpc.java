package org.vaadin.aceeditor.client;


import org.vaadin.aceeditor.client.TransportDoc.TransportRange;

import com.vaadin.shared.annotations.Delayed;
import com.vaadin.shared.communication.ServerRpc;

public interface AceEditorServerRpc extends ServerRpc {
	
	public void changed(TransportDiff diff, TransportRange selection, boolean focused);
	
	@Delayed(lastOnly=true)
	public void changedDelayed(TransportDiff diff, TransportRange selection, boolean focused);
	
}
