package org.vaadin.aceeditor.client;

import com.vaadin.shared.communication.ClientRpc;

public interface AceEditorClientRpc extends ClientRpc {
	public void diff(TransportDiff diff);
	
	/**
	 * Notifies the client that the server-side value has changed.
	 * 
	 * We always start the "diff roundtrip" from client to server.
	 * I guess that's simplest that way (?)
	 * 
	 */
	public void changedOnServer();
}
