package org.vaadin.aceeditor.client;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarker;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;

public class AceDoc implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String text;

	// key: markerId
	private final Map<String, AceMarker> markers;

	private final Set<RowAnnotation> rowAnnotations;

	private final Set<MarkerAnnotation> markerAnnotations;

	public AceDoc() {
		this("");
	}

	public AceDoc(final String text) {
		this(text,
				Collections.<String, AceMarker> emptyMap(),
				Collections.<RowAnnotation>emptySet(),
				Collections.<MarkerAnnotation>emptySet());
	}

	public AceDoc(final String text, final Map<String, AceMarker> markers) {
		this(text, markers,
				Collections.<RowAnnotation>emptySet(),
				Collections.<MarkerAnnotation>emptySet());
	}

	public AceDoc(final String text, final Map<String, AceMarker> markers,
			final Set<RowAnnotation> rowAnnotations,
			final Set<MarkerAnnotation> markerAnnotations) {
		this.text = text == null ? "" : text;
		this.markers = markers;
		this.rowAnnotations = rowAnnotations;
		this.markerAnnotations = markerAnnotations;
	}

	public String getText() {
		return this.text;
	}

	public Map<String, AceMarker> getMarkers() {
		return Collections.unmodifiableMap(this.markers);
	}

	public Set<RowAnnotation> getRowAnnotations() {
		if (this.rowAnnotations==null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(this.rowAnnotations);
	}

	public Set<MarkerAnnotation> getMarkerAnnotations() {
		if (this.markerAnnotations==null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(this.markerAnnotations);
	}

	public boolean hasRowAnnotations() {
		return this.rowAnnotations != null;
	}

	public boolean hasMarkerAnnotations() {
		return this.markerAnnotations != null;
	}

	@Override
	public String toString() {
		return this.text + "\n/MARKERS: "+this.markers+"\nra:"+this.rowAnnotations+", ma:"+this.markerAnnotations;
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof AceDoc) {
			final AceDoc od = (AceDoc) other;
			return this.textEquals(this.text, od.text) &&
					Util.sameMaps(this.markers, od.markers) &&
					Util.sameSets(this.markerAnnotations, od.markerAnnotations) &&
					Util.sameSets(this.rowAnnotations, od.rowAnnotations);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getText().hashCode();
	}

	public boolean textEquals(final String a, final String b) {
		return a == null ? b == null : a.equals(b);
	}

	public AceDoc withText(final String newText) {
		return new AceDoc(newText, this.markers, this.rowAnnotations, this.markerAnnotations);
	}

	public TransportDoc asTransport() {
		final TransportDoc td = new TransportDoc();
		td.text = this.text;

		td.markers = this.getTransportMarkers();
		td.markerAnnotations = this.getTransportMarkerAnnotations();
		td.rowAnnotations = this.getTransportRowAnnotations();

		return td;
	}

	/* TODO private */ Map<String, TransportMarker> getTransportMarkers() {
		final HashMap<String, TransportMarker> ms = new HashMap<>(
				this.markers.size());
		for (final Entry<String, AceMarker> e : this.markers.entrySet()) {
			ms.put(e.getKey(), e.getValue().asTransport());
		}
		return ms;
	}


	private Set<TransportRowAnnotation> getTransportRowAnnotations() {
		if (this.rowAnnotations==null) {
			return null;
		}
		final HashSet<TransportRowAnnotation> anns = new HashSet<>(
				this.rowAnnotations.size());
		for (final RowAnnotation ra : this.rowAnnotations) {
			anns.add(ra.asTransport());
		}
		return anns;
	}

	private Set<TransportMarkerAnnotation> getTransportMarkerAnnotations() {
		if (this.markerAnnotations==null) {
			return null;
		}
		final HashSet<TransportMarkerAnnotation> anns = new HashSet<>(
				this.markerAnnotations.size());
		for (final MarkerAnnotation ma : this.markerAnnotations) {
			anns.add(ma.asTransport());
		}
		return anns;
	}

	public static AceDoc fromTransport(final TransportDoc doc) {
		final String text = doc.text;
		final Map<String, AceMarker> markers = AceDoc.markersFromTransport(doc.markers, doc.text);
		final Set<RowAnnotation> rowAnnotations = AceDoc.rowAnnotationsFromTransport(doc.rowAnnotations);
		final Set<MarkerAnnotation> markerAnnotations = AceDoc.markerAnnotationsFromTransport(doc.markerAnnotations);
		return new AceDoc(text, markers, rowAnnotations, markerAnnotations);
	}

	private static Map<String, AceMarker> markersFromTransport(
			final Map<String, TransportMarker> markers, final String text) {
		final HashMap<String, AceMarker> ms = new HashMap<>();
		for (final Entry<String, TransportMarker> e : markers.entrySet()) {
			ms.put(e.getKey(), AceMarker.fromTransport(e.getValue()));
		}
		return ms;
	}

	private static Set<MarkerAnnotation> markerAnnotationsFromTransport(
			final Set<TransportMarkerAnnotation> markerAnnotations) {
		if (markerAnnotations==null) {
			return null;
		}
		final HashSet<MarkerAnnotation> anns = new HashSet<>(markerAnnotations.size());
		for (final TransportMarkerAnnotation ta : markerAnnotations) {
			anns.add(ta.fromTransport());
		}
		return anns;
	}

	private static Set<RowAnnotation> rowAnnotationsFromTransport(
			final Set<TransportRowAnnotation> rowAnnotations) {
		if (rowAnnotations==null) {
			return null;
		}
		final HashSet<RowAnnotation> anns = new HashSet<>(rowAnnotations.size());
		for (final TransportRowAnnotation ta : rowAnnotations) {
			anns.add(ta.fromTransport());
		}
		return anns;
	}

	// TODO?
	public AceDoc withMarkers(final Set<AceMarker> newMarkers) {
		final HashMap<String, AceMarker> markers2 = new HashMap<>(newMarkers.size());
		for (final AceMarker m : newMarkers) {
			markers2.put(m.getMarkerId(), m);
		}
		return new AceDoc(this.text, markers2, this.rowAnnotations, this.markerAnnotations);
	}

	public AceDoc withMarkers(final Map<String, AceMarker> newMarkers) {
		return new AceDoc(this.text, newMarkers, this.rowAnnotations, this.markerAnnotations);
	}
	public AceDoc withAdditionalMarker(final AceMarker marker) {
		final HashMap<String, AceMarker> markers2 = new HashMap<>(this.markers);
		markers2.put(marker.getMarkerId(), marker);
		return new AceDoc(this.text, markers2, this.rowAnnotations, this.markerAnnotations);
	}
	public AceDoc withAdditionalMarkers(final Map<String, AceMarker> addMarkers) {
		final HashMap<String, AceMarker> newMarkers = new HashMap<>(this.markers);
		newMarkers.putAll(addMarkers);
		return new AceDoc(this.text, newMarkers, this.rowAnnotations, this.markerAnnotations);
	}

	public AceDoc withoutMarker(final String markerId) {
		final HashMap<String, AceMarker> markers2 = new HashMap<>(this.markers);
		markers2.remove(markerId);
		return new AceDoc(this.text, markers2, this.rowAnnotations, this.markerAnnotations);
	}

	public AceDoc withoutMarkers() {
		final Map<String, AceMarker> noMarkers = Collections.emptyMap();
		return new AceDoc(this.text, noMarkers, this.rowAnnotations, this.markerAnnotations);
	}

	public AceDoc withoutMarkers(final Set<String> without) {
		final Map<String, AceMarker> newMarkers = new HashMap<>(this.markers);
		for (final String m : without) {
			newMarkers.remove(m);
		}
		return new AceDoc(this.text, newMarkers, this.rowAnnotations, this.markerAnnotations);
	}

	public AceDoc withRowAnnotations(final Set<RowAnnotation> ranns) {
		return new AceDoc(this.text, this.markers, ranns, this.markerAnnotations);
	}

	public AceDoc withMarkerAnnotations(final Set<MarkerAnnotation> manns) {
		return new AceDoc(this.text, this.markers, this.rowAnnotations, manns);
	}

	public AceDoc withAdditionalMarkerAnnotation(final MarkerAnnotation mann) {
		final HashSet<MarkerAnnotation> manns = this.markerAnnotations==null?new HashSet<>():new HashSet<>(this.markerAnnotations);
		manns.add(mann);
		return new AceDoc(this.text, this.markers, this.rowAnnotations, manns);
	}

	public AceDoc withAdditionalRowAnnotation(final RowAnnotation rann) {
		final HashSet<RowAnnotation> ranns = this.rowAnnotations==null?new HashSet<>():new HashSet<>(this.rowAnnotations);
		ranns.add(rann);
		return new AceDoc(this.text, this.markers, ranns, this.markerAnnotations);
	}
}