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

	public SetDiff(final Set<V> added, final Set<V> removed) {
		this.added = added;
		this.removed = removed;
	}

	public SetDiff() {
		this.added = Collections.emptySet();
		this.removed = Collections.emptySet();
	}

	public static class Differ<V extends TransportableAs<T>,T> {
		public SetDiff<V,T> diff(final Set<V> s1, final Set<V> s2) {
			final Set<V> removed = new HashSet<>(s1);
			removed.removeAll(s2);

			final Set<V> added = new HashSet<>(s2);
			added.removeAll(s1);
			return new SetDiff<>(added, removed);
		}
	}

	// XXX Unnecessary copy-pasting
	public static SetDiff<RowAnnotation,TransportRowAnnotation> fromTransport(final TransportSetDiffForRowAnnotations tsd) {
		final Set<RowAnnotation> added = new HashSet<>();
		for (final TransportRowAnnotation t : tsd.added) {
			added.add(t.fromTransport());
		}
		final Set<RowAnnotation> removed = new HashSet<>();
		for (final TransportRowAnnotation t : tsd.removed) {
			removed.add(t.fromTransport());
		}
		return new SetDiff<>(added, removed);
	}

	// XXX Unnecessary copy-pasting
	public static SetDiff<MarkerAnnotation,TransportMarkerAnnotation> fromTransport(final TransportSetDiffForMarkerAnnotations tsd) {
		final Set<MarkerAnnotation> added = new HashSet<>();
		for (final TransportMarkerAnnotation t : tsd.added) {
			added.add(t.fromTransport());
		}
		final Set<MarkerAnnotation> removed = new HashSet<>();
		for (final TransportMarkerAnnotation t : tsd.removed) {
			removed.add(t.fromTransport());
		}
		return new SetDiff<>(added, removed);
	}

	public Set<V> applyTo(final Set<V> s1) {
		final Set<V> s2 = new HashSet<>(s1);
		s2.removeAll(this.removed);
		s2.addAll(this.added);
		return s2;
	}

	// XXX Unnecessary copy-pasting
	public TransportSetDiffForRowAnnotations asTransportRowAnnotations() {
		final HashSet<TransportRowAnnotation> ta = new HashSet<>();
		for (final V v : this.added) {
			ta.add((TransportRowAnnotation) v.asTransport());
		}
		final HashSet<TransportRowAnnotation> tr = new HashSet<>();
		for (final V v : this.removed) {
			tr.add((TransportRowAnnotation) v.asTransport());
		}
		return new TransportSetDiffForRowAnnotations(ta, tr);
	}

	// XXX Unnecessary copy-pasting
	public TransportSetDiffForMarkerAnnotations asTransportMarkerAnnotations() {
		final HashSet<TransportMarkerAnnotation> ta = new HashSet<>();
		for (final V v : this.added) {
			ta.add((TransportMarkerAnnotation) v.asTransport());
		}
		final HashSet<TransportMarkerAnnotation> tr = new HashSet<>();
		for (final V v : this.removed) {
			tr.add((TransportMarkerAnnotation) v.asTransport());
		}
		return new TransportSetDiffForMarkerAnnotations(ta, tr);
	}

	@Override
	public String toString() {
		return "added: " + this.added + ", removed: " + this.removed;
	}
}
