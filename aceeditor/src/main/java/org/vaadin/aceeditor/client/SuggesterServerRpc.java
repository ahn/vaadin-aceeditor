package org.vaadin.aceeditor.client;


import org.vaadin.aceeditor.client.TransportDoc.TransportRange;

import com.vaadin.shared.communication.ServerRpc;

public interface SuggesterServerRpc extends ServerRpc {
	
	// TODO: it may not be necessary to send the whole text here
	// but I guess it's simplest...
	
	public void suggest(String text, TransportRange selection);

	public void suggestionSelected(int index);
	
	
}
