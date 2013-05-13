package org.vaadin.aceeditor.client;

/**
 * A single suggestion.
 */
public class Suggestion {

	private String valueText;
	private String displayText;
	private String descriptionText;

	public Suggestion() {
	}

	/**
	 * 
	 * @param valueText ???
	 * @param displayText
	 *            the text shown to the user
	 * @param descriptionText
	 *            a longer description
	 */
	public Suggestion(String valueText, String displayText,
			String descriptionText) {
		this.valueText = valueText;
		this.displayText = displayText;
		this.descriptionText = descriptionText;
	}

	public String getValueText() {
		return valueText;
	}

	public void setValueText(String s) {
		valueText = s;
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

}
