package org.vaadin.aceeditor.client;

import org.vaadin.aceeditor.client.TransportDiff.TransportMarkerDiff;

// TODO This is not the best way to diff markers.
// This is just stupid line-col diff without considering the
// text at all. That's why the markers don't always stay
// where you expect them to.
// Hence you can't currently use markers in anything you need to be
// sure their position is correct.

public class MarkerDiff {
	private final RangeDiff rangeDiff;
	public MarkerDiff(final RangeDiff rangeDiff) {
		this.rangeDiff = rangeDiff;
	}
	public AceMarker applyTo(final AceMarker m) {
		return m.withNewPosition(this.rangeDiff.applyTo(m.getRange()));
	}
	public static MarkerDiff diff(final AceMarker m1, final AceMarker m2) {
		return new MarkerDiff(RangeDiff.diff(m1.getRange(), m2.getRange()));
	}
	public boolean isIdentity() {
		return this.rangeDiff.isIdentity();
	}
	@Override
	public String toString() {
		return this.rangeDiff.toString();
	}
	public TransportMarkerDiff asTransport() {
		return new TransportMarkerDiff(this.rangeDiff.asTransport());
	}
	public static MarkerDiff fromTransport(final TransportMarkerDiff tmd) {
		return new MarkerDiff(RangeDiff.fromTransport(tmd.rangeDiff));
	}
}
