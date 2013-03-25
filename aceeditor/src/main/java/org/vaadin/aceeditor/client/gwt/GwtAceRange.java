package org.vaadin.aceeditor.client.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * 
 * {start: {@link GwtAcePosition}, end: {@link GwtAcePosition}
 * 
 */
public class GwtAceRange extends JavaScriptObject {
	protected GwtAceRange() {
	}

	public final static native GwtAceRange create(int startRow, int startCol,
			int endRow, int endCol) /*-{
		var Range = $wnd.ace.require("ace/range").Range;
		return new Range(startRow,startCol,endRow,endCol);
	}-*/;

	public final native GwtAcePosition getStart() /*-{
		return this.start;
	}-*/;

	public final native GwtAcePosition getEnd() /*-{
		return this.end;
	}-*/;

}
