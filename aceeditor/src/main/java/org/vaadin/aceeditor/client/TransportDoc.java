package org.vaadin.aceeditor.client;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;

/**
 *
 * Classes to be used internally by the ace editor component,
 * for transporting between client and server, etc.
 *
 * This may seem a bit overkill???
 *
 */
@SuppressWarnings("serial")
public class TransportDoc implements Serializable {

	public interface TransportableAs<T> {
		public T asTransport();
	}

	public interface TransportableOf<T> extends Serializable {
		public T fromTransport();
	}

	public static class TransportMarker implements Serializable {
		public String markerId = null;
		public TransportRange range;
		public String onChange;
		public String cssClass;
		public String type;
		public boolean inFront = false;
		@Override
		public boolean equals(final Object o) {
			if (o instanceof TransportMarker) {
				final TransportMarker om = (TransportMarker)o;
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
			return "((Marker " + this.markerId + " at "+this.range+", " + this.cssClass + "))";
		}
	}

	public static class TransportRange implements Serializable {
		public int row1;
		public int col1;
		public int row2;
		public int col2;
		public TransportRange() {}
		public TransportRange(final int row1, final int col1, final int row2, final int col2) {
			this.row1 = row1;
			this.col1 = col1;
			this.row2 = row2;
			this.col2 = col2;
		}
		@Override
		public String toString() {
			return "[(" + this.row1+","+this.col1+")-(" + this.row2+","+this.col2+")]";
		}
	}

	public static class TransportAnnotation implements Serializable {
		public String message;
		public AceAnnotation.Type type;
		public TransportAnnotation() {}
		public TransportAnnotation(final String message, final AceAnnotation.Type type) {
			this.message = message;
			this.type = type;
		}
	}

	public static class TransportRowAnnotation implements TransportableOf<RowAnnotation> {
		public int row;
		public TransportAnnotation ann;
		public TransportRowAnnotation() {}
		public TransportRowAnnotation(final int row, final TransportAnnotation ann) {
			this.row = row;
			this.ann = ann;
		}
		@Override
		public RowAnnotation fromTransport() {
			return new RowAnnotation(this.row, AceAnnotation.fromTransport(this.ann));
		}
	}

	public static class TransportMarkerAnnotation implements TransportableOf<MarkerAnnotation> {
		public String markerId;
		public TransportAnnotation ann;
		public TransportMarkerAnnotation() {}
		public TransportMarkerAnnotation(final String markerId, final TransportAnnotation ann) {
			this.markerId = markerId;
			this.ann = ann;
		}
		@Override
		public MarkerAnnotation fromTransport() {
			return new MarkerAnnotation(this.markerId, AceAnnotation.fromTransport(this.ann));
		}
	}



	public String text;
	public Map<String, TransportMarker> markers;
	public Set<TransportRowAnnotation> rowAnnotations;
	public Set<TransportMarkerAnnotation> markerAnnotations;

	@Override
	public String toString() {
		return "doc text >>>>>>>>>>>\n"+this.text+"\n/////////////////\n"+this.markers+"\n<<<<<<<<<<<<<<<";
	}

}
