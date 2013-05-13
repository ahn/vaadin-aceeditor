package org.vaadin.aceeditor.client;

import org.vaadin.aceeditor.client.TransportDiff.TransportMarkerAddition;

public class MarkerAddition {
	private final AceMarker marker;
	private final String startContext;
	private final String endContext;
	public MarkerAddition(AceMarker marker, String text2) {
		this.marker = marker;
		
		// TODO
		startContext = "";
		endContext = "";
	}
	private MarkerAddition(AceMarker marker, String startContext, String endContext) {
		this.marker = marker;
		this.startContext = startContext;
		this.endContext = endContext;
	}
	public AceMarker getAdjustedMarker(String text) {
		// TODO adjust
		return marker;
	}
	public TransportMarkerAddition asTransport() {
		return new TransportMarkerAddition(marker.asTransport(), startContext, endContext);
	}
	public static MarkerAddition fromTransport(TransportMarkerAddition ta) {
		return new MarkerAddition(AceMarker.fromTransport(ta.marker), ta.startContext, ta.endContext);
	}
	

}
