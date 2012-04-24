package org.vaadin.aceeditor;

import java.util.List;

import org.vaadin.aceeditor.gwt.shared.Suggestion;

/**
 * Gives {@link org.vaadin.aceeditor.gwt.shared.Suggestion Suggestions} based
 * on text and cursor position.
 * 
 */
public interface Suggester {

	/**
	 * Returns {@link org.vaadin.aceeditor.gwt.shared.Suggestion Suggestions}
	 * based on the text and cursor position.
	 * 
	 * @param text
	 * @param cursor
	 * @return suggestions
	 */
	List<Suggestion> getSuggestions(String text, int cursor);
}
