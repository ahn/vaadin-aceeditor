package org.vaadin.aceeditor.client;

import com.vaadin.shared.communication.SharedState;

@SuppressWarnings("serial")
public class SuggesterState extends SharedState {
	public boolean suggestOnDot = true;
	// TODO?
    public boolean showDescriptions = true;
    
    public int popupWidth = 150;
    
    public int popupHeight = 200;
    
    public int popupDescriptionWidth = 225;
}