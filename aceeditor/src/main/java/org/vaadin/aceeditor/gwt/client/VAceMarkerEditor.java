package org.vaadin.aceeditor.gwt.client;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.aceeditor.gwt.shared.Marker;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ValueMap;

public class VAceMarkerEditor extends VAceEditor {

	protected MarkerEditorFacade markerEditor;

	public VAceMarkerEditor() {
		super(false);
		editor = markerEditor = new AceMarkerEditorFacade();
		initWidget(editor.getWidget());
	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		if (uidl.hasAttribute("markers")) {
			ValueMap markers = uidl.getMapAttribute("markers");
			Map<String, Marker> currentMarkers = markerEditor.getMarkers();
			for (Entry<String, Marker> e : currentMarkers.entrySet()) {
				if (!markers.containsKey(e.getKey())
						&& !isClientOnlyMarker(e.getValue())) {
					markerEditor.removeMarker(e.getKey());
				}
			}

			Set<String> keys = markers.getKeySet();
			for (String key : keys) {
				if (currentMarkers.containsKey(key)) {
					continue;
				}
				String m = markers.getString(key);
				Marker marker = Marker.fromString(m);
				markerEditor.putMarker(key, marker);
			}
		}
	}

	private boolean isClientOnlyMarker(Marker marker) {
		// ?
		return marker.getType() == Marker.Type.SUGGESTION;
	}

	@Override
	public void textChanged() {
		super.textChanged();
	}

}
