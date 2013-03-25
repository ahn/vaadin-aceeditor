package org.vaadin.aceeditor.client.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A text change event received from Ace.
 * 
 */
public class GwtAceChangeEvent extends GwtAceEvent {
	protected GwtAceChangeEvent() {
	}

	public final native Data getData() /*-{
										return this.data;
										}-*/;

	public static class Data extends JavaScriptObject {
		protected Data() {
		}

		public enum Action {
			insertText, insertLines, removeText, removeLines
		}

		public final Action getAction() {
			return Action.valueOf(getActionString());
		}

		private final native String getActionString() /*-{
			return this.action;
		}-*/;

		public final native GwtAceRange getRange() /*-{
			return this.range;
		}-*/;

		public final native String getText() /*-{
			return this.text;
		}-*/;
	}

}
