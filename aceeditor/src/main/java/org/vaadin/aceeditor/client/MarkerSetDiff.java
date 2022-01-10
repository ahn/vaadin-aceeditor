package org.vaadin.aceeditor.client;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.aceeditor.client.TransportDiff.TransportMarkerAddition;
import org.vaadin.aceeditor.client.TransportDiff.TransportMarkerDiff;
import org.vaadin.aceeditor.client.TransportDiff.TransportMarkerSetDiff;

@SuppressWarnings("serial")
public class MarkerSetDiff implements Serializable {

	private final Map<String, MarkerAddition> added;
	private final Map<String, MarkerDiff> moved;
	private final Set<String> removed;

	public MarkerSetDiff(final Map<String, MarkerAddition> added, final Set<String> removed) {
		this.added = added;
		this.moved = Collections.emptyMap();
		this.removed = removed;
	}

	public MarkerSetDiff(final Map<String, MarkerAddition> added,
			final Map<String, MarkerDiff> moved, final Set<String> removed) {
		this.added = added;
		this.moved = moved;
		this.removed = removed;
	}

	public static MarkerSetDiff diff(final Map<String, AceMarker> m1, final Map<String, AceMarker> m2, final String text2) {

		final Map<String, MarkerAddition> added = new HashMap<>();
		final Map<String, MarkerDiff> diffs = new HashMap<>();
		for (final Entry<String, AceMarker> e : m2.entrySet()) {
			final AceMarker c1 = m1.get(e.getKey());
			if (c1 != null) {
				final MarkerDiff d = MarkerDiff.diff(c1, e.getValue());
				if (!d.isIdentity()) {
					diffs.put(e.getKey(), d);
				}
			} else {
				added.put(e.getKey(), new MarkerAddition(e.getValue(), text2));
			}
		}

		final Set<String> removedIds = new HashSet<>(m1.keySet());
		removedIds.removeAll(m2.keySet());

		return new MarkerSetDiff(added, diffs, removedIds);
	}

	public Map<String, AceMarker> applyTo(final Map<String, AceMarker> markers, final String text2) {
		final Map<String, AceMarker> markers2 = new HashMap<>();
		for (final Entry<String, MarkerAddition> e : this.added.entrySet()) {
			final AceMarker adjusted = e.getValue().getAdjustedMarker(text2);
			if (adjusted != null) {
				markers2.put(e.getKey(), adjusted);
			}
		}

		for (final Entry<String, AceMarker> e : markers.entrySet()) {
			if (this.removed.contains(e.getKey())) {
				continue;
			}
			AceMarker m = e.getValue();

			// ???
			if (markers2.containsKey(e.getKey())) {
				m = markers2.get(e.getKey());
			}

			final MarkerDiff md = this.moved.get(e.getKey());
			if (md != null) {
				markers2.put(e.getKey(), md.applyTo(m));
			} else {
				markers2.put(e.getKey(), m);
			}
		}

		return markers2;
	}

	@Override
	public String toString() {
		return "added: " + this.added + "\n" +
				"moved: " + this.moved + "\n" +
				"removed: " + this.removed;
	}

	public boolean isIdentity() {
		return this.added.isEmpty() && this.moved.isEmpty() && this.removed.isEmpty();
	}

	public TransportMarkerSetDiff asTransportDiff() {
		final TransportMarkerSetDiff msd = new TransportMarkerSetDiff();
		msd.added = this.getTransportAdded();
		msd.moved = this.getTransportMoved();
		msd.removed = this.getTransportRemoved();
		return msd;
	}

	private Map<String, TransportMarkerAddition> getTransportAdded() {
		final HashMap<String, TransportMarkerAddition> ta = new HashMap<>();
		for (final Entry<String, MarkerAddition> e : this.added.entrySet()) {
			ta.put(e.getKey(), e.getValue().asTransport());
		}
		return ta;
	}

	private Map<String, TransportMarkerDiff> getTransportMoved() {
		final HashMap<String, TransportMarkerDiff> ta = new HashMap<>();
		for (final Entry<String, MarkerDiff> e : this.moved.entrySet()) {
			ta.put(e.getKey(), e.getValue().asTransport());
		}
		return ta;
	}

	private Set<String> getTransportRemoved() {
		return this.removed; // No need for a defensive copy??
	}

	public static MarkerSetDiff fromTransportDiff(final TransportMarkerSetDiff td) {
		return new MarkerSetDiff(
				MarkerSetDiff.addedFromTransport(td.added),
				MarkerSetDiff.movedFromTransport(td.moved),
				MarkerSetDiff.removedFromTransport(td.removed));
	}

	private static Map<String, MarkerAddition> addedFromTransport(
			final Map<String, TransportMarkerAddition> added2) {
		final HashMap<String, MarkerAddition> added = new HashMap<>();
		for (final Entry<String, TransportMarkerAddition> e : added2.entrySet()) {
			added.put(e.getKey(), MarkerAddition.fromTransport(e.getValue()));
		}
		return added;
	}

	private static Map<String, MarkerDiff> movedFromTransport(
			final Map<String, TransportMarkerDiff> mt) {
		final HashMap<String, MarkerDiff> moved = new HashMap<>();
		for (final Entry<String, TransportMarkerDiff> e : mt.entrySet()) {
			moved.put(e.getKey(), MarkerDiff.fromTransport(e.getValue()));
		}
		return moved;
	}

	private static Set<String> removedFromTransport(final Set<String> tr) {
		return tr; // No need for a defensive copy??
	}

}
