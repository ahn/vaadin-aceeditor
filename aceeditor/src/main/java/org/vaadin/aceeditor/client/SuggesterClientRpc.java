package org.vaadin.aceeditor.client;

import java.util.List;

import com.vaadin.shared.communication.ClientRpc;

public interface SuggesterClientRpc extends ClientRpc {
	
	public void showSuggestions(List<TransportSuggestion> suggs);

	public void applySuggestionDiff(TransportDiff diff);
}
