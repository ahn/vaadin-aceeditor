package org.vaadin.aceeditor.gwt.shared;

/**
 * A marker in a text.
 * 
 * A Marker has a type, start, end, and possibly additional data depending on
 * the type.
 * 
 * For example: error markers, search markers, etc.
 * 
 */
public class Marker implements Comparable<Marker> {

	public enum Type {
		INVISIBLE, CURSOR, ERROR, NOTE, LOCK, EDIT, SEARCH, ACE, SUGGESTION, COMMENT
	}

	public interface Data {
		public String getDataString();
	}

	private final Type type;
	private int start;
	private int end;
	private final Data data;

	public Marker(Type type, int start, int end) {
		this(type, start, end, (Data) null);
	}

	public Marker(Type type, int start, int end, Data data) {
		super();
		this.type = type;
		this.start = start;
		this.end = end;
		this.data = data;
	}

	public Marker(Type type, int start, int end, String dataStr) {
		super();
		this.type = type;
		this.start = start;
		this.end = end;
		this.data = dataStr != null && !dataStr.isEmpty() ? dataForType(type,
				dataStr) : null;
	}

	/**
	 * Returns a new lock.
	 * 
	 * Locks create an unmodifiable segment in the editor.
	 * 
	 * @param start
	 * @param end
	 */
	public static Marker newLockMarker(int start, int end) {
		return new Marker(Type.LOCK, start, end);
	}

	public static Marker newLockMarker(int start, int end, String lockerId,
			String msg) {
		return new Marker(Type.LOCK, start, end, new LockMarkerData(lockerId,
				msg));
	}

	/**
	 * An error marker.
	 * 
	 * @param start
	 * @param end
	 * @param msg
	 *            the error message
	 */
	public static Marker newErrorMarker(int start, int end, String msg) {
		return new Marker(Type.ERROR, start, end, new ErrorMarkerData(msg));
	}

	public static Marker newCursorMarker(int start, int end, String collabId) {
		return new Marker(Type.CURSOR, start, end, new CursorMarkerData(
				collabId));
	}

	public static Marker newNoteMarker(int start, int end) {
		return new Marker(Type.NOTE, start, end);
	}

	public static Marker newEditMarker(int start, int end, String userId,
			String userStyle) {
		return new Marker(Type.EDIT, start, end, new EditMarkerData(userId,
				userStyle));
	}

	public static Marker newSearchMarker(int start, int end) {
		return new Marker(Type.SEARCH, start, end);
	}

	public static Marker newCommentMarker(int start, int end, String msg) {
		return new Marker(Type.COMMENT, start, end, new CommentMarkerData(msg));
	}

	/**
	 * "Raw" Ace marker.
	 * 
	 * <p>
	 * A css definition for the marker must be created in some .css file. For
	 * Example:
	 * 
	 * <pre>
	 * .myerrormarker {
	 *   position: absolute;
	 *   z-index: 5;
	 *   box-sizing: border-box;
	 *   -moz-box-sizing: border-box;
	 *   -webkit-box-sizing: border-box;
	 *   border-bottom: 2px red dotted;
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param start
	 * @param end
	 * @param cls
	 *            the css class of the marker
	 * @param type
	 *            "text" or "line"
	 * @param inFront
	 */
	public static Marker newAceMarker(int start, int end, String cls,
			String type, boolean inFront) {
		return new Marker(Marker.Type.ACE, start, end, new AceMarkerData(cls,
				type, inFront));
	}

	public static Marker newSuggestionMarker(int start, int end) {
		return new Marker(Type.SUGGESTION, start, end);
	}

	public static Marker fromString(String s) {
		String[] items = s.split(":", 4);
		Type type = Type.valueOf(items[0]);
		int start = Integer.valueOf(items[1]);
		int end = Integer.valueOf(items[2]);
		String dataStr = items.length == 4 ? items[3] : null;
		if (items.length == 4) {
			return new Marker(type, start, end, dataForType(type, dataStr));
		} else {
			return new Marker(type, start, end);
		}

	}

	@Override
	public String toString() {
		if (data == null) {
			return type.toString() + ":" + start + ":" + end;
		} else {
			return type.toString() + ":" + start + ":" + end + ":"
					+ data.getDataString();
		}
	}

	private static Data dataForType(Type type, String dataString) {
		switch (type) {
		case ERROR:
			return new ErrorMarkerData(dataString);
		case ACE:
			return new AceMarkerData(dataString);
		case LOCK:
			return new LockMarkerData(dataString);
		case EDIT:
			return new EditMarkerData(dataString);
		case COMMENT:
			return new CommentMarkerData(dataString);
		}
		return null;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public Type getType() {
		return type;
	}

	public Marker withNewPos(int start, int end) {
		return new Marker(type, start, end, data);
	}

	public Data getData() {
		return data;
	}

	public String substringOf(String s) {
		return s.substring(getStart(), getEnd());
	}

	public boolean touches(int cursor) {
		return cursor >= start && cursor <= end;
	}

	public boolean touches(int start, int end) {
		return !(start > this.end || end < this.start);
	}

//	@Override
	public int compareTo(Marker other) {
		if (getStart() < other.getStart()) {
			return -1;
		} else if (getStart() > other.getStart()) {
			return 1;
		}
		return getEnd() - other.getEnd();
	}

	public String getDataString() {
		return data != null ? data.getDataString() : "";
	}

}
