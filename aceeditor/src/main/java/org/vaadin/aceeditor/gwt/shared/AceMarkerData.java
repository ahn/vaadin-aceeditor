package org.vaadin.aceeditor.gwt.shared;

public class AceMarkerData implements Marker.Data {

	private final String cls;
	private final String type;
	private final boolean inFront;

	public AceMarkerData(String cls, String type, boolean inFront) {
		this.cls = cls;
		this.type = type;
		this.inFront = inFront;
	}

	public AceMarkerData(String dataString) {
		String[] items = dataString.split(":", 3);
		this.cls = items[0];
		this.type = items[1];
		this.inFront = Boolean.valueOf(items[2]);
	}

//	@Override
	public String getDataString() {
		return cls + ":" + type + ":" + inFront;
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

}
