package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.AceMarker.OnTextChange;
import org.vaadin.aceeditor.client.ClientSideDocDiff.Adjuster;
import org.vaadin.aceeditor.client.gwt.GwtAceAnnotation;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeCursorHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeEvent.Data.Action;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceChangeSelectionHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceEditor;
import org.vaadin.aceeditor.client.gwt.GwtAceEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceFocusBlurHandler;
import org.vaadin.aceeditor.client.gwt.GwtAceKeyboardHandler;
import org.vaadin.aceeditor.client.gwt.GwtAcePosition;
import org.vaadin.aceeditor.client.gwt.GwtAceRange;
import org.vaadin.aceeditor.client.gwt.GwtAceSelection;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * A {@link com.google.gwt.user.client.ui.Widget} containing
 * {@link org.vaadin.aceeditor.client.gwt.GwtAceEditor}
 */
public class AceEditorWidget extends FocusWidget implements
GwtAceChangeHandler, GwtAceFocusBlurHandler,
GwtAceChangeSelectionHandler, GwtAceChangeCursorHandler {

	public interface TextChangeListener {
		public void changed();
	}
	public interface SelectionChangeListener {
		public void selectionChanged();
	}

	public interface FocusChangeListener {
		public void focusChanged(boolean focused);
	}

	protected LinkedList<TextChangeListener> changeListeners = new LinkedList<>();
	public void addTextChangeListener(final TextChangeListener li) {
		this.changeListeners.add(li);
	}
	public void removeTextChangeListener(final TextChangeListener li) {
		this.changeListeners.remove(li);
	}

	protected LinkedList<SelectionChangeListener> selChangeListeners = new LinkedList<>();
	public void addSelectionChangeListener(final SelectionChangeListener li) {
		this.selChangeListeners.add(li);
	}
	public void removeSelectionChangeListener(final SelectionChangeListener li) {
		this.selChangeListeners.remove(li);
	}

	protected FocusChangeListener focusChangeListener;
	public void setFocusChangeListener(final FocusChangeListener li) {
		this.focusChangeListener = li;
	}

	protected class MarkerInEditor {
		protected AceMarker marker;
		protected String clientId;
		protected MarkerInEditor(final AceMarker marker, final String clientId) {
			this.marker = marker;
			this.clientId = clientId;
		}
	}

	protected class AnnotationInEditor {
		protected int row;
		protected AceAnnotation ann;
		protected String markerId;
		protected AnnotationInEditor(final AceAnnotation ann, final String markerId) {
			this.ann = ann;
			this.markerId = markerId;
		}
	}

	protected GwtAceEditor editor;

	protected String editorId;

	protected static int idCounter = 0;

	protected String text = "";
	protected boolean enabled = true;
	protected boolean readOnly = false;
	protected boolean propertyReadOnly = false;
	protected boolean focused;
	protected AceRange selection = new AceRange(0,0,0,0);

	// key: marker markerId
	protected Map<String,MarkerInEditor> markersInEditor = Collections.emptyMap();

	protected Set<RowAnnotation> rowAnnsInEditor = Collections.emptySet();
	protected Set<AnnotationInEditor> markerAnnsInEditor = Collections.emptySet();

	protected Map<Integer, AceRange> invisibleMarkers = new HashMap<>();
	protected int latestInvisibleMarkerId = 0;

	protected boolean ignoreEditorEvents = false;

	protected Set<MarkerAnnotation> markerAnnotations = Collections.emptySet();
	protected Set<RowAnnotation> rowAnnotations = Collections.emptySet();

	protected GwtAceKeyboardHandler keyboardHandler;

	protected AceDoc doc;

	protected static String nextId() {
		return "_AceEditorWidget_" + (++AceEditorWidget.idCounter);
	}

	public AceEditorWidget() {
		super(DOM.createDiv());
		this.editorId = AceEditorWidget.nextId();
		this.setStylePrimaryName("AceEditorWidget");

	}

	public boolean isInitialized() {
		return this.editor != null;
	}

	public void initialize() {
		this.editor = GwtAceEditor.create(this.getElement(), this.editorId);
		this.editor.addChangeHandler(this);
		this.editor.addFocusListener(this);
		this.editor.addChangeSelectionHandler(this);
		this.editor.addChangeCursorHandler(this);
		if (this.keyboardHandler!=null) {
			this.editor.setKeyboardHandler(this.keyboardHandler);
		}
	}

	public void setKeyboardHandler(final GwtAceKeyboardHandler handler) {
		this.keyboardHandler = handler;
		if (this.isInitialized()) {
			this.editor.setKeyboardHandler(handler);
		}
	}

	@Override
	public void setWidth(final String w) {
		super.setWidth(w);
		if (this.editor!=null) {
			this.editor.resize();
		}
	}

	@Override
	public void setHeight(final String h) {
		super.setHeight(h);
		if (this.editor!=null) {
			this.editor.resize();
		}
	}

	public void setWordwrap(final boolean wrap) {
		if (this.isInitialized()) {
			this.editor.setUseWrapMode(wrap);
		}
	}

	public void setShowGutter(final boolean showGutter) {
		if (this.isInitialized()) {
			this.editor.setShowGutter(showGutter);
		}
	}

	public void setShowPrintMargin(final boolean showPrintMargin) {
		if (this.isInitialized()) {
			this.editor.setShowPrintMargin(showPrintMargin);
		}
	}

	public void setHighlightActiveLineEnabled(final boolean highlightActiveLine) {
		if (this.isInitialized()) {
			this.editor.setHighlightActiveLineEnabled(highlightActiveLine);
		}
	}

	public void setDisplayIndentGuides(final boolean displayIndentGuides) {
		if (this.isInitialized()) {
			this.editor.setDisplayIndentGuides(displayIndentGuides);
		}
	}

	public void setUseSoftTabs(final boolean softTabs) {
		if (this.isInitialized()) {
			this.editor.setUseSoftTabs(softTabs);
		}
	}

	public void setTabSize(final int tabSize) {
		if (this.isInitialized()) {
			this.editor.setTabSize(tabSize);
		}
	}

	protected void setText(final String text) {
		if (!this.isInitialized() || this.text.equals(text)) {
			return;
		}
		final AceRange oldSelection = this.selection;
		final Adjuster adjuster = new Adjuster(this.text, text);
		this.adjustInvisibleMarkersOnTextChange(adjuster);
		this.text = text;
		this.doc = null;
		this.ignoreEditorEvents = true;
		final double wasAtRow = this.editor.getScrollTopRow();
		this.editor.setText(text);
		final AceRange adjSel = adjuster.adjust(oldSelection);
		this.setSelection(adjSel, true);
		this.editor.scrollToRow(wasAtRow);
		this.ignoreEditorEvents = false;
	}


	protected void adjustInvisibleMarkersOnTextChange(final Adjuster adjuster) {
		final HashMap<Integer, AceRange> ims = new HashMap<>(this.invisibleMarkers.size());
		for (final Entry<Integer, AceRange> e : this.invisibleMarkers.entrySet()) {
			ims.put(e.getKey(), adjuster.adjust(e.getValue()));
		}
		this.invisibleMarkers = ims;
	}

	public void setSelection(final AceRange s) {
		this.setSelection(s, false);
	}

	protected void setSelection(final AceRange s, final boolean force) {
		if (!this.isInitialized()) {
			return;
		}
		if (s.equals(this.selection) && !force) {
			return;
		}

		this.selection = s;

		final int r1 = s.getStartRow();
		final int c1 = s.getStartCol();
		final int r2 = s.getEndRow();
		final int c2 = s.getEndCol();
		final boolean backwards = r1 > r2 || (r1 == r2 && c1 > c2);
		GwtAceRange range;
		if (backwards) {
			range = GwtAceRange.create(r2, c2, r1, c1);
		} else {
			range = GwtAceRange.create(r1, c1, r2, c2);
		}
		this.editor.setSelection(range, backwards);
	}

	public void setMode(final String mode) {
		if (!this.isInitialized()) {
			return;
		}
		this.editor.setMode(mode);
	}

	public void setTheme(final String theme) {
		if (!this.isInitialized()) {
			return;
		}
		this.editor.setTheme(theme);
	}

	public void setFontSize(final String fontSize)
	{
		if (!this.isInitialized()) {
			return;
		}
		this.editor.setFontSize(fontSize);
	}

	public void setHighlightSelectedWord(final boolean highlightSelectedWord) {
		if (!this.isInitialized()) {
			return;
		}
		this.editor.setHighlightSelectedWord(highlightSelectedWord);
	}

	protected void setMarkers(final Map<String, AceMarker> markers) {
		if (!this.isInitialized()) {
			return;
		}

		final HashMap<String,MarkerInEditor> newMarkers = new HashMap<>();
		for (final Entry<String, AceMarker> e : markers.entrySet()) {
			final String mId = e.getKey();
			final AceMarker m = e.getValue();
			MarkerInEditor existing = this.markersInEditor.get(mId);
			if (existing!=null) {
				this.editor.removeMarker(existing.clientId);
			}
			final String type = (m.getType()==AceMarker.Type.cursor ? "text" :
				(m.getType()==AceMarker.Type.cursorRow ? "line" : m.getType().toString()));
			final String clientId = this.editor.addMarker(this.convertRange(m.getRange()), m.getCssClass(), type, m.isInFront());
			existing = new MarkerInEditor(m, clientId);
			newMarkers.put(mId, existing);
		}


		for (final MarkerInEditor hehe : this.markersInEditor.values()) {
			if (!newMarkers.containsKey(hehe.marker.getMarkerId())) {
				this.editor.removeMarker(hehe.clientId);
			}
		}

		this.markersInEditor = newMarkers;
		this.adjustMarkerAnnotations();
	}

	protected void adjustMarkerAnnotations() {
		boolean changed = false;
		for (final AnnotationInEditor aie : this.markerAnnsInEditor) {
			final int row = this.rowOfMarker(aie.markerId);
			if (row!=-1 && row != aie.row) {
				aie.row = row;
				changed = true;
			}
		}
		if (changed) {
			this.setAnnotationsToEditor();
		}
	}

	protected void setAnnotations(final Set<MarkerAnnotation> manns, final Set<RowAnnotation> ranns) {
		if (!this.isInitialized()) {
			return;
		}
		if (manns!=null) {
			this.markerAnnotations = manns;
			this.markerAnnsInEditor = this.createAIEfromMA(manns);
		}
		if (ranns!=null) {
			this.rowAnnotations = ranns;
			this.rowAnnsInEditor = ranns;
		}
		this.setAnnotationsToEditor();
	}

	protected void setAnnotationsToEditor() {
		final JsArray<GwtAceAnnotation> arr = GwtAceAnnotation.createEmptyArray();

		final JsArray<GwtAceAnnotation> existing = this.editor.getAnnotations();

		for (int i=0; i<existing.length(); ++i) {
			final GwtAceAnnotation ann = existing.get(i);
			if (!ann.isVaadinAceEditorAnnotation()) {
				arr.push(ann);
			}
		}

		for (final AnnotationInEditor maie : this.markerAnnsInEditor) {
			final GwtAceAnnotation jsAnn = GwtAceAnnotation.create(maie.ann.getType().toString(), maie.ann.getMessage(), maie.row);
			arr.push(jsAnn);
		}
		for (final RowAnnotation ra : this.rowAnnsInEditor) {
			final AceAnnotation a = ra.getAnnotation();
			final GwtAceAnnotation jsAnn = GwtAceAnnotation.create(a.getType().toString(), a.getMessage(), ra.getRow());
			arr.push(jsAnn);
		}
		this.editor.setAnnotations(arr);
	}

	protected Set<AnnotationInEditor> createAIEfromMA(
			final Set<MarkerAnnotation> anns) {
		final Set<AnnotationInEditor> adjusted = new HashSet<>();
		for (final MarkerAnnotation a : anns) {
			final int row = this.rowOfMarker(a.getMarkerId());
			if (row!=-1) {
				final AnnotationInEditor maie = new AnnotationInEditor(a.getAnnotation(), a.getMarkerId());
				maie.row = row;
				adjusted.add(maie);
			}
		}
		return adjusted;
	}

	protected int rowOfMarker(final String markerId) {
		final MarkerInEditor cm = this.markersInEditor.get(markerId);
		if (cm==null) {
			return -1;
		}
		return cm.marker.getRange().getStartRow();
	}

	@Override
	public void onChange(final GwtAceChangeEvent e) {
		if (this.ignoreEditorEvents) {
			return;
		}
		final String newText = this.editor.getText();
		if (newText.equals(this.text)) {
			return;
		}

		// TODO: do we do too much work here?
		// most of the time the editor doesn't have any markers nor annotations...

		this.adjustMarkers(e);
		this.adjustInvisibleMarkers(e);
		this.adjustMarkerAnnotations();
		this.text = newText;
		this.doc = null;
		this.fireTextChanged();
	}

	public void fireTextChanged() {
		for (final TextChangeListener li : this.changeListeners) {
			li.changed();
		}
	}

	protected void adjustMarkers(final GwtAceChangeEvent e) {
		final Action act = e.getData().getAction();
		final GwtAceRange range = e.getData().getRange();
		final Set<MarkerInEditor> moved = new HashSet<>();
		final Set<MarkerInEditor> removed = new HashSet<>();

		if (act==Action.insertLines || act==Action.insertText) {
			for (final MarkerInEditor cm : this.markersInEditor.values()) {
				if (cm.marker.getOnChange()==OnTextChange.ADJUST) {
					AceRange newRange = AceEditorWidget.moveMarkerOnInsert(cm.marker.getRange(), range);
					if (newRange!=null) {
						newRange = this.cursorMarkerSanityCheck(cm.marker, newRange);
						cm.marker = cm.marker.withNewPosition(newRange);
						if (AceEditorWidget.markerIsValid(cm.marker)) {
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
			for (final MarkerInEditor cm : this.markersInEditor.values()) {
				if (cm.marker.getOnChange()==OnTextChange.ADJUST) {
					AceRange newRange = AceEditorWidget.moveMarkerOnRemove(cm.marker.getRange(), range);
					if (newRange!=null) {
						newRange = this.cursorMarkerSanityCheck(cm.marker, newRange);
						cm.marker = cm.marker.withNewPosition(newRange);
						if (AceEditorWidget.markerIsValid(cm.marker)) {
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

		this.removeMarkers(removed);
		this.updateMarkers(moved);
	}

	private AceRange cursorMarkerSanityCheck(final AceMarker m, final AceRange r) {
		if (m.getType()==AceMarker.Type.cursorRow && r.getEndRow() > r.getStartRow() + 1) {
			return new AceRange(r.getStartRow(), 0, r.getStartRow()+1, 0);
		}
		if (m.getType()==AceMarker.Type.cursor &&
				(r.getStartRow() != r.getEndRow() || r.getEndCol() > r.getStartCol() +1 )) {
			return new AceRange(r.getEndRow(), r.getEndCol(), r.getEndRow(), r.getEndCol() + 1);
		}

		return r;
	}
	protected void adjustInvisibleMarkers(final GwtAceChangeEvent event) {
		final Action act = event.getData().getAction();
		final GwtAceRange range = event.getData().getRange();
		final HashMap<Integer, AceRange> newMap = new HashMap<>();
		if (act==Action.insertLines || act==Action.insertText) {
			for (final Entry<Integer, AceRange> e : this.invisibleMarkers.entrySet()) {
				final AceRange newRange = AceEditorWidget.moveMarkerOnInsert(e.getValue(), range);
				newMap.put(e.getKey(), newRange==null?e.getValue():newRange);
			}
		}
		else if (act==Action.removeLines || act==Action.removeText) {
			for (final Entry<Integer, AceRange> e : this.invisibleMarkers.entrySet()) {
				final AceRange newRange = AceEditorWidget.moveMarkerOnRemove(e.getValue(), range);
				newMap.put(e.getKey(), newRange==null?e.getValue():newRange);
			}
		}
		this.invisibleMarkers = newMap;
	}

	protected static boolean markerIsValid(final AceMarker marker) {
		final AceRange r = marker.getRange();
		return !r.isZeroLength() && !r.isBackwards() && r.getStartRow() >= 0 && r.getStartCol() >= 0 && r.getEndCol() >= 0; // no need to check endrow
	}

	protected static AceRange moveMarkerOnInsert(final AceRange mr, final GwtAceRange range) {
		final int startRow = range.getStart().getRow();
		final int startCol = range.getStart().getColumn();
		final int dRow = range.getEnd().getRow() - startRow;
		final int dCol = range.getEnd().getColumn() - startCol;

		if (dRow==0 && dCol==0) {
			return null;
		}

		if (range.getStart().getRow() > mr.getEndRow()) {
			return null;
		}

		final boolean aboveMarkerStart = startRow < mr.getStartRow();
		final boolean beforeMarkerStartOnRow = startRow == mr.getStartRow() && startCol < mr.getStartCol(); // < or <=
		final boolean aboveMarkerEnd = startRow < mr.getEndRow();
		final boolean beforeMarkerEndOnRow = startRow == mr.getEndRow() && startCol <= mr.getEndCol();	 // < or <=

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

	protected static AceRange moveMarkerOnRemove(final AceRange mr, final GwtAceRange range) {
		int[] p1 = AceEditorWidget.overlapping(range, mr.getStartRow(), mr.getStartCol());
		boolean changed = false;
		if (p1 == null) {
			p1 = new int[]{mr.getStartRow(), mr.getStartCol()};
		}
		else {
			changed = true;
		}

		int[] p2 = AceEditorWidget.overlapping(range, mr.getEndRow(), mr.getEndCol());
		if (p2 == null) {
			p2 = new int[]{mr.getEndRow(), mr.getEndCol()};
		}
		else {
			changed = true;
		}

		return changed ? new AceRange(p1[0], p1[1], p2[0], p2[1]) : null;
	}

	protected static int[] overlapping(final GwtAceRange range, final int row, final int col) {
		final GwtAcePosition start = range.getStart();

		if (start.getRow() > row || (start.getRow() == row && start.getColumn() >= col)) {
			return null;
		}

		final GwtAcePosition end = range.getEnd();

		if (end.getRow() < row) {
			final int dRow = end.getRow() - start.getRow();
			return new int[] {row-dRow, col};
		}
		if (end.getRow() == row && end.getColumn() < col) {
			final int dRow = end.getRow() - start.getRow();
			final int dCol = end.getColumn() - start.getColumn();
			return new int[] {row-dRow, col-dCol};
		}
		return new int[] {start.getRow(), start.getColumn()};
	}

	protected void removeMarkers(final Set<MarkerInEditor> removed) {
		for (final MarkerInEditor cm : removed) {
			this.editor.removeMarker(cm.clientId);
			this.markersInEditor.remove(cm.marker.getMarkerId());
		}
	}

	protected void updateMarkers(final Set<MarkerInEditor> moved) {
		for (final MarkerInEditor cm : moved) {
			this.editor.removeMarker(cm.clientId);
			final AceMarker m = cm.marker;
			cm.clientId = this.editor.addMarker(this.convertRange(m.getRange()), m.getCssClass(), m.getType().toString(), m.isInFront());
		}

	}

	public String getText() {
		return this.text;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (!this.isInitialized()) {
			return;
		}
		this.enabled = enabled;
		this.updateEditorReadOnlyState();
	}

	public void setPropertyReadOnly(final boolean propertyReadOnly) {
		if (!this.isInitialized()) {
			return;
		}
		this.propertyReadOnly = propertyReadOnly;
		this.updateEditorReadOnlyState();
	}

	public void setReadOnly(final boolean readOnly) {
		if (!this.isInitialized()) {
			return;
		}
		this.readOnly = readOnly;
		this.updateEditorReadOnlyState();
	}

	private void updateEditorReadOnlyState() {
		this.editor.setReadOnly(this.readOnly || this.propertyReadOnly || !this.enabled);
	}

	public void setShowInvisibles(final boolean showInvisibles) {
		this.editor.setShowInvisibles(showInvisibles);
	}

	protected static AceRange convertSelection(final GwtAceSelection selection) {
		final GwtAcePosition start = selection.getRange().getStart();
		final GwtAcePosition end = selection.getRange().getEnd();
		if (selection.isBackwards()) {
			return new AceRange(end.getRow(), end.getColumn(), start.getRow(),
					start.getColumn());
		} else {
			return new AceRange(start.getRow(), start.getColumn(),
					end.getRow(), end.getColumn());
		}

	}

	public AceRange getSelection() {
		return this.selection;
	}

	@Override
	public void onFocus(final GwtAceEvent e) {
		if (this.focused) {
			return;
		}
		this.focused = true;
		if (this.focusChangeListener != null) {
			this.focusChangeListener.focusChanged(true);
		}
	}

	@Override
	public void onBlur(final GwtAceEvent e) {
		if (!this.focused) {
			return;
		}
		this.focused = false;
		if (this.focusChangeListener != null) {
			this.focusChangeListener.focusChanged(false);
		}
	}

	@Override
	public void onChangeSelection(final GwtAceEvent e) {
		this.selectionChanged();
	}

	@Override
	public void onChangeCursor(final GwtAceEvent e) {
		this.selectionChanged();
	}

	protected void selectionChanged() {
		if (this.ignoreEditorEvents) {
			return;
		}
		final AceRange sel = AceEditorWidget.convertSelection(this.editor.getSelection());
		if (!sel.equals(this.selection)) {
			this.selection = sel;
			for (final SelectionChangeListener li : this.selChangeListeners) {
				li.selectionChanged();
			}
		}
	}

	public void setUseWorker(final boolean use) {
		if (!this.isInitialized()) {
			return;
		}
		this.editor.setUseWorker(use);
	}

	@Override
	public void setFocus(final boolean focused) {
		super.setFocus(focused);
		if (focused) {
			this.editor.focus();
		}
		else {
			this.editor.blur();
		}
		// Waiting for the event from editor to update 'focused'.
	}

	public boolean isFocused() {
		return this.focused;
	}

	protected GwtAceRange convertRange(final AceRange r) {
		final int r1 = r.getStartRow();
		final int c1 = r.getStartCol();
		final int r2 = r.getEndRow();
		final int c2 = r.getEndCol();
		final boolean backwards = r1 > r2 || (r1 == r2 && c1 > c2);
		if (backwards) {
			return GwtAceRange.create(r2, c2, r1, c1);
		} else {
			return GwtAceRange.create(r1, c1, r2, c2);
		}
	}

	protected Map<String, AceMarker> getMarkers() {
		final HashMap<String, AceMarker> markers = new HashMap<>();
		for (final MarkerInEditor cm : this.markersInEditor.values()) {
			markers.put(cm.marker.getMarkerId(), cm.marker);
		}
		return markers;
	}

	public void resize() {
		if (this.editor!=null) {
			this.editor.resize();
		}
	}

	public AceDoc getDoc() {
		if (this.doc==null) {
			this.doc = new AceDoc(this.getText(), this.getMarkers(), this.getRowAnnotations(), this.getMarkerAnnotations());
		}
		return this.doc;
	}

	public void scrollToRow(final int row) {
		this.editor.scrollToRow(row);
	}

	protected Set<MarkerAnnotation> getMarkerAnnotations() {
		return this.markerAnnotations;
	}

	protected Set<RowAnnotation> getRowAnnotations() {
		return this.rowAnnotations;
	}

	public void setDoc(final AceDoc doc) {
		if (doc.equals(this.doc)) {
			return;
		}

		this.setText(doc.getText());

		// Too much work is done in the case there
		// are no markers or annotations, which is probably most of the time...
		// TODO: optimize

		this.setMarkers(doc.getMarkers());
		this.setAnnotations(doc.getMarkerAnnotations(), doc.getRowAnnotations());
		this.doc = doc;
	}

	public int[] getCursorCoords() {
		final JsArrayInteger cc = this.editor.getCursorCoords();
		return new int[] {cc.get(0), cc.get(1)};
	}

	public int addInvisibleMarker(final AceRange range) {
		final int id = ++this.latestInvisibleMarkerId;
		this.invisibleMarkers.put(id, range);
		return id;
	}

	public void removeInvisibleMarker(final int id) {
		this.invisibleMarkers.remove(id);
	}

	public AceRange getInvisibleMarker(final int id) {
		return this.invisibleMarkers.get(id);
	}

	public void setTextAndAdjust(final String text) {
		if (this.text.equals(text)) {
			return;
		}

		final HashMap<String, AceMarker> newMarkers = this.adjustMarkersOnTextChange(this.text, text);
		this.setText(text);
		if (newMarkers!=null) {
			this.setMarkers(newMarkers);
		}
	}

	protected HashMap<String, AceMarker> adjustMarkersOnTextChange(final String text1, final String text2) {
		final Map<String, AceMarker> ms = this.getMarkers();
		if (ms.isEmpty()) {
			return null;
		}
		final HashMap<String, AceMarker> newMarkers = new HashMap<>();
		final Adjuster adjuster = new Adjuster(text1, text2);
		boolean adjusted = false;
		for (final Entry<String, AceMarker> e : ms.entrySet()) {
			if (e.getValue().getOnChange()==OnTextChange.ADJUST) {
				final AceMarker m1 = e.getValue();
				final AceMarker m2 = m1.withNewPosition(adjuster.adjust(m1.getRange()));
				newMarkers.put(e.getKey(), m2);
				adjusted = true;
			}
			else {
				newMarkers.put(e.getKey(), e.getValue());
			}
		}
		if (!adjusted) {
			return null;
		}
		return newMarkers;
	}

	public void removeContentsOfInvisibleMarker(final int imId) {
		final AceRange r = this.getInvisibleMarker(imId);
		if (r==null || r.isZeroLength()) {
			return;
		}
		final String newText = Util.replaceContents(r, this.text, "");
		this.setTextAndAdjust(newText);
	}
}