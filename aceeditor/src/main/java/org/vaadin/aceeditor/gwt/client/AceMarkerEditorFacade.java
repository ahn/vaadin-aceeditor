package org.vaadin.aceeditor.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.gwt.ace.GwtAceAnnotation;
import org.vaadin.aceeditor.gwt.ace.GwtAceChangeEvent;
import org.vaadin.aceeditor.gwt.ace.GwtAceEditor;
import org.vaadin.aceeditor.gwt.ace.GwtAceRange;
import org.vaadin.aceeditor.gwt.ace.GwtAceSelection;
import org.vaadin.aceeditor.gwt.ace.GwtAceChangeEvent.Data.Action;
import org.vaadin.aceeditor.gwt.shared.AceMarkerData;
import org.vaadin.aceeditor.gwt.shared.EditMarkerData;
import org.vaadin.aceeditor.gwt.shared.ErrorMarkerData;
import org.vaadin.aceeditor.gwt.shared.LockMarkerData;
import org.vaadin.aceeditor.gwt.shared.Marker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.vaadin.terminal.gwt.client.VConsole;

public class AceMarkerEditorFacade extends AceEditorFacade implements
		MarkerEditorFacade {

	private class AceMarker {
		final Marker marker;
		boolean visible;
		int start;
		int end;
		final String cls;
		final String type;
		final boolean inFront;
		final boolean stretching;
		String aceId;
		GwtAceAnnotation annotation;

		AceMarker(Marker marker, String cls, String type, boolean inFront,
				boolean visible) {
			//VConsole.log("NEW AceMarker " + marker.toString());
			this.marker = marker;
			this.start = marker.getStart();
			this.end = marker.getEnd();
			this.cls = cls;
			this.type = type;
			this.inFront = inFront;
			this.visible = visible;
			this.stretching = (marker.getType() == Marker.Type.EDIT ||
							   marker.getType() == Marker.Type.SUGGESTION); // ?
		}
		
		GwtAceRange getRange() {
			return aceRangeFromStartEnd(start, end, getPosAtBeginningOfRows());
		}

		void updateOnEditor(GwtAceEditor editor) {
			if (!visible) {
				return;
			}
			if (aceId != null) {
				editor.removeMarker(aceId);
			}
			GwtAceRange range = getRange();
			aceId = editor.addMarker(range, cls, type, inFront);
			if (annotation != null) {
				annotation.setRow(range.getStart().getRow());
				updateAnnotations();
			}
		}

		public void removeFromEditor(GwtAceEditor editor) {
			if (!visible) {
				return;
			}
			if (aceId != null) {
				editor.removeMarker(aceId);
				aceId = null;
			}
		}

		boolean moveOnInsert(int s, int e) {
			boolean moved = false;
			if ((!stretching && s <= start) || (s < start)) {
				start += (e - s);
				moved = true;
			}
			if ((stretching && s <= end) || (s < end)) { // XXX
				end += (e - s);
				moved = true;
			}
			return moved;
		}

		boolean moveOnRemove(int s, int e) {
			boolean moved = false;
			if (s < start) {
				if (e <= start) {
					start -= (e - s);
				} else {
					start -= (start - s);
				}
				moved = true;
			}
			if (s < end) {
				if (e < end) {
					end -= (e - s);
				} else {
					end -= (end - s);
				}

				moved = true;
			}
			return moved;
		}

		Marker getUpToDateMarker() {
			if (marker.getStart() != start || marker.getEnd() != end) {
				return marker.withNewPos(start, end);
			}
			return marker;
		}

		boolean overlaps(int start, int end) {
			return !(start >= this.end || end <= this.start);
		}

		boolean touches(int start, int end) {
			return !(start > this.end || end < this.start);
		}
	}

	private HashMap<String, AceMarker> markers = new HashMap<String, AceMarker>();
	private HashMap<String, AceMarker> lockMarkers = new HashMap<String, AceMarker>();
	private HashMap<String, AceMarker> fragileMarkers = new HashMap<String, AceMarker>();
	private HashMap<String, GwtAceAnnotation> annotations = new HashMap<String, GwtAceAnnotation>();
	private String userId;

	/* @Override */
	public void setUserId(String userId) {
		if (!sameUser(userId, this.userId)) {
			this.userId = userId;
			redrawMarkers();
		}
	}

	private static boolean sameUser(String uid1, String uid2) {
		return (uid1 == null && uid2 == null)
				|| (uid1 != null && uid1.equals(uid2));
	}

	@Override
	public boolean initializeEditor() {
		boolean init = super.initializeEditor();
		if (init) {
			editor.setKeyboardHandler(this);
		}
		return init;
	}

	/* @Override */
	public void putMarker(String markerId, Marker marker) {
		AceMarker mm = aceMarkerFromMarker(marker);
		AceMarker existing = markers.put(markerId, mm);

		if (existing != null) {
			existing.removeFromEditor(editor);
			GwtAceAnnotation eann = existing.annotation;
			if (eann != null) {
				annotations.remove(markerId);
				updateAnnotations();
			}
		}

		GwtAceAnnotation ann = annotationForMarker(marker);
		if (ann != null) {
			annotations.put(markerId, ann);
			mm.annotation = ann;
		}

		mm.updateOnEditor(editor);

		if (isLockMarker(marker)) {
			lockMarkers.put(markerId, mm);
		}

		if (isFragileMarker(marker)) {
			fragileMarkers.put(markerId, mm);
		}
	}

	private boolean isLockMarker(Marker m) {
		if (m.getType() == Marker.Type.LOCK) {
			LockMarkerData lmd = (LockMarkerData) m.getData();
			return lmd == null || lmd.getLockerId() == null
					|| !lmd.getLockerId().equals(userId);
		}
		return false;
	}

	private boolean isFragileMarker(Marker m) {
		if (m.getType() == Marker.Type.EDIT) {
			EditMarkerData emd = (EditMarkerData) m.getData();
			return !emd.getUserId().equals(userId);
		}
		return false;
	}

	private AceMarker aceMarkerFromMarker(Marker m) {
		// TODO?
		if (m.getType() == Marker.Type.ACE) {
			AceMarkerData amd = (AceMarkerData) m.getData();
			return new AceMarker(m, amd.getCls(), amd.getType(),
					amd.isInFront(), true);
		} else if (m.getType() == Marker.Type.EDIT) {
			boolean visible = !((EditMarkerData) m.getData()).getUserId()
					.equals(userId);
			return new AceMarker(m, markerClassOf(m), markerTypeOf(m), false,
					visible);
		} else {
			return new AceMarker(m, markerClassOf(m), markerTypeOf(m), false,
					true);
		}
	}

	private GwtAceAnnotation annotationForMarker(Marker m) {
		if (m.getType() == Marker.Type.ERROR) {
			ErrorMarkerData emd = (ErrorMarkerData) m.getData();
			if (emd == null || emd.getErrorMessage() == null) {
				return null;
			}
			return GwtAceAnnotation.create("error", emd.getErrorMessage(), 0);
		} else if (m.getType() == Marker.Type.LOCK) {
			LockMarkerData lmd = (LockMarkerData) m.getData();
			if (lmd == null || lmd.getMessage() == null) {
				return null;
			}
			return GwtAceAnnotation.create("info", lmd.getMessage(), 0);
		}
		return null;
	}

	private void updateAnnotations() {
		JsArray<GwtAceAnnotation> anns = GwtAceAnnotation.createEmptyArray();
		for (GwtAceAnnotation a : annotations.values()) {
			anns.push(a);
		}
		editor.setAnnotations(anns);
	}

	private String markerClassOf(Marker marker) {
		if (marker.getType() == Marker.Type.EDIT) {
			EditMarkerData ed = (EditMarkerData) marker.getData();
			return "acemarker-1 EDIT " + ed.getUserStyle();
		}
		if (marker.getType() == Marker.Type.LOCK) {
			LockMarkerData lmd = (LockMarkerData) marker.getData();
			if (lmd != null && lmd.getLockerId() != null
					&& lmd.getLockerId().equals(userId)) {
				return "acemarker-1 MYLOCK";
			}
			return "acemarker-1 LOCK";
		}
		return "acemarker-1 " + marker.getType().toString();
	}

	private static String markerTypeOf(Marker marker) {
		if (marker.getType() == Marker.Type.LOCK) {
			return "line";
		}
		if (marker.getType() == Marker.Type.EDIT) {
			return "line";
		}
		return "text";
	}

	/* @Override */
	public void removeMarker(String markerId) {
		AceMarker existing = markers.remove(markerId);
		if (existing != null) {
			existing.removeFromEditor(editor);
			if (existing.annotation != null) {
				annotations.remove(markerId);
				updateAnnotations();
			}
		}
		lockMarkers.remove(markerId);
		fragileMarkers.remove(markerId);
	}

	/* @Override */
	public void clearMarkers() {
		for (Entry<String, AceMarker> e : markers.entrySet()) {
			e.getValue().removeFromEditor(editor);
		}
		markers.clear();
		lockMarkers.clear();
		if (!annotations.isEmpty()) {
			annotations.clear();
			updateAnnotations();
		}
	}

	public Marker getMarker(String markerId) {
		AceMarker am = markers.get(markerId);
		return am == null ? null : am.getUpToDateMarker();
	}

	/* @Override */
	public Map<String, Marker> getMarkers() {
		HashMap<String, Marker> ms = new HashMap<String, Marker>();
		for (Entry<String, AceMarker> e : markers.entrySet()) {
			ms.put(e.getKey(), e.getValue().getUpToDateMarker());
		}
		return ms;
	}

	@Override
	protected void onChangeBeforeCallingListeners(GwtAceChangeEvent e) {
		// consolelog(e);
		Action act = e.getData().getAction();

		if (act == Action.insertText || act == Action.insertLines) {
			int[] pabor = getPosAtBeginningOfRows();
			int start = posFromAcePos(e.getData().getRange().getStart(), pabor);
			int end = posFromAcePos(e.getData().getRange().getEnd(), pabor);
			updateMarkersOnInsert(start, end, e.getData().getText());
		} else if (act == Action.removeText || act == Action.removeLines) {
			// The remove event positions seem to be wrt. the previous text.
			// That's why we use previous positions.
			// Hopefully this works.
			// What if previous positions are null?
			int[] pabor = getPrevPosAtBeginningOfRows();
			if (pabor == null) {
				VConsole.log("Can't get previous positions => can't adjust markers!");
				return;
			}
			int start = posFromAcePos(e.getData().getRange().getStart(), pabor);
			int end = posFromAcePos(e.getData().getRange().getEnd(), pabor);
			updateMarkersOnRemove(start, end, e.getData().getText());
		}
	}

	private void updateMarkersOnInsert(int start, int end, String text) {
		removeOverlappingFragile(start, end);

		for (AceMarker mm : markers.values()) {
			boolean moved = mm.moveOnInsert(start, end);
			if (moved) {
				mm.updateOnEditor(editor);
			}
		}
	}

	private void updateMarkersOnRemove(int start, int end, String text) {
		removeOverlappingFragile(start, end);

		LinkedList<String> zeroSized = new LinkedList<String>();
		for (Entry<String, AceMarker> e : markers.entrySet()) {
			AceMarker mm = e.getValue();
			boolean moved = mm.moveOnRemove(start, end);
			if (moved) {
				// SUGGESTION markers are only ones allowed to be 0-sized for now.
				// TODO: This different behavior of different types of markers is
				// a bit of a mess now, could think some way to clear it up... 
				if (mm.start == mm.end && mm.marker.getType()!=Marker.Type.SUGGESTION) {
					zeroSized.add(e.getKey());
				} else {
					mm.updateOnEditor(editor);
				}
			}
		}

		// Removing the zero-sized markers.
		// TODO: zero-sized markers should probably be allowed at least in some
		// cases,
		// and they should be shown, too.
		for (String rem : zeroSized) {
			removeMarker(rem);
		}

	}

	private void removeOverlappingFragile(int start, int end) {
		Collection<String> ofs = new LinkedList<String>();
		for (Entry<String, AceMarker> e : fragileMarkers.entrySet()) {
			if (e.getValue().touches(start, end)) {
				ofs.add(e.getKey());
			}
		}
		for (String markerId : ofs) {
			removeMarker(markerId);
		}
	}

	private void redrawMarkers() {
		// Could be done in a more sophisticated way...s
		Map<String, Marker> ms = getMarkers();
//		VConsole.log("redrawMarkers " + ms.size());
		clearMarkers();
		for (Entry<String, Marker> e : ms.entrySet()) {
//			VConsole.log("redrawMarker " + e.getKey());
			putMarker(e.getKey(), e.getValue());
		}
	}

	@Override
	public Command handleKeyboard(JavaScriptObject data, int hashId,
			String keyString, int keyCode, JavaScriptObject e) {
		if (wouldModifyLocked(hashId, keyString, keyCode)) {
			return Command.NULL;
		}
		return super.handleKeyboard(data, hashId, keyString, keyCode, e);
	}

	private boolean wouldModifyLocked(int hashId, String keyString, int keyCode) {
//		VConsole.log("wouldModifyLocked? " + hashId + ", " + keyString + ", "
//				+ keyCode + ".");
		if (isAllowedKeyOnLock(hashId, keyString, keyCode)) {
			return false;
		}

		GwtAceSelection sel = editor.getSelection();
		int start = posFromAcePos(sel.getRange().getStart(),
				getPosAtBeginningOfRows());
		int end = posFromAcePos(sel.getRange().getEnd(),
				getPosAtBeginningOfRows());

		if (modifiesLocked(start, end, keyCode)) {
			return true;
		}

		return false;
	}

	private static boolean isAllowedKeyOnLock(int hashId, String keyString,
			int keyCode) {
		return (isMoveKeyCode(keyCode) ||
		// TODO: These shortcuts are probably not the same on Mac
				(hashId == 1 && keyString.equals("a")) /* Ctrl-A */
				|| (hashId == 1 && keyString.equals("c")) /* Ctrl-C */
				|| (hashId == 1 && keyString.equals("z")) /* Ctrl-Z */
		|| (hashId == 1 && keyString.equals("y")) /* Ctrl-Y */);
	}

	private static boolean isMoveKeyCode(int keyCode) {
		return keyCode >= 37 && keyCode <= 40;
	}

	private boolean modifiesLocked(int start, int end, int keyCode) {
		for (AceMarker lock : lockMarkers.values()) {
			if (lock.overlaps(start, end)) {
				return true;
			}
			// del at the start of lock?
			else if (end == lock.start) {
				return !(keyCode == 8 || (keyCode == 46 && start != end));
			}
			// backspace at the end of the lock?
			else if (start == end && start == lock.end) {
				return keyCode != 46;
			}
		}
		return false;
	}
}
