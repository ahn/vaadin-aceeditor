package org.vaadin.aceeditor.client;

import com.vaadin.shared.communication.SharedState;

@SuppressWarnings("serial")
public class SuggesterState extends SharedState {
	public boolean suggestOnDot = true;

	public boolean showDescriptions = true;

	public String suggestText = ".";

	public int popupWidth = 150;
	public String popupWidthUnit = "px";

	public int popupHeight = 200;
	public String popupHeightUnit = "px";

	public int popupDescriptionWidth = 225;
	public String popupDescriptionWidthUnit = "px";
}