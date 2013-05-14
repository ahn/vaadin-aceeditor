package org.vaadin.aceeditor;

import org.vaadin.aceeditor.client.TransportSuggestion;

/**
 * A single suggestion.
 */
public class Suggestion {

	private String displayText;
	private String descriptionText;
	private String suggestionText;

	public Suggestion() {
	}

	/**
	 * 
	 * If suggestionText is "cat", the suggestion popup will stay there
	 * if user types "c" "ca" or "cat".
	 * 
	 * @param displayText
	 *            the text shown to the user
	 * @param descriptionText
	 *            a longer description
	 * @param suggestionText
	 */
	public Suggestion(String displayText,
			String descriptionText, String suggestionText) {
		this.displayText = displayText;
		this.descriptionText = descriptionText;
		this.suggestionText = suggestionText;
	}
	
	/**
	 * 
	 * @param displayText
	 *            the text shown to the user
	 * @param descriptionText
	 *            a longer description
	 */
	public Suggestion(String displayText,
			String descriptionText) {
		this(displayText, descriptionText, "");
	}

	public String getSuggestionText() {
		return suggestionText;
	}

	public void setSuggestionText(String s) {
		suggestionText = s;
	}

	public String getDisplayText() {
		return displayText;
	}

	public void setDisplayText(String s) {
		displayText = s;
	}

	public String getDescriptionText() {
		return descriptionText;
	}

	public void setDescriptionText(String s) {
		descriptionText = s;
	}
	
	public TransportSuggestion asTransport(int index) {
		TransportSuggestion ts = new TransportSuggestion();
		ts.displayText = displayText;
		ts.descriptionText = descriptionText;
		ts.suggestionText = suggestionText;
		ts.index = index;
		return ts;
	}
	
}
