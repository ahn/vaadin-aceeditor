package org.vaadin.aceeditor.client;

import java.io.Serializable;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.client.TransportDoc.TransportAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportableAs;

/**
 * An annotation for {@link AceEditor}.
 *
 */
public class AceAnnotation implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		error,
		warning,
		info
	}

	private final String message;
	private final Type type;

	public AceAnnotation(final String message, final Type type) {
		this.message = message;
		this.type = type;
	}

	public String getMessage() {
		return this.message;
	}

	public Type getType() {
		return this.type;
	}

	public TransportAnnotation asTransport() {
		return new TransportAnnotation(this.message, this.type);
	}

	public static AceAnnotation fromTransport(final TransportAnnotation ta) {
		return new AceAnnotation(ta.message, ta.type);
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof AceAnnotation) {
			final AceAnnotation oa = (AceAnnotation)o;
			return this.type.equals(oa.type) && this.message.equals(oa.message);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.message.hashCode(); // ?
	}

	@Override
	public String toString() {
		return "<"+this.message+", "+this.type+">";
	}


	public static class MarkerAnnotation implements TransportableAs<TransportMarkerAnnotation>, Serializable {
		private static final long serialVersionUID = 1L;

		private final String markerId;
		private final AceAnnotation ann;
		public MarkerAnnotation(final String markerId, final AceAnnotation ann) {
			this.markerId = markerId;
			this.ann = ann;
		}
		public String getMarkerId() {
			return this.markerId;
		}
		public AceAnnotation getAnnotation() {
			return this.ann;
		}
		@Override
		public boolean equals(final Object o) {
			if (o instanceof MarkerAnnotation) {
				final MarkerAnnotation oma = (MarkerAnnotation)o;
				return this.markerId.equals(oma.markerId) && this.ann.equals(oma.ann);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return this.markerId.hashCode(); // ?
		}

		@Override
		public TransportMarkerAnnotation asTransport() {
			return new TransportMarkerAnnotation(this.markerId, this.ann.asTransport());
		}

		@Override
		public String toString() {
			return this.markerId+": " + this.ann;
		}
	}

	public static class RowAnnotation implements TransportableAs<TransportRowAnnotation>, Serializable {
		private static final long serialVersionUID = 1L;

		private final int row;
		private final AceAnnotation ann;
		public RowAnnotation(final int row, final AceAnnotation ann) {
			this.row = row;
			this.ann = ann;
		}
		public int getRow() {
			return this.row;
		}
		public AceAnnotation getAnnotation() {
			return this.ann;
		}
		@Override
		public boolean equals(final Object o) {
			if (o instanceof RowAnnotation) {
				final RowAnnotation ora = (RowAnnotation)o;
				return this.row==ora.row && this.ann.equals(ora.ann);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return this.row; // ?
		}
		@Override
		public TransportRowAnnotation asTransport() {
			return new TransportRowAnnotation(this.row, this.ann.asTransport());
		}
		@Override
		public String toString() {
			return this.row+": " + this.ann;
		}
	}
}
