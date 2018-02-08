package org.vaadin.aceeditor;

import org.vaadin.aceeditor.client.TransportSuggestion;

/**
 * A single suggestion.
 *
 * Feel free to subclass.
 */
public class Suggestion {

	private final String group;
	private final String displayText;
	private final String descriptionText;
	private final String suggestionText;
	private final Integer selectionStart;
	private final Integer selectionEnd;

	/**
	 *
	 * @param displayText
	 *            the text shown in the popup list
	 * @param descriptionText
	 *            a longer description
	 */
	public Suggestion(final String displayText,
			final String descriptionText) {
		this("", displayText, descriptionText, "", null, null);
	}
	/**
	 *
	 * If suggestionText is "cat", the suggestion popup will stay there
	 * if user types "c" "ca" or "cat".
	 *
	 * @param displayText
	 *            the text shown in the popup list
	 * @param descriptionText
	 *            a longer description
	 * @param suggestionText
	 */
	public Suggestion(final String group, final String displayText,
			final String descriptionText, final String suggestionText) {
		this(group, displayText, descriptionText, suggestionText, null, null);
	}

	/**
	 *
	 * If suggestionText is "cat", the suggestion popup will stay there
	 * if user types "c" "ca" or "cat".
	 *
	 * @param displayText
	 *            the text shown in the popup list
	 * @param descriptionText
	 *            a longer description
	 * @param suggestionText
	 */
	public Suggestion(final String group, final String displayText,
			final String descriptionText, final String suggestionText, final Integer selectionStart, final Integer selectionEnd) {
		this.group = group == null || group.trim().equals("") ? "" : group;
		this.displayText = displayText;
		this.descriptionText = descriptionText;
		this.suggestionText = suggestionText;
		this.selectionStart = selectionStart;
		this.selectionEnd = selectionEnd;
	}

	public TransportSuggestion asTransport(final int index) {
		final TransportSuggestion ts = new TransportSuggestion();
		ts.displayText = this.displayText;
		ts.descriptionText = this.descriptionText;
		ts.suggestionText = this.suggestionText;
		ts.group = this.group;
		ts.selectionStart = this.selectionStart;
		ts.selectionEnd = this.selectionEnd;
		ts.index = index;
		return ts;
	}

	public String getDisplayText() {
		return this.displayText;
	}

	public String getDescriptionText() {
		return this.descriptionText;
	}

	public String getSuggestionText() {
		return this.suggestionText;
	}

	public String getGroup() {
		return this.group;
	}

	public Integer getSelectionStart() {
		return this.selectionStart;
	}

	public Integer getSelectionEnd() {
		return this.selectionEnd;
	}
}
