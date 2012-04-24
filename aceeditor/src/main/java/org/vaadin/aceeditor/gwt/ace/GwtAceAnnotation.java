package org.vaadin.aceeditor.gwt.ace;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * An annotation shown at the Ace editor.
 * 
 */
public class GwtAceAnnotation extends JavaScriptObject {
	protected GwtAceAnnotation() {
	}

	/**
	 * Eg. create("error", "An error on line 5", 4);
	 * 
	 * @param type
	 * @param text
	 * @param row
	 * @return the annotation
	 */
	public static final native GwtAceAnnotation create(String type, String text,
			int row) /*-{
		return {
			text: text,
			row: row,
			type: type
		};
	}-*/;

	public static final native JsArray<GwtAceAnnotation> createEmptyArray() /*-{
		return [];
	}-*/;

	public final native void setRow(int row) /*-{
		this.row = row;
	}-*/;
}
