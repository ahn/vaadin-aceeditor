package org.vaadin.aceeditor.client;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.TransportDoc.TransportMarker;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;

/**
 * 
 * Diff kinda classes to be used internally by the ace editor component,
 * for transporting between client and server, etc.
 * 
 * This may seem a bit overkill???
 * 
 */
@SuppressWarnings("serial")
public class TransportDiff implements Serializable {
	
	public static class TransportMarkerSetDiff implements Serializable {
		public Map<String, TransportMarkerAddition> added;
		public Map<String, TransportMarkerDiff> moved;
		public Set<String> removed;
		@Override
		public String toString() {
			return "added: " + added + "\n" +
					"moved: " + moved + "\n" +
					"removed: " + removed;
		}
	}
	
	public static class TransportMarkerAddition implements Serializable {
		public TransportMarkerAddition() {}
		public TransportMarkerAddition(TransportMarker marker, String startContext, String endContext) {
			this.marker = marker;
			this.startContext = startContext;
			this.endContext = endContext;
		}
		public TransportMarker marker;
		public String startContext;
		public String endContext;
	}
	
	public static class TransportMarkerDiff implements Serializable {
		public TransportRangeDiff rangeDiff;
		public TransportMarkerDiff() {}
		public TransportMarkerDiff(TransportRangeDiff rangeDiff) {
			this.rangeDiff = rangeDiff;
		}
		@Override
		public String toString() {
			return rangeDiff.toString();
		}
	}
	
	public static class TransportRangeDiff implements Serializable {
		public int drow1;
		public int dcol1;
		public int drow2;
		public int dcol2;
		public TransportRangeDiff() {}
		public TransportRangeDiff(int drow1, int dcol1, int drow2, int dcol2) {
			this.drow1 = drow1;
			this.dcol1 = dcol1;
			this.drow2 = drow2;
			this.dcol2 = dcol2;
		}
		@Override
		public String toString() {
			return "(("+drow1+","+dcol1+"), ("+drow2+","+dcol2+"))";
		}
	}
	
	public static class TransportSetDiff<V> implements Serializable {
		public Set<V> added;
		public Set<V> removed;
		public TransportSetDiff() {}
		public TransportSetDiff(Set<V> added, Set<V> removed) {
			this.added = added;
			this.removed = removed;
		}
		@Override
		public String toString() {
			return "added: " + added + ", removed: " + removed;
		}
	}

	// XXX unnecessary copy-pasting
	public static class TransportSetDiffForMarkerAnnotations implements Serializable {
		public Set<TransportMarkerAnnotation> added;
		public Set<TransportMarkerAnnotation> removed;
		public TransportSetDiffForMarkerAnnotations() {}
		public TransportSetDiffForMarkerAnnotations(Set<TransportMarkerAnnotation> added, Set<TransportMarkerAnnotation> removed) {
			this.added = added;
			this.removed = removed;
		}
		@Override
		public String toString() {
			return "added: " + added + ", removed: " + removed;
		}
	}
	
	// XXX unnecessary copy-pasting
	public static class TransportSetDiffForRowAnnotations implements Serializable {
		public Set<TransportRowAnnotation> added;
		public Set<TransportRowAnnotation> removed;
		public TransportSetDiffForRowAnnotations() {}
		public TransportSetDiffForRowAnnotations(Set<TransportRowAnnotation> added, Set<TransportRowAnnotation> removed) {
			this.added = added;
			this.removed = removed;
		}
		@Override
		public String toString() {
			return "added: " + added + ", removed: " + removed;
		}
	}
	
	public String patchesAsString;
	public TransportMarkerSetDiff markerSetDiff;
	public TransportSetDiffForRowAnnotations rowAnnDiff;
	public TransportSetDiffForMarkerAnnotations markerAnnDiff;
	
	@Override
	public String toString() {
		return "///// DIFF\n" + patchesAsString+"\n|||| Markers\n" + markerSetDiff+"\n//////\nrad:" + rowAnnDiff + ", mad:" + markerAnnDiff;
	}
}
