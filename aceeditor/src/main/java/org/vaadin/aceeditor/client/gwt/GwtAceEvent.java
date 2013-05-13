package org.vaadin.aceeditor.client.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * An event received from Ace.
 * 
 * @see GwtAceChangeEvent
 * 
 */
public class GwtAceEvent extends JavaScriptObject {

	protected GwtAceEvent() {
	}

	enum Type {
		change, changeCursor, changeSelection, keydown // TODO?
	}

	final public Type getType() {
		return Type.valueOf(getTypeString());
	}

	private final native String getTypeString() /*-{
		return this.type;
	}-*/;

	public final native void preventDefault() /*-{
		this.preventDefault();
	}-*/;

	public final native void stopPropagation() /*-{
		this.stopPropagation();
	}-*/;

}
