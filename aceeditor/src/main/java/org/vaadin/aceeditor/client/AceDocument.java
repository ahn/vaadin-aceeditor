package org.vaadin.aceeditor.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The document that AceEditor internally sends to and from server.
 */
@SuppressWarnings("serial")
public class AceDocument implements Serializable {

	private String text = "";
	private Set<AceClientMarker> markers = new HashSet<AceClientMarker>();
	private Set<AceClientAnnotation> rowAnnotations = null;
	private Set<AceClientAnnotation> markerAnnotations = null;
	private boolean latestChangeByServer = true;
	
	public AceDocument() {
		
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Set<AceClientMarker> getMarkers() {
		return markers;
	}

	public void setMarkers(Set<AceClientMarker> markers) {
		this.markers = markers;
	}

	public Set<AceClientAnnotation> getRowAnnotations() {
		return rowAnnotations;
	}

	public void setRowAnnotations(Set<AceClientAnnotation> rowAnnotations) {
		this.rowAnnotations = rowAnnotations;
	}

	public Set<AceClientAnnotation> getMarkerAnnotations() {
		return markerAnnotations;
	}

	public void setMarkerAnnotations(Set<AceClientAnnotation> markerAnnotations) {
		this.markerAnnotations = markerAnnotations;
	}

	public boolean isLatestChangeByServer() {
		return latestChangeByServer;
	}

	public void setLatestChangeByServer(boolean latestChangeByServer) {
		this.latestChangeByServer = latestChangeByServer;
	}
	
	
}
