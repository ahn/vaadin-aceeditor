package org.vaadin.aceeditor.gwt.shared;

/**
 * @author jkubrynski@gmail.com
 * @since 2012.12.17
 */
public class CommentMarkerData implements Marker.Data {

	private final String comment;

	public CommentMarkerData(String dataString) {
		comment = dataString;
	}

	public String getComment() {
		return comment;
	}

	public String getDataString() {
		return comment;
	}
}
