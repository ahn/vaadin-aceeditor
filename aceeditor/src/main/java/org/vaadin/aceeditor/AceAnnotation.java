package org.vaadin.aceeditor;

/**
 * An annotation for {@link AceEditor}.
 *
 */
public class AceAnnotation {
	
	public enum Type {
		error,
		warning,
		info
	}
	
	private final String message;
	private final Type type;
	
	public AceAnnotation(String message, Type type) {
		this.message = message;
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public Type getType() {
		return type;
	}
}
