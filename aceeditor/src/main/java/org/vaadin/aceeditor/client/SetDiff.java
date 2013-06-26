package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.TransportDiff.TransportSetDiffForMarkerAnnotations;
import org.vaadin.aceeditor.client.TransportDiff.TransportSetDiffForRowAnnotations;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportableAs;

public class SetDiff<V extends TransportableAs<T>,T> {
	
	private final Set<V> added;
	private final Set<V> removed;

	public SetDiff(Set<V> added, Set<V> removed) {
		this.added = added;
		this.removed = removed;
	}

	public SetDiff() {
		added = Collections.emptySet();
		removed = Collections.emptySet();
	}

	public static class Differ<V extends TransportableAs<T>,T> {
		public SetDiff<V,T> diff(Set<V> s1, Set<V> s2) {
			Set<V> removed = new HashSet<V>(s1);
			removed.removeAll(s2);
			
			Set<V> added = new HashSet<V>(s2);
			added.removeAll(s1);
			return new SetDiff<V,T>(added, removed);
		}
		
//		public SetDiff<V,T> fromTransport(TransportSetDiff<T> tsd) {
//			Set<V> added = new HashSet<V>();
//			for (T t : tsd.added) {
//				added.add(t.fromTransport());
//			}
//			Set<V> removed = new HashSet<V>();
//			for (T t : tsd.removed) {
//				removed.add(t.fromTransport());
//			}
//			return new SetDiff<V,T>(added, removed);
//		}
		

	}
	
	// XXX Unnecessary copy-pasting
	public static SetDiff<RowAnnotation,TransportRowAnnotation> fromTransport(TransportSetDiffForRowAnnotations tsd) {
		Set<RowAnnotation> added = new HashSet<RowAnnotation>();
		for (TransportRowAnnotation t : tsd.added) {
			added.add(t.fromTransport());
		}
		Set<RowAnnotation> removed = new HashSet<RowAnnotation>();
		for (TransportRowAnnotation t : tsd.removed) {
			removed.add(t.fromTransport());
		}
		return new SetDiff<RowAnnotation,TransportRowAnnotation>(added, removed);
	}
	
	// XXX Unnecessary copy-pasting
	public static SetDiff<MarkerAnnotation,TransportMarkerAnnotation> fromTransport(TransportSetDiffForMarkerAnnotations tsd) {
		Set<MarkerAnnotation> added = new HashSet<MarkerAnnotation>();
		for (TransportMarkerAnnotation t : tsd.added) {
			added.add(t.fromTransport());
		}
		Set<MarkerAnnotation> removed = new HashSet<MarkerAnnotation>();
		for (TransportMarkerAnnotation t : tsd.removed) {
			removed.add(t.fromTransport());
		}
		return new SetDiff<MarkerAnnotation,TransportMarkerAnnotation>(added, removed);
	}
	
	public Set<V> applyTo(Set<V> s1) {
		Set<V> s2 = new HashSet<V>(s1);
		s2.removeAll(removed);
		s2.addAll(added);
		return s2;
	}
	
//	public TransportSetDiff<T> asTransport() {
//		HashSet<T> ta = new HashSet<T>();
//		for (V v : added) {
//			ta.add(v.asTransport());
//		}
//		HashSet<T> tr = new HashSet<T>();
//		for (V v : removed) {
//			tr.add(v.asTransport());
//		}
//		return new TransportSetDiff<T>(ta, tr);
//	}
	
	// XXX Unnecessary copy-pasting
	public TransportSetDiffForRowAnnotations asTransportRowAnnotations() {
		HashSet<TransportRowAnnotation> ta = new HashSet<TransportRowAnnotation>();
		for (V v : added) {
			ta.add((TransportRowAnnotation) v.asTransport());
		}
		HashSet<TransportRowAnnotation> tr = new HashSet<TransportRowAnnotation>();
		for (V v : removed) {
			tr.add((TransportRowAnnotation) v.asTransport());
		}
		return new TransportSetDiffForRowAnnotations(ta, tr);
	}
	
	// XXX Unnecessary copy-pasting
	public TransportSetDiffForMarkerAnnotations asTransportMarkerAnnotations() {
		HashSet<TransportMarkerAnnotation> ta = new HashSet<TransportMarkerAnnotation>();
		for (V v : added) {
			ta.add((TransportMarkerAnnotation) v.asTransport());
		}
		HashSet<TransportMarkerAnnotation> tr = new HashSet<TransportMarkerAnnotation>();
		for (V v : removed) {
			tr.add((TransportMarkerAnnotation) v.asTransport());
		}
		return new TransportSetDiffForMarkerAnnotations(ta, tr);
	}
	
	@Override
	public String toString() {
		return "added: " + added + ", removed: " + removed;
	}
}
