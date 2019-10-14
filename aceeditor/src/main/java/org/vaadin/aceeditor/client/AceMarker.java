package org.vaadin.aceeditor.client;

import java.io.Serializable;

import org.vaadin.aceeditor.client.TransportDoc.TransportMarker;



/**
 *
 * Ace marker.
 *
 * The cssClass must be defined in some css file. Example:
 *
 * .ace_marker-layer .mymarker1 {
 *		background: red;
 *  	border-bottom: 2px solid black;
 *  	position: absolute;
 *  }
 *
 */
public class AceMarker implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 * Ace Marker type.
	 *
	 */
	public enum Type {
		line,
		text,
		cursor,
		cursorRow
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

	private final String markerId;
	private final AceRange range;
	private final OnTextChange onChange;
	private final String cssClass;
	private final Type type;
	private final boolean inFront;





	public AceMarker(final String markerId, final AceRange range, final String cssClass, final Type type, final boolean inFront, final OnTextChange onChange) {
		this.markerId = markerId;
		this.range = range.isBackwards() ? range.reversed() : range;
		this.cssClass = cssClass;
		this.type = type;
		this.inFront = inFront;
		this.onChange = onChange;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof AceMarker) {
			final AceMarker om = (AceMarker)o;
			return this.markerId.equals(om.markerId) && this.range.equals(om.range) && this.onChange.equals(om.onChange) &&
					this.cssClass.equals(om.cssClass) && this.type.equals(om.type) && this.inFront==om.inFront;
		}
		return false;
	}
	@Override
	public int hashCode() {
		return this.range.hashCode(); // ?
	}

	@Override
	public String toString() {
		return "("+this.range+";"+this.cssClass+";"+this.type+";"+this.inFront+")";
	}

	public TransportMarker asTransport() {
		final TransportMarker tm = new TransportMarker();
		tm.markerId = this.markerId;
		tm.range = this.range.asTransport();
		tm.cssClass = this.cssClass;
		tm.type = this.type.toString();
		tm.inFront = this.inFront;
		tm.onChange = this.onChange.toString();
		return tm;
	}

	public String getMarkerId() {
		return this.markerId;
	}

	public static AceMarker fromTransport(final TransportMarker im) {
		return new AceMarker(im.markerId, AceRange.fromTransport(im.range),
				im.cssClass, Type.valueOf(im.type), im.inFront, OnTextChange.valueOf(im.onChange));
	}

	public AceRange getRange() {
		return this.range;
	}

	public OnTextChange getOnChange() {
		return this.onChange;
	}

	public String getCssClass() {
		return this.cssClass;
	}

	public Type getType() {
		return this.type;
	}

	public boolean isInFront() {
		return this.inFront;
	}

	public AceMarker withNewPosition(final AceRange newRange) {
		return new AceMarker(this.markerId, newRange, this.cssClass, this.type, this.inFront, this.onChange);
	}
}
