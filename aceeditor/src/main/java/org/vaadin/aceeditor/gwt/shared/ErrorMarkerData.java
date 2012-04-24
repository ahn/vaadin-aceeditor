package org.vaadin.aceeditor.gwt.shared;

public class ErrorMarkerData implements Marker.Data {

	private final String msg;

	public ErrorMarkerData(String dataString) {
		msg = dataString;
	}

	public String getErrorMessage() {
		return msg;
	}

//	@Override
	public String getDataString() {
		return msg;
	}
}
