package org.vaadin.aceeditor.gwt.shared;

/**
 * A single suggestion.
 */
public class Suggestion {

	private final String valueText;
	private final String displayText;
	private final String descriptionText;
	private final int selStart;
	private final int selEnd;

	/**
	 * 
	 * @param valueText
	 *            the to be added to the text
	 */
	public Suggestion(String valueText) {
		this(valueText, valueText, valueText, valueText.length(), valueText
				.length());
	}

	/**
	 * 
	 * @param valueText
	 *            the to be added to the text
	 * @param displayText
	 *            the text shown to the user
	 */
	public Suggestion(String valueText, String displayText) {
		this(valueText, displayText, displayText, valueText.length(), valueText
				.length());
	}

	/**
	 * 
	 * @param valueText
	 *            the to be added to the text
	 * @param displayText
	 *            the text shown to the user
	 * @param descriptionText
	 *            a longer description
	 */
	public Suggestion(String valueText, String displayText,
			String descriptionText) {
		this(valueText, displayText, descriptionText, valueText.length(),
				valueText.length());
	}

	/**
	 * 
	 * @param valueText
	 *            the to be added to the text
	 * @param displayText
	 *            the text shown to the user
	 * @param descriptionText
	 *            a longer description
	 * @param selStart
	 *            selection start after applying suggestions. RELATIVE TO CURSOR
	 *            POSITION!
	 * @param selEnd
	 *            selection start after applying suggestions. RELATIVE TO CURSOR
	 *            POSITION!
	 */
	public Suggestion(String valueText, String displayText,
			String descriptionText, int selStart, int selEnd) {
		this.displayText = displayText;
		this.valueText = valueText;
		this.descriptionText = descriptionText;
		this.selStart = selStart;
		this.selEnd = selEnd;
	}

	public String getDescriptionText() {
		return descriptionText;
	}

	public String getDisplayText() {
		return displayText;
	}

	public String getValueText() {
		return valueText;
	}

	public int getSelectionStart() {
		return selStart;
	}

	public int getSelectionEnd() {
		return selEnd;
	}

}