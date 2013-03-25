package org.vaadin.aceeditor.client.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * 
 * {row: int, column: int}
 * 
 */
public class GwtAcePosition extends JavaScriptObject {
	protected GwtAcePosition() {
	}

	public static final native GwtAcePosition create(int row, int column) /*-{
		return {row:row, column:column};
	}-*/;

	public final native int getRow() /*-{
		return this.row;
	}-*/;

	public final native int getColumn() /*-{
		return this.column;
	}-*/;
}
