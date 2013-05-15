package org.vaadin.aceeditor;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

@SuppressWarnings("serial")
public class LeetSpeakerizer implements TextChangeListener {
	
	private AceEditor editor;

	public void attachTo(AceEditor editor) {
		this.editor = editor;
		editor.addTextChangeListener(this);
		correctText(editor.getValue());
	}

	@Override
	public void textChange(TextChangeEvent event) {
		correctText(event.getText());
	}
	
	private void correctText(String text) {
		String text2 = text
				.replaceAll("l", "1")
				.replaceAll("e", "3")
				.replaceAll("k", "|<")
				.replaceAll("a", "4")
				.replaceAll("o", "0")
				.replaceAll("t", "7");
		editor.setValue(text2);
	}
}
