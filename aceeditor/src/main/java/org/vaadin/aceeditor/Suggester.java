package org.vaadin.aceeditor;

import java.util.List;

import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.Suggestion;

public interface Suggester {
	List<Suggestion> getSuggestions(String text, AceRange range);
}
