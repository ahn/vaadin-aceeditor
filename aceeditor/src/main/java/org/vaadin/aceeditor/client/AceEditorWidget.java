package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.gwt.GwtAceAnnotation;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeCursorHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeSelectionHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceEditor;
import org.vaadin.aceeditor.client.gwt.GwtAceEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceFocusBlurHandler;
import org.vaadin.aceeditor.client.gwt.GwtAcePosition;
import org.vaadin.aceeditor.client.gwt.GwtAceRange;
import org.vaadin.aceeditor.client.gwt.GwtAceSelection;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeEvent.Data.Action;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusWidget;
import com.vaadin.client.VConsole;

/**
 * A {@link com.google.gwt.user.client.ui.Widget} containing
 * {@link org.vaadin.aceeditor.client.gwt.GwtAceEditor}
 */
public class AceEditorWidget extends FocusWidget implements
		GwtAceChangeHandler, GwtAceFocusBlurHandler,
		GwtAceChangeSelectionHandler, GwtAceChangeCursorHandler {

	public interface ChangeListener {
		public void changed();
		public void markersChanged();
	}

	public interface FocusChangeListener {
		public void focusChanged(boolean focused);
	}

	private ChangeListener changeListener;

	public void setChangeListener(ChangeListener li) {
		changeListener = li;
	}

	private FocusChangeListener focusChangeListener;

	public void setFocusChangeListener(FocusChangeListener li) {
		focusChangeListener = li;
	}

	private class ClientMarker {
		private AceMarker marker;
		private String clientId;
		private ClientMarker(AceMarker marker, String clientId) {
			this.marker = marker;
			this.clientId = clientId;
		}
	}
	
	private GwtAceEditor editor;

	private String editorId;

	private static int idCounter = 0;

	private String text = null;
	private boolean readOnly = false;
	private boolean propertyReadOnly = false;
	private boolean focused;
	private AceClientRange selection;

	private boolean listenSelections;

	// key: marker serverId
	private Map<Long,ClientMarker> markersInEditor = Collections.emptyMap();

	private Set<AceClientAnnotation> markerAnnotations = null;
	
	private static String nextId() {
		return "_AceEditorWidget_" + (++idCounter);
	}

	public AceEditorWidget() {
		super(DOM.createDiv());
		this.editorId = nextId();
		this.setStylePrimaryName("AceEditorWidget");
		
	}
	
	public boolean isInitialized() {
		return editor!=null;
	}
	
	public void initialize() {
		editor = GwtAceEditor.create(this.getElement(), editorId);
		editor.addChangeHandler(this);
		editor.addFocusListener(this);
		editor.addChangeSelectionHandler(this);
		editor.addChangeCursorHandler(this);
	}
	
	@Override
	public void setWidth(String w) {
		super.setWidth(w);
		if (editor!=null) {
			editor.resize();
		}
	}
	
	@Override
	public void setHeight(String h) {
		super.setHeight(h);
		if (editor!=null) {
			editor.resize();
		}
	}
	
	public void setWordwrap(boolean wrap) {
		if (isInitialized()) { 
			editor.setUseWrapMode(wrap);
		}
	}

	public void setText(String text) {
		if (!isInitialized()) {
			return;
		}
		
		// XXX:
		// Not setting text when focused. This is for (among other things?) to prevent
		// my own older values appearing when they arrive from the server.
		// TODO: a better solution for this would be better.
		if (focused) {
			return;
		}
		
		if (text == null) {
			text = "";
		}
		
		if (!text.equals(this.text)) {
			removeAllMarkers();
			this.text = text;
			editor.setText(text);
		}
	}

	public void setMode(String mode) {
		if (!isInitialized()) {
			return;
		}
		editor.setMode(mode);
	}

	public void setTheme(String theme) {
		if (!isInitialized()) {
			return;
		}
		editor.setTheme(theme);
	}

	public void setMarkers(List<AceMarker> markers) {
		if (!isInitialized()) {
			return;
		}
		
		HashMap<Long,ClientMarker> mmm = new HashMap<Long,ClientMarker>();
		for (AceMarker m : markers) {
			ClientMarker existing = markersInEditor.get(m.serverId);
			if (existing==null) {
				String clientId = editor.addMarker(convertRange(m.range), m.cssClass, m.type.toString(), m.inFront);
				existing = new ClientMarker(m, clientId);
			}
			mmm.put(existing.marker.serverId, existing);
		}
		
		
		for (ClientMarker hehe : markersInEditor.values()) {
			if (!mmm.containsKey(hehe.marker.serverId)) {
				editor.removeMarker(hehe.clientId);
			}
		}
		
		markersInEditor = mmm;
		
		if (markerAnnotations != null) {
			markerAnnotations = adjustedAnnotations(markerAnnotations);
			setAnnotationsToEditor(markerAnnotations);
		}
	}
	
	public void setRowAnnotations(Set<AceClientAnnotation> ranns) {
		if (!isInitialized()) {
			return;
		}
		if (ranns==null) {
			return;
		}
		
		this.markerAnnotations = null;
		
		setAnnotationsToEditor(ranns);
	}
	
	private void setAnnotationsToEditor(Set<AceClientAnnotation> annotations) {
		JsArray<GwtAceAnnotation> arr = GwtAceAnnotation.createEmptyArray();
		for (AceClientAnnotation ann : annotations) {
			GwtAceAnnotation jsAnn = GwtAceAnnotation.create(ann.type.toString(), ann.text, ann.row);
			arr.push(jsAnn);
		}
		
		editor.setAnnotations(arr);
	}
	
	public void setMarkerAnnotations(Set<AceClientAnnotation> manns) {
		if (!isInitialized()) {
			return;
		}
		if (manns==null) {
			return;
		}
		
		markerAnnotations = adjustedAnnotations(manns);
		
		setAnnotationsToEditor(markerAnnotations);
	}
	
	private Set<AceClientAnnotation> adjustedAnnotations(
			Set<AceClientAnnotation> annotations) {
		Set<AceClientAnnotation> adjusted = new HashSet<AceClientAnnotation>();
		for (AceClientAnnotation a : annotations) {
			if (a.markerId>0) {
				ClientMarker cm = markersInEditor.get(a.markerId);
				if (cm!=null) {
					a.row = cm.marker.range.getStartRow();
					adjusted.add(a);
				}
			}
			else {
				adjusted.add(a);
			}
		}
		return adjusted;
	}

	@Override
	public void onChange(GwtAceChangeEvent e) {
		String newText = editor.getText();
		if (newText.equals(text)) {
			return;
		}
		adjustMarkers(e);
		if (markerAnnotations!=null) {
			markerAnnotations = adjustedAnnotations(markerAnnotations);
			setAnnotationsToEditor(markerAnnotations);
		}
		text = newText;
		if (changeListener != null) {
			changeListener.changed();
		}

	}

	private void adjustMarkers(GwtAceChangeEvent e) {
		Action act = e.getData().getAction();
		GwtAceRange range = e.getData().getRange();
		Set<ClientMarker> moved = new HashSet<ClientMarker>();
		Set<ClientMarker> removed = new HashSet<ClientMarker>();
		
		if (act==Action.insertLines || act==Action.insertText) {
			for (ClientMarker cm : markersInEditor.values()) {
				AceMarker m = cm.marker;
				VConsole.log("adjusting " + m);
				if (m.onChange==AceMarker.OnTextChange.ADJUST) {
					if (moveMarkerOnInsert(m, range)) {
						moved.add(cm);
					}
				}
				else if (m.onChange==AceMarker.OnTextChange.REMOVE) {
					removed.add(cm);
				}
			}
		}
		else if (act==Action.removeLines || act==Action.removeText) {
			for (ClientMarker cm : markersInEditor.values()) {
				AceMarker m = cm.marker;
				if (m.onChange==AceMarker.OnTextChange.ADJUST) {
					if (moveMarkerOnRemove(m, range)) {
						moved.add(cm);
					}
				}
				else if (m.onChange==AceMarker.OnTextChange.REMOVE) {
					removed.add(cm);
				}
			}
		}
		
		removeMarkers(removed);
		updateMarkers(moved);
		
		if (!removed.isEmpty() || !moved.isEmpty()) {
			if (changeListener != null) {
				changeListener.markersChanged();
			}
		}
	}
	
	private void removeAllMarkers() {
		if (!markersInEditor.isEmpty()) {
			for (ClientMarker m : markersInEditor.values()) {
				editor.removeMarker(m.clientId);
			}
			markersInEditor.clear();
			if (changeListener!=null) {
				changeListener.markersChanged();
			}
		}
	}
	
	private boolean moveMarkerOnInsert(AceMarker m, GwtAceRange range) {
		int startRow = range.getStart().getRow();
		int startCol = range.getStart().getColumn();
		int dRow = range.getEnd().getRow() - startRow;
		int dCol = range.getEnd().getColumn() - startCol;
		
		if (dRow==0 && dCol==0) {
			return false;
		}
		
		if (range.getStart().getRow() > m.range.getStartRow()) {
			return false;
		}
		
		boolean aboveMarkerStart = startRow < m.range.getStartRow();
		boolean beforeMarkerStartOnRow = startRow == m.range.getStartRow() && startCol <= m.range.getStartCol();
		boolean aboveMarkerEnd = startRow < m.range.getEndRow();
		boolean beforeMarkerEndOnRow = startRow == m.range.getEndRow() && startCol < m.range.getEndCol();	
		
		if (aboveMarkerStart) {
			m.range.setStartRow(m.range.getStartRow() + dRow);
		}
		else if (beforeMarkerStartOnRow) {
			m.range.setStartRow(m.range.getStartRow() + dRow);
			m.range.setStartCol(m.range.getStartCol() + dCol);
		}
		
		if (aboveMarkerEnd) {
			m.range.setEndRow(m.range.getEndRow() + dRow);
		}
		else if (beforeMarkerEndOnRow) {
			m.range.setEndRow(m.range.getEndRow() + dRow);
			m.range.setEndCol(m.range.getEndCol() + dCol);
		}
		
		return true; // TODO???
	}
	
	private boolean moveMarkerOnRemove(AceMarker m, GwtAceRange range) {
		int[] p1 = overlapping(range, m.range.getStartRow(), m.range.getStartCol());
		if (p1 != null) {
			m.range.setStartRow(p1[0]);
			m.range.setStartCol(p1[1]);
		}
		
		int[] p2 = overlapping(range,m.range.getEndRow(), m.range.getEndCol());
		if (p2 != null) {
			m.range.setEndRow(p2[0]);
			m.range.setEndCol(p2[1]);
		}
		
		return p1 != null | p2 != null;
	}
	
	private int[] overlapping(GwtAceRange range, int row, int col) {
		GwtAcePosition start = range.getStart();
		
		if (start.getRow() > row || (start.getRow() == row && start.getColumn() >= col)) {
			return null;
		}
		
		GwtAcePosition end = range.getEnd();
		
		if (end.getRow() < row) {
			int dRow = end.getRow() - start.getRow();
			return new int[] {row-dRow, col};
		}
		if (end.getRow() == row && end.getColumn() < col) {
			int dRow = end.getRow() - start.getRow();
			int dCol = end.getColumn() - start.getColumn();
			return new int[] {row-dRow, col-dCol};
		}
		return new int[] {start.getRow(), start.getColumn()};
	}
	
	private void removeMarkers(Set<ClientMarker> removed) {
		for (ClientMarker cm : removed) {
			editor.removeMarker(cm.clientId);
			markersInEditor.remove(cm.marker.serverId);
		}
	}
	
	private void updateMarkers(Set<ClientMarker> moved) {
		for (ClientMarker cm : moved) {
			VConsole.log("UPDATING "+cm.clientId+" "+cm.marker);
			editor.removeMarker(cm.clientId);
			AceMarker m = cm.marker;
			cm.clientId = editor.addMarker(convertRange(m.range), m.cssClass, m.type.toString(), m.inFront);
			VConsole.log("UPDATED "+cm.clientId+" "+cm.marker);
		}
		
	}

	public String getText() {
		return text;
	}

	public void setPropertyReadOnly(boolean propertyReadOnly) {
		if (!isInitialized()) {
			return;
		}
		this.propertyReadOnly = propertyReadOnly;
		editor.setReadOnly(this.readOnly || this.propertyReadOnly);
	}

	public void setReadOnly(boolean readOnly) {
		if (!isInitialized()) {
			return;
		}
		this.readOnly = readOnly;
		editor.setReadOnly(this.readOnly || this.propertyReadOnly);
	}

	private static AceClientRange convertSelection(GwtAceSelection selection) {
		GwtAcePosition start = selection.getRange().getStart();
		GwtAcePosition end = selection.getRange().getEnd();
		if (selection.isBackwards()) {
			return new AceClientRange(end.getRow(), end.getColumn(), start.getRow(),
					start.getColumn());
		} else {
			return new AceClientRange(start.getRow(), start.getColumn(),
					end.getRow(), end.getColumn());
		}

	}

	public AceClientRange getSelection() {
		return selection;
	}

	@Override
	public void onFocus(GwtAceEvent e) {
		if (focused) {
			return;
		}
		focused = true;
		if (focusChangeListener != null) {
			focusChangeListener.focusChanged(true);
		}
	}

	@Override
	public void onBlur(GwtAceEvent e) {
		if (!focused) {
			return;
		}
		focused = false;
		if (focusChangeListener != null) {
			focusChangeListener.focusChanged(false);
		}
	}

	@Override
	public void onChangeSelection(GwtAceEvent e) {
		selectionChanged();
	}

	@Override
	public void onChangeCursor(GwtAceEvent e) {
		selectionChanged();
	}

	private void selectionChanged() {
		AceClientRange sel = convertSelection(editor.getSelection());
		if (!sel.equals(selection)) {
			selection = sel;
			if (listenSelections) {
				changeListener.changed();
			}
		}
	}
	
	public void setUseWorker(boolean use) {
		if (!isInitialized()) {
			return;
		}
		editor.setUseWorker(use);
	}

	public boolean isFocused() {
		return focused;
	}

	public void setListenToSelectionChanges(boolean listen) {
		listenSelections = listen;
	}

	public void setSelection(AceClientRange s) {
		if (!isInitialized()) {
			return;
		}
		if (selection.equals(s)) {
			return;
		}
		selection = s;
		
		int r1 = s.getStartRow();
		int c1 = s.getStartCol();
		int r2 = s.getEndRow();
		int c2 = s.getEndCol();
		boolean backwards = r1 > r2 || (r1 == r2 && c1 > c2);
		GwtAceRange range;
		if (backwards) {
			range = GwtAceRange.create(r2, c2, r1, c1);
		} else {
			range = GwtAceRange.create(r1, c1, r2, c2);
		}
		editor.setSelection(range, backwards);
	}
	
	private GwtAceRange convertRange(AceClientRange s) {
		int r1 = s.getStartRow();
		int c1 = s.getStartCol();
		int r2 = s.getEndRow();
		int c2 = s.getEndCol();
		boolean backwards = r1 > r2 || (r1 == r2 && c1 > c2);
		if (backwards) {
			return GwtAceRange.create(r2, c2, r1, c1);
		} else {
			return GwtAceRange.create(r1, c1, r2, c2);
		}
	}

	public List<AceMarker> getMarkers() {
		LinkedList<AceMarker> markers = new LinkedList<AceMarker>();
		for (ClientMarker cm : markersInEditor.values()) {
			markers.add(cm.marker);
		}
		return markers;
	}

	public Set<AceClientAnnotation> getMarkerAnnotations() {
		return markerAnnotations;
	}

	public void resize() {
		if (editor!=null) {
			editor.resize();
		}
	}


	

}
