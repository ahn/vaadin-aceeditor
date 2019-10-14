package org.vaadin.aceeditor.client;

import org.vaadin.aceeditor.client.TransportDiff.TransportMarkerAddition;

public class MarkerAddition {
	private final AceMarker marker;
	private final String startContext;
	private final String endContext;
	public MarkerAddition(final AceMarker marker, final String text2) {
		this.marker = marker;

		// TODO
		this.startContext = "";
		this.endContext = "";
	}
	private MarkerAddition(final AceMarker marker, final String startContext, final String endContext) {
		this.marker = marker;
		this.startContext = startContext;
		this.endContext = endContext;
	}
	public AceMarker getAdjustedMarker(final String text) {
		// TODO adjust
		return this.marker;
	}
	public TransportMarkerAddition asTransport() {
		return new TransportMarkerAddition(this.marker.asTransport(), this.startContext, this.endContext);
	}
	public static MarkerAddition fromTransport(final TransportMarkerAddition ta) {
		return new MarkerAddition(AceMarker.fromTransport(ta.marker), ta.startContext, ta.endContext);
	}


}
