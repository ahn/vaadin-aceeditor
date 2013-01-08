package org.vaadin.aceeditor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.gwt.shared.Marker;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

// FIXME
// Markers' positions are not updated on the server side currently.
// That's why they are in wrong positions when eg. page is refreshed and the
// client side is drawn from scratch (with updated text but old markers).
// TODO: send updated marker positions along with changed text from client to server

/**
 * {@link AceEditor} that can contain
 * {@link org.vaadin.aceeditor.gwt.shared.Marker Markers}.
 * 
 */
@SuppressWarnings("serial")
@com.vaadin.ui.ClientWidget(org.vaadin.aceeditor.gwt.client.VAceMarkerEditor.class)
public class AceMarkerEditor extends AceEditor {

	private long latestMarkerId = 0;

	private String nextMarkerId() {
		return "m" + (++latestMarkerId);
	}

	private Map<String, Marker> markers = new HashMap<String, Marker>();

	/**
	 * Adds a new {@link org.vaadin.aceeditor.gwt.shared.Marker Marker}.
	 * 
	 * @param marker
	 * @return the id of the Marker
	 */
	public String addMarker(Marker marker) {
		String mid = nextMarkerId();
		markers.put(mid, marker);
		requestRepaint();
		return mid;
	}

	/**
	 * Removes a {@link org.vaadin.aceeditor.gwt.shared.Marker Marker} with the
	 * given id.
	 * 
	 * @param markerId
	 * @return the removed Marker, or null
	 */
	public Marker removeMarker(String markerId) {
		Marker removed = markers.remove(markerId);
		if (removed != null) {
			requestRepaint();
		}
		return removed;
	}

	/**
	 * Removes all the markers.
	 */
	public void clearMarkers() {
		markers.clear();
		requestRepaint();
	}

	/**
	 * Removes all the markers of the given
	 * {@link org.vaadin.aceeditor.gwt.shared.Marker.Type Type}.
	 * 
	 * @param type
	 */
	public void clearMarkersOfType(Marker.Type type) {
		LinkedList<String> ofType = new LinkedList<String>();
		for (Entry<String, Marker> e : markers.entrySet()) {
			if (e.getValue().getType() == type) {
				ofType.add(e.getKey());
			}
		}
		if (!ofType.isEmpty()) {
			for (String mid : ofType) {
				markers.remove(mid);
			}
			requestRepaint();
		}
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		target.addAttribute("markers", markers);
	}

}
