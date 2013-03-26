package org.vaadin.aceeditor.client;

import java.io.Serializable;


@SuppressWarnings("serial")
public class AceClientMarker implements Serializable {
	
	public enum Type {
		line,
		text
	}
	
	/**
	 * What to do with the marker when the text changes.
	 * 
	 * By default, Ace just keeps the marker in its place (DEFAULT).
	 * Alternatively, you can set the marker to ADJUST to text insertion/deletion.
	 * Or, you can set the marker to be REMOVE'd on text change.
	 *
	 */
	public enum OnTextChange {
		/**
		 * Keep the marker in its place.
		 */
		DEFAULT,
		/**
		 * Adjust the marker based on text insertion/deletion.
		 */
		ADJUST,
		/**
		 * Remove the marker when text changes.
		 */
		REMOVE
	}
	
	public long serverId = -1;
	public AceClientRange range;
	public OnTextChange onChange;
	public String cssClass;
	public Type type;
	public boolean inFront = false;
	
	public AceClientMarker() {
		
	}
	
	
	
	@Override
	public String toString() {
		return "("+range+";"+cssClass+";"+type+";"+inFront+")";
	}
	
}
