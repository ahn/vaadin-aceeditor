package org.vaadin.aceeditor.gwt.shared;

public class CollaboratorAceMarkerData implements Marker.Data {

	private final String cls;
	private final String type;
	private final boolean inFront;
	private final String userId;

	public CollaboratorAceMarkerData(String cls, String type, boolean inFront, String userId) {
		this.cls = cls;
		this.type = type;
		this.inFront = inFront;
		this.userId = userId;
	}

	public CollaboratorAceMarkerData(String dataString) {
		String[] items = dataString.split(":", 4);
		this.cls = items[0];
		this.type = items[1];
		this.inFront = Boolean.valueOf(items[2]);
		this.userId = items[3];
	}

	public String getDataString() {
		return cls + ":" + type + ":" + inFront + ":" + userId;
	}

	public String getCls() {
		return cls;
	}

	public String getType() {
		return type;
	}

	public boolean isInFront() {
		return inFront;
	}
	
	public String getUserId() {
		return userId;
	}

}
