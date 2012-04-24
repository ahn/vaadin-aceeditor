package org.vaadin.aceeditor.gwt.shared;

public class LockMarkerData implements Marker.Data {

	private final String lockerId;
	private final String msg;

	public LockMarkerData(String dataString) {
		String[] items = dataString.split(":", 2);
		lockerId = items[0];
		msg = (items.length == 2 && !items[1].isEmpty()) ? items[1] : null;
	}

	public LockMarkerData(String lockerId, String msg) {
		this.lockerId = lockerId;
		this.msg = msg;
	}

//	@Override
	public String getDataString() {
		if (msg == null) {
			return lockerId;
		} else {
			return lockerId + ":" + msg;
		}
	}

	public String getLockerId() {
		return lockerId;
	}

	public String getMessage() {
		return msg;
	}
}
