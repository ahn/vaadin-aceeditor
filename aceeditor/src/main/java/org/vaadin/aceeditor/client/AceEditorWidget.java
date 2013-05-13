package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.AceMarker.OnTextChange;
import org.vaadin.aceeditor.client.TransportDoc.TransportRange;
import org.vaadin.aceeditor.client.gwt.GwtAceAnnotation;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeCursorHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeEvent.Data.Action;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeSelectionHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceEditor;
import org.vaadin.aceeditor.client.gwt.GwtAceEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceFocusBlurHandler;
import org.vaadin.aceeditor.client.gwt.GwtAcePosition;
import org.vaadin.aceeditor.client.gwt.GwtAceRange;
import org.vaadin.aceeditor.client.gwt.GwtAceSelection;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * A {@link com.google.gwt.user.client.ui.Widget} containing
 * {@link org.vaadin.aceeditor.client.gwt.GwtAceEditor}
 */
public class AceEditorWidget extends FocusWidget implements
		GwtAceChangeHandler, GwtAceFocusBlurHandler,
		GwtAceChangeSelectionHandler, GwtAceChangeCursorHandler {

	public interface ChangeListener {
		public void changed();
		public void selectionChanged();
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

	private class MarkerInEditor {
		private AceMarker marker;
		private String clientId;
		private MarkerInEditor(AceMarker marker, String clientId) {
			this.marker = marker;
			this.clientId = clientId;
		}
	}
	
	private class AnnotationInEditor {
		private int row;
		private AceAnnotation ann;
		private String markerId;
		private AnnotationInEditor(AceAnnotation ann, String markerId) {
			this.ann = ann;
			this.markerId = markerId;
		}
	}
	
	private GwtAceEditor editor;

	private String editorId;

	private static int idCounter = 0;

	private String text = null;
	private boolean readOnly = false;
	private boolean propertyReadOnly = false;
	private boolean focused;
	private TransportRange selection;

	// key: marker markerId
	private Map<String,MarkerInEditor> markersInEditor = Collections.emptyMap();

	private Set<RowAnnotation> rowAnnsInEditor = Collections.emptySet();
	private Set<AnnotationInEditor> markerAnnsInEditor = Collections.emptySet();

	private boolean settingText = false;

	private Set<MarkerAnnotation> markerAnnotations = Collections.emptySet();
	private Set<RowAnnotation> rowAnnotations = Collections.emptySet();
	
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

	private void setText(String text) {
		if (!isInitialized()) {
			return;
		}
		
		if (text == null) {
			text = "";
		}
		
		if (!text.equals(this.text)) {
			this.text = text;
			settingText = true;
			editor.setText(text);
			settingText = false;
		}
	}

	public void setSelection(TransportRange s) {
		if (!isInitialized()) {
			return;
		}
		if (s==null || selection.equals(s)) {
			return;
		}
		
		selection = s;
		
		int r1 = s.row1;
		int c1 = s.col1;
		int r2 = s.row2;
		int c2 = s.col2;
		boolean backwards = r1 > r2 || (r1 == r2 && c1 > c2);
		GwtAceRange range;
		if (backwards) {
			range = GwtAceRange.create(r2, c2, r1, c1);
		} else {
			range = GwtAceRange.create(r1, c1, r2, c2);
		}
		editor.setSelection(range, backwards);
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

	public void setMarkers(Map<String, AceMarker> markers) {
		if (!isInitialized()) {
			return;
		}
		
		HashMap<String,MarkerInEditor> newMarkers = new HashMap<String,MarkerInEditor>();
		for (Entry<String, AceMarker> e : markers.entrySet()) {
			String mId = e.getKey();
			AceMarker m = e.getValue();
			MarkerInEditor existing = markersInEditor.get(mId);
			if (existing!=null) {
				editor.removeMarker(existing.clientId);
			}
			String clientId = editor.addMarker(convertRange(m.getRange()), m.getCssClass(), m.getType().toString(), m.isInFront());
			existing = new MarkerInEditor(m, clientId);
			newMarkers.put(mId, existing);
		}
		
		
		for (MarkerInEditor hehe : markersInEditor.values()) {
			if (!newMarkers.containsKey(hehe.marker.getMarkerId())) {
				editor.removeMarker(hehe.clientId);
			}
		}
		
		markersInEditor = newMarkers;
		adjustMarkerAnnotations();
	}
	
	private void adjustMarkerAnnotations() {
		for (AnnotationInEditor aie : markerAnnsInEditor) {
			int row = rowOfMarker(aie.markerId);
			if (row!=-1) {
				aie.row = row;
			}
		}
		setAnnotationsToEditor();
		if (changeListener!=null) {
			changeListener.changed();
		}
	}
	
	public void setRowAnnotations(Set<RowAnnotation> ranns) {
		if (!isInitialized()) {
			return;
		}
		if (ranns==null) {
			return;
		}
		rowAnnotations = ranns;
		rowAnnsInEditor = ranns;
		setAnnotationsToEditor();
	}
	
	private void setAnnotationsToEditor() {
		JsArray<GwtAceAnnotation> arr = GwtAceAnnotation.createEmptyArray();
		for (AnnotationInEditor maie : markerAnnsInEditor) {
			GwtAceAnnotation jsAnn = GwtAceAnnotation.create(maie.ann.getType().toString(), maie.ann.getMessage(), maie.row);
			arr.push(jsAnn);
		}
		for (RowAnnotation ra : rowAnnsInEditor) {
			AceAnnotation a = ra.getAnnotation();
			GwtAceAnnotation jsAnn = GwtAceAnnotation.create(a.getType().toString(), a.getMessage(), ra.getRow());
			arr.push(jsAnn);
		}
		editor.setAnnotations(arr);
	}
	
	public void setMarkerAnnotations(Set<MarkerAnnotation> manns) {
		if (!isInitialized()) {
			return;
		}
		if (manns==null) {
			return;
		}
		markerAnnotations = manns;
		markerAnnsInEditor = createAIEfromMA(manns);
		setAnnotationsToEditor();
	}
	
	
	
	private Set<AnnotationInEditor> createAIEfromMA(
			Set<MarkerAnnotation> anns) {
		Set<AnnotationInEditor> adjusted = new HashSet<AnnotationInEditor>();
		for (MarkerAnnotation a : anns) {
			int row = rowOfMarker(a.getMarkerId());
			if (row!=-1) {
				AnnotationInEditor maie = new AnnotationInEditor(a.getAnnotation(), a.getMarkerId());
				maie.row = row;
				adjusted.add(maie);
			}
		}
		return adjusted;
	}
	
	private int rowOfMarker(String markerId) {
		MarkerInEditor cm = markersInEditor.get(markerId);
		if (cm==null) {
			return -1;
		}
		return cm.marker.getRange().getStartRow();
	}

	@Override
	public void onChange(GwtAceChangeEvent e) {
		if (settingText) {
			return;
		}
		String newText = editor.getText();
		if (newText.equals(text)) {
			return;
		}
		adjustMarkers(e);
		adjustMarkerAnnotations();
		text = newText;
		if (changeListener != null) {
			changeListener.changed();
		}
	}

	private void adjustMarkers(GwtAceChangeEvent e) {
		Action act = e.getData().getAction();
		GwtAceRange range = e.getData().getRange();
		Set<MarkerInEditor> moved = new HashSet<MarkerInEditor>();
		Set<MarkerInEditor> removed = new HashSet<MarkerInEditor>();
		
		if (act==Action.insertLines || act==Action.insertText) {
			for (MarkerInEditor cm : markersInEditor.values()) {
				if (cm.marker.getOnChange()==OnTextChange.ADJUST) {
					AceRange newRange = moveMarkerOnInsert(cm.marker.getRange(), range);
					if (newRange!=null) {
						cm.marker = cm.marker.withNewPosition(newRange);
						if (markerIsValid(cm.marker)) {
							moved.add(cm);
						}
						else {
							removed.add(cm);
						}
					}
				}
				else if (cm.marker.getOnChange()==OnTextChange.REMOVE) {
					removed.add(cm);
				}
			}
		}
		else if (act==Action.removeLines || act==Action.removeText) {
			for (MarkerInEditor cm : markersInEditor.values()) {
				if (cm.marker.getOnChange()==OnTextChange.ADJUST) {
					AceRange newRange = moveMarkerOnRemove(cm.marker.getRange(), range);
					if (newRange!=null) {
						cm.marker = cm.marker.withNewPosition(newRange);
						if (markerIsValid(cm.marker)) {
							moved.add(cm);
						}
						else {
							removed.add(cm);
						}
					}
				}
				else if (cm.marker.getOnChange()==OnTextChange.REMOVE) {
					removed.add(cm);
				}
			}
		}
		
		removeMarkers(removed);
		updateMarkers(moved);
	}
	
	private static boolean markerIsValid(AceMarker marker) {
		AceRange r = marker.getRange();
		return !r.isZeroLength() && !r.isBackwards() && r.getStartRow() >= 0 && r.getStartCol() >= 0 && r.getEndCol() >= 0; // no need to check endrow
	}

//	private void removeAllMarkers() {
//		if (!markersInEditor.isEmpty()) {
//			for (TransportMarker m : markersInEditor.values()) {
//				editor.removeMarker(m.clientId);
//			}
//			markersInEditor.clear();
//			if (changeListener!=null) {
//				changeListener.changed();
//			}
//			adjustMarkerAnnotations();
//		}
//	}
	
	private static AceRange moveMarkerOnInsert(AceRange mr, GwtAceRange range) {
		int startRow = range.getStart().getRow();
		int startCol = range.getStart().getColumn();
		int dRow = range.getEnd().getRow() - startRow;
		int dCol = range.getEnd().getColumn() - startCol;
		
		if (dRow==0 && dCol==0) {
			return null;
		}
		
		if (range.getStart().getRow() > mr.getEndRow()) {
			return null;
		}
		
		boolean aboveMarkerStart = startRow < mr.getStartRow();
		boolean beforeMarkerStartOnRow = startRow == mr.getStartRow() && startCol <= mr.getStartCol();
		boolean aboveMarkerEnd = startRow < mr.getEndRow();
		boolean beforeMarkerEndOnRow = startRow == mr.getEndRow() && startCol < mr.getEndCol();	
		
		int row1 = mr.getStartRow();
		int col1 = mr.getStartCol();
		if (aboveMarkerStart) {
			row1 += dRow;
		}
		else if (beforeMarkerStartOnRow) {
			row1 += dRow;
			col1 += dCol;
		}
		
		int row2 = mr.getEndRow();
		int col2 = mr.getEndCol();
		if (aboveMarkerEnd) {
			row2 += dRow;
		}
		else if (beforeMarkerEndOnRow) {
			row2 += dRow;
			col2 += dCol;
		}
		
		return new AceRange(row1, col1, row2, col2);
	}
	
	private static AceRange moveMarkerOnRemove(AceRange mr, GwtAceRange range) {
		int[] p1 = overlapping(range, mr.getStartRow(), mr.getStartCol());
		boolean changed = false;
		if (p1 == null) {
			p1 = new int[]{mr.getStartRow(), mr.getStartCol()};
		}
		else {
			changed = true;
		}
		
		int[] p2 = overlapping(range, mr.getEndRow(), mr.getEndCol());
		if (p2 == null) {
			p2 = new int[]{mr.getEndRow(), mr.getEndCol()};
		}
		else {
			changed = true;
		}
		
		return changed ? new AceRange(p1[0], p1[1], p2[0], p2[1]) : null;
	}
	
	private static int[] overlapping(GwtAceRange range, int row, int col) {
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
	
	private void removeMarkers(Set<MarkerInEditor> removed) {
		for (MarkerInEditor cm : removed) {
			editor.removeMarker(cm.clientId);
			markersInEditor.remove(cm.marker.getMarkerId());
		}
	}
	
	private void updateMarkers(Set<MarkerInEditor> moved) {
		for (MarkerInEditor cm : moved) {
			editor.removeMarker(cm.clientId);
			AceMarker m = cm.marker;
			cm.clientId = editor.addMarker(convertRange(m.getRange()), m.getCssClass(), m.getType().toString(), m.isInFront());
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

	private static TransportRange convertSelection(GwtAceSelection selection) {
		GwtAcePosition start = selection.getRange().getStart();
		GwtAcePosition end = selection.getRange().getEnd();
		if (selection.isBackwards()) {
			return new TransportRange(end.getRow(), end.getColumn(), start.getRow(),
					start.getColumn());
		} else {
			return new TransportRange(start.getRow(), start.getColumn(),
					end.getRow(), end.getColumn());
		}

	}

	public TransportRange getSelection() {
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
		TransportRange sel = convertSelection(editor.getSelection());
		if (!sel.equals(selection)) {
			selection = sel;
			if (changeListener!=null) {
				changeListener.selectionChanged();
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
	
	private GwtAceRange convertRange(AceRange r) {
		int r1 = r.getStartRow();
		int c1 = r.getStartCol();
		int r2 = r.getEndRow();
		int c2 = r.getEndCol();
		boolean backwards = r1 > r2 || (r1 == r2 && c1 > c2);
		if (backwards) {
			return GwtAceRange.create(r2, c2, r1, c1);
		} else {
			return GwtAceRange.create(r1, c1, r2, c2);
		}
	}

	private Map<String, AceMarker> getMarkers() {
		HashMap<String, AceMarker> markers = new HashMap<String, AceMarker>();
		for (MarkerInEditor cm : markersInEditor.values()) {
			markers.put(cm.marker.getMarkerId(), cm.marker);
		}
		return markers;
	}

	public void resize() {
		if (editor!=null) {
			editor.resize();
		}
	}

	
	
	public AceDoc getDoc() {
		// TODO: cache?
		
		return new AceDoc(getText(), getMarkers(), getRowAnnotations(), getMarkerAnnotations());
	}

	
	private Set<MarkerAnnotation> getMarkerAnnotations() {
		return markerAnnotations;
	}

	private Set<RowAnnotation> getRowAnnotations() {
		return rowAnnotations;
	}

	public void setDoc(AceDoc doc) {
		setText(doc.getText());
		setMarkers(doc.getMarkers());
		setMarkerAnnotations(doc.getMarkerAnnotations());
		setRowAnnotations(doc.getRowAnnotations());
	}



	

}
