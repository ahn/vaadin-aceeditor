package org.vaadin.aceeditor.client.gwt;

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
			type: type,
			isVaadinAceEditorAnnotation: true
		};
	}-*/;

	public static final native JsArray<GwtAceAnnotation> createEmptyArray() /*-{
		return [];
	}-*/;
	
	public final native String getText() /*-{
		return this.text;
	}-*/;
	
	public final native int getRow() /*-{
		return this.row;
	}-*/;
	
	public final native String getType() /*-{
		return this.type;
	}-*/;

	public final native boolean isVaadinAceEditorAnnotation() /*-{
		return !!this.isVaadinAceEditorAnnotation;
	}-*/;
}
