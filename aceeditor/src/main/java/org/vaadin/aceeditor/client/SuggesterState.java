package org.vaadin.aceeditor.client;

import com.vaadin.shared.communication.SharedState;

@SuppressWarnings("serial")
public class SuggesterState extends SharedState {
	public boolean suggestOnDot = true;
	// TODO?
    public boolean showDescriptions = true;
	public int suggestPopupWidth = 150;
}