package org.vaadin.aceeditor.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The document that AceEditor internally sends to and from server.
 */
@SuppressWarnings("serial")
public class AceDocument implements Serializable {

	// TODO: should probably implement equals?
	
	private String text = "";
	private Set<AceClientMarker> markers = new HashSet<AceClientMarker>();
	private Set<AceClientAnnotation> rowAnnotations = null;
	private Set<AceClientAnnotation> markerAnnotations = null;
	
	/**
	 * 0 if no change on server
	 * > 0 if document value changed on server and should be set on client
	 * 
	 * int instead of boolean because we need to make sure that the value
	 * changes (increments), and thus the client receives the changed value.
	 * If latestChangedByServer "changed" from true to true, that might
	 * not be the case.
	 */
	private int latestChangeByServer = 0;
	
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

	public int getLatestChangeByServer() {
		return latestChangeByServer;
	}

	public void setLatestChangeByServer(int latestChangeByServer) {
		this.latestChangeByServer = latestChangeByServer;
	}
	
	public void incrementLatestChangeByServer() {
		this.latestChangeByServer++;
	}
	
}
