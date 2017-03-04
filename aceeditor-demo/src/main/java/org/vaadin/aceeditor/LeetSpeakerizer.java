package org.vaadin.aceeditor;

import com.vaadin.data.HasValue;

@SuppressWarnings("serial")
public class LeetSpeakerizer implements HasValue.ValueChangeListener<String> {
	
	private AceEditor editor;

	public void attachTo(AceEditor editor) {
		this.editor = editor;
//		editor.addValueChangeListener(this);
		correctText(editor.getValue());
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

	@Override
	public void valueChange(HasValue.ValueChangeEvent<String> valueChangeEvent) {
		correctText(valueChangeEvent.getValue());
	}
}
