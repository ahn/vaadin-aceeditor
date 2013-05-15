package org.vaadin.aceeditor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vaadin.aceeditor.client.AceAnnotation;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceRange;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

@SuppressWarnings("serial")
public class MyErrorChecker implements TextChangeListener {
	
	private AceEditor editor;
	
	private Pattern pattern = Pattern.compile("[Xx]+");
	
	private long latestErrorMarkerId = 0L;
	private String newErrorMarkerId() {
		return "e"+(++latestErrorMarkerId);
	}
	
	public MyErrorChecker() {
		
	}
	
	public void attachTo(AceEditor editor) {
		this.editor = editor;
		editor.addTextChangeListener(this);
		checkErrors(editor.getValue());
	}

	@Override
	public void textChange(TextChangeEvent event) {
		checkErrors(event.getText());
	}
	
	private void checkErrors(String text) {
		editor.clearMarkerAnnotations();
		editor.clearMarkers();
		
		Matcher matcher = pattern.matcher(text);
		int i = 0;
		while (i < text.length() && matcher.find(i)) {
			i = matcher.end() + 1;
			AceRange range = AceRange.fromPositions(matcher.start(), matcher.end(), text);
			AceMarker m = new AceMarker(newErrorMarkerId(), range, "myerrormarker1", AceMarker.Type.text, false, AceMarker.OnTextChange.ADJUST);
			editor.addMarker(m);
			
			AceAnnotation ann = new AceAnnotation("X's not allowed here! ("+matcher.group()+")", AceAnnotation.Type.error);
			editor.addMarkerAnnotation(ann, m);
		}
		
	}
}
