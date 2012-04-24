package org.vaadin.aceeditor;

import java.util.Collection;

import org.vaadin.aceeditor.gwt.shared.Marker;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

@SuppressWarnings("serial")
public class ErrorCheckListener implements TextChangeListener {
	private final AceMarkerEditor editor;
	private final ErrorChecker checker;

	public ErrorCheckListener(AceMarkerEditor editor, ErrorChecker checker) {
		this.editor = editor;
		this.checker = checker;
	}

//	@Override
	public void textChange(TextChangeEvent event) {
		checkErrors(event.getText());
		
	}

	public void checkErrors(String text) {
		Collection<Marker> errors = checker.getErrors(text);
		editor.clearMarkersOfType(Marker.Type.ERROR);
		for (Marker m : errors) {
			editor.addMarker(m);
		}
	}

}