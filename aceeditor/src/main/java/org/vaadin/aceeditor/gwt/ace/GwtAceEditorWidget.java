package org.vaadin.aceeditor.gwt.ace;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * A {@link com.google.gwt.user.client.ui.Widget} containing
 * {@link org.vaadin.aceeditor.gwt.ace.GwtAceEditor}
 */
public class GwtAceEditorWidget extends FocusWidget {

	private GwtAceEditor editor;

	private String editorId;

	private static int idCounter = 0;

	private static String nextId() {
		return "_AceEditorWidget_" + (++idCounter);
	}

	public GwtAceEditorWidget() {
		super(DOM.createDiv());
		this.editorId = nextId();
		this.setStylePrimaryName("AceEditorWidget");
	}

	public GwtAceEditor createEditor() {
		editor = GwtAceEditor.create(this.getElement(), editorId);
		return editor;
	}

	public GwtAceEditor getAceEditor() {
		return editor;
	}
}
