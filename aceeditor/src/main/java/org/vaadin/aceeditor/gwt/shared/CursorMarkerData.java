package org.vaadin.aceeditor.gwt.shared;

public class CursorMarkerData implements Marker.Data {

	private String collabId;

	public CursorMarkerData(String dataString) {
		this.collabId = dataString;
	}

	public String getCollabId() {
		return collabId;
	}

//	@Override
	public String getDataString() {
		return collabId;
	}

}
