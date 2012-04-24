package org.vaadin.aceeditor;

import java.util.Collection;

import org.vaadin.aceeditor.gwt.shared.Marker;

/**
 * Error checker returns the errors in the source code.
 * 
 */
public interface ErrorChecker {

	/**
	 * Returns Errors in the source code.
	 * 
	 * The returned errors are {@link org.vaadin.aceeditor.gwt.shared.Marker
	 * Markers} with {@link org.vaadin.aceeditor.gwt.shared.Marker.Type#ERROR
	 * Type.ERROR}.
	 * 
	 * @param source
	 * @return errors as {@link org.vaadin.aceeditor.gwt.shared.Marker Markers}
	 */
	Collection<Marker> getErrors(String source);
}
