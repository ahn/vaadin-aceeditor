package org.vaadin.aceeditor.client;

import java.util.List;

import com.vaadin.shared.communication.ClientRpc;

public interface SuggesterClientRpc extends ClientRpc {

	void showSuggestions(List<TransportSuggestion> suggs);

	void applySuggestionDiff(TransportDiff diff);
}
