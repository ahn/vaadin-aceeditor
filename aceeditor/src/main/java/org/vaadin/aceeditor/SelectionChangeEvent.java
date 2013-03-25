package org.vaadin.aceeditor;

import com.vaadin.ui.Component.Event;

@SuppressWarnings("serial")
public class SelectionChangeEvent extends Event {
	public static String EVENT_ID = "aceeditor-selection";
	private final AceRange selection;

	public SelectionChangeEvent(AceEditor ed) {
		super(ed);
		this.selection = ed.getSelection();
	}

	public AceRange getSelection() {
		return selection;
	}
}