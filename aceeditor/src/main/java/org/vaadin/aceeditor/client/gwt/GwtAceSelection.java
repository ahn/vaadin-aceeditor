package org.vaadin.aceeditor.client.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * 
 * { range: {@link GwtAceRange}, isBackwards: function }
 * 
 */
public class GwtAceSelection extends JavaScriptObject {
	protected GwtAceSelection() {
	}

	public final native GwtAceRange getRange() /*-{
		return this.getRange();
	}-*/;

	public final native boolean isBackwards() /*-{
		return this.isBackwards();
	}-*/;

}
