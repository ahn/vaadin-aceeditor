package org.vaadin.aceeditor.client.gwt;


public class GwtAceKeyboardEvent extends GwtAceEvent {
	
	protected GwtAceKeyboardEvent() {}
	
	public final native boolean isCtrlKey() /*-{
		return !!this.ctrlKey;
	}-*/;
	
	public final native boolean isAltKey() /*-{
		return !!this.altKey;
	}-*/;
	
	public final native boolean isShiftKey() /*-{
		return !!this.shiftKey;
	}-*/;
	
	public final native boolean getKeyCode() /*-{
		return this.keyCode;
	}-*/;

}
