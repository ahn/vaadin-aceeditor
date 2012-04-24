package org.vaadin.aceeditor.gwt.client;

import java.util.Map;

import org.vaadin.aceeditor.gwt.shared.Marker;

public interface MarkerEditorFacade extends EditorFacade {
	public Marker getMarker(String markerId);

	public void putMarker(String markerId, Marker marker);

	public void removeMarker(String markerId);

	public void clearMarkers();

	public Map<String, Marker> getMarkers();

	void setUserId(String userId);
}
