package org.vaadin.aceeditor.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AceClientAnnotation implements Serializable {
	
	public enum Type {
		error,
		warning,
		info
	}
	
	public String text;
	public Type type;
	public int row;
	public long markerId;
	
	public AceClientAnnotation() {
		
	}
	
	public AceClientAnnotation(String text, Type type, int row) {
		this.text = text;
		this.type = type;
		this.row = row;
	}
	
	static public AceClientAnnotation annotationForMarker(String text, Type type, long markerId) {
		AceClientAnnotation ann = new AceClientAnnotation();
		ann.text = text;
		ann.type = type;
		ann.markerId = markerId;
		return ann;
	}
	
	@Override
	public String toString() {
		return "["+type+", '"+text+"' "+row+", "+markerId+"]";
	}
}
