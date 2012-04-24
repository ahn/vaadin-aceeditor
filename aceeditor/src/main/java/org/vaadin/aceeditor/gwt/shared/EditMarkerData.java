package org.vaadin.aceeditor.gwt.shared;

public class EditMarkerData implements Marker.Data {

	private final String userId;
	private final String userStyle;

	public EditMarkerData(String dataString) {
		String[] items = dataString.split(":", 2);
		this.userId = items[0];
		this.userStyle = items[1];
	}

	public EditMarkerData(String userId, String userStyle) {
		this.userId = userId;
		this.userStyle = userStyle;
	}

//	@Override
	public String getDataString() {
		return userId + ":" + userStyle;
	}

	public String getUserStyle() {
		return userStyle;
	}

	public Object getUserId() {
		return userId;
	}

}
