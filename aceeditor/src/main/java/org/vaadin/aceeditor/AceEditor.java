package org.vaadin.aceeditor;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EventObject;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.aceeditor.client.AceAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceEditorClientRpc;
import org.vaadin.aceeditor.client.AceEditorServerRpc;
import org.vaadin.aceeditor.client.AceEditorState;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceMarker.OnTextChange;
import org.vaadin.aceeditor.client.AceMarker.Type;
import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.TransportDiff;
import org.vaadin.aceeditor.client.TransportDoc.TransportRange;
import org.vaadin.aceeditor.client.Util;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.BlurNotifier;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.FocusNotifier;
import com.vaadin.shared.Registration;
import com.vaadin.ui.AbstractField;
import com.vaadin.util.ReflectTools;

/**
 *
 * AceEditor wraps an Ace code editor inside a TextField-like Vaadin component.
 *
 */
@SuppressWarnings("serial")
@JavaScript({ "client/js/ace/ace.js", "client/js/ace/ext-searchbox.js",
"client/js/diff_match_patch.js" })
@StyleSheet("client/css/ace-gwt.css")
public class AceEditor extends AbstractField<String> implements BlurNotifier,
FocusNotifier {

	private String value;

	public static class DiffEvent extends Event {
		public static String EVENT_ID = "aceeditor-diff";
		private final ServerSideDocDiff diff;

		public DiffEvent(final AceEditor ed, final ServerSideDocDiff diff) {
			super(ed);
			this.diff = diff;
		}

		public ServerSideDocDiff getDiff() {
			return this.diff;
		}
	}

	public interface DiffListener extends Serializable {
		Method diffMethod = ReflectTools.findMethod(
				DiffListener.class, "diff", DiffEvent.class);

		void diff(DiffEvent e);
	}

	public static class SelectionChangeEvent extends Event {
		public static final String EVENT_ID = "aceeditor-selection";
		private final TextRange selection;

		public SelectionChangeEvent(final AceEditor ed) {
			super(ed);
			this.selection = ed.getSelection();
		}

		public TextRange getSelection() {
			return this.selection;
		}
	}

	public interface SelectionChangeListener extends Serializable {
		Method selectionChangedMethod = ReflectTools
				.findMethod(SelectionChangeListener.class, "selectionChanged",
						SelectionChangeEvent.class);

		void selectionChanged(SelectionChangeEvent e);
	}

	public static class TextChangeEventImpl extends EventObject {
		private final TextRange selection;
		private final String text;

		private TextChangeEventImpl(final AceEditor ace, final String text) {
			super(ace);
			this.text = text;
			this.selection = ace.getSelection();
		}

		public int getCursorPosition() {
			return this.selection.getEnd();
		}

		public String getText() {
			return this.text;
		}
	}

	// By default, using the version 1.1.9 of Ace from GitHub via rawgit.com.
	// It's recommended to host the Ace files yourself as described in README.
	private static final String DEFAULT_ACE_PATH = "//cdn.rawgit.com/ajaxorg/ace-builds/e3ccd2c654cf45ee41ffb09d0e7fa5b40cf91a8f/src-min-noconflict";

	private AceDoc doc = new AceDoc();

	private boolean isFiringTextChangeEvent;

	private boolean latestFocus = false;
	private long latestMarkerId = 0L;

	private static final Logger logger = Logger.getLogger(AceEditor.class
			.getName());

	private boolean onRoundtrip = false;

	private final AceEditorServerRpc rpc = new AceEditorServerRpc() {
		@Override
		public void changed(final TransportDiff diff, final TransportRange trSelection,
				final boolean focused) {
			AceEditor.this.clientChanged(diff, trSelection, focused);
		}

		@Override
		public void changedDelayed(final TransportDiff diff,
				final TransportRange trSelection, final boolean focused) {
			AceEditor.this.clientChanged(diff, trSelection, focused);
		}
	};

	private TextRange selection = new TextRange("", 0, 0, 0, 0);
	// {startPos,endPos} or {startRow,startCol,endRow,endCol}
	private Integer[] selectionToClient = null;
	private AceDoc shadow = new AceDoc();

	{
		AceEditor.logger.setLevel(Level.WARNING);
	}

	public AceEditor() {
		super();
		this.setWidth("300px");
		this.setHeight("200px");

		this.setModePath(AceEditor.DEFAULT_ACE_PATH);
		this.setThemePath(AceEditor.DEFAULT_ACE_PATH);
		this.setWorkerPath(AceEditor.DEFAULT_ACE_PATH);

		this.registerRpc(this.rpc);
	}

	@Override
	protected void doSetValue(final String s) {
		this.value = s;
	}

	public void addDiffListener(final DiffListener listener) {
		this.addListener(DiffEvent.EVENT_ID, DiffEvent.class, listener,
				DiffListener.diffMethod);
	}

	@Override
	public Registration addFocusListener(final FocusListener listener) {
		final Registration registration = this.addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener,
				FocusListener.focusMethod);
		this.getState().listenToFocusChanges = true;
		return registration;
	}

	@Override
	public Registration addBlurListener(final BlurListener listener) {
		final Registration registration = this.addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener,
				BlurListener.blurMethod);
		this.getState().listenToFocusChanges = true;
		return registration;
	}

	/**
	 * Adds an ace marker. The id of the marker must be unique within this
	 * editor.
	 *
	 * @param marker
	 * @return marker id
	 */
	public String addMarker(final AceMarker marker) {
		this.doc = this.doc.withAdditionalMarker(marker);
		this.markAsDirty();
		return marker.getMarkerId();
	}

	/**
	 * Adds an ace marker with a generated id. The id is unique within this
	 * editor.
	 *
	 * @param range
	 * @param cssClass
	 * @param type
	 * @param inFront
	 * @param onChange
	 * @return marker id
	 */
	public String addMarker(final AceRange range, final String cssClass, final Type type,
			final boolean inFront, final OnTextChange onChange) {
		return this.addMarker(new AceMarker(this.newMarkerId(), range, cssClass, type,
				inFront, onChange));
	}

	public void addMarkerAnnotation(final AceAnnotation ann, final AceMarker marker) {
		this.addMarkerAnnotation(ann, marker.getMarkerId());
	}

	public void addMarkerAnnotation(final AceAnnotation ann, final String markerId) {
		this.doc = this.doc.withAdditionalMarkerAnnotation(new MarkerAnnotation(markerId,
				ann));
		this.markAsDirty();
	}

	public void addRowAnnotation(final AceAnnotation ann, final int row) {
		this.doc = this.doc.withAdditionalRowAnnotation(new RowAnnotation(row, ann));
		this.markAsDirty();
	}

	public void addSelectionChangeListener(final SelectionChangeListener listener) {
		this.addListener(SelectionChangeEvent.EVENT_ID, SelectionChangeEvent.class,
				listener, SelectionChangeListener.selectionChangedMethod);
		this.getState().listenToSelectionChanges = true;
	}

	@Override
	public void beforeClientResponse(final boolean initial) {
		super.beforeClientResponse(initial);
		if (initial) {
			this.getState().initialValue = this.doc.asTransport();
			this.shadow = this.doc;
		} else if (this.onRoundtrip) {
			final ServerSideDocDiff diff = ServerSideDocDiff.diff(this.shadow, this.doc);
			this.shadow = this.doc;
			final TransportDiff td = diff.asTransport();
			this.getRpcProxy(AceEditorClientRpc.class).diff(td);

			this.onRoundtrip = false;
		} else if (true /* TODO !shadow.equals(doc) */) {
			this.getRpcProxy(AceEditorClientRpc.class).changedOnServer();
		}

		if (this.selectionToClient != null) {
			// {startPos,endPos}
			if (this.selectionToClient.length == 2) {
				final AceRange r = AceRange.fromPositions(this.selectionToClient[0],
						this.selectionToClient[1], this.doc.getText());
				this.getState().selection = r.asTransport();
			}
			// {startRow,startCol,endRow,endCol}
			else if (this.selectionToClient.length == 4) {
				final TransportRange tr = new TransportRange(this.selectionToClient[0],
						this.selectionToClient[1], this.selectionToClient[2],
						this.selectionToClient[3]);
				this.getState().selection = tr;
			}
			this.selectionToClient = null;
		}
	}

	public void clearMarkerAnnotations() {
		final Set<MarkerAnnotation> manns = Collections.emptySet();
		this.doc = this.doc.withMarkerAnnotations(manns);
		this.markAsDirty();
	}

	public void clearMarkers() {
		this.doc = this.doc.withoutMarkers();
		this.markAsDirty();
	}

	public void clearRowAnnotations() {
		final Set<RowAnnotation> ranns = Collections.emptySet();
		this.doc = this.doc.withRowAnnotations(ranns);
		this.markAsDirty();
	}

	public int getCursorPosition() {
		return this.selection.getEnd();
	}

	public AceDoc getDoc() {
		return this.doc;
	}

	public TextRange getSelection() {
		return this.selection;
	}

	public Class<? extends String> getType() {
		return String.class;
	}

	@SuppressWarnings("deprecation")
	public void removeDiffListener(final DiffListener listener) {
		this.removeListener(DiffEvent.EVENT_ID, DiffEvent.class, listener);
	}

	@SuppressWarnings("deprecation")
	public void removeFocusListener(final FocusListener listener) {
		this.removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
		this.getState().listenToFocusChanges = !this.getListeners(FocusEvent.class)
				.isEmpty() || !this.getListeners(BlurEvent.class).isEmpty();
	}

	@SuppressWarnings("deprecation")
	public void removeBlurListener(final BlurListener listener) {
		this.removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
		this.getState().listenToFocusChanges = !this.getListeners(FocusEvent.class)
				.isEmpty() || !this.getListeners(BlurEvent.class).isEmpty();
	}


	public void removeMarker(final AceMarker marker) {
		this.removeMarker(marker.getMarkerId());
	}

	public void removeMarker(final String markerId) {
		this.doc = this.doc.withoutMarker(markerId);
		this.markAsDirty();
	}

	@SuppressWarnings("deprecation")
	public void removeSelectionChangeListener(final SelectionChangeListener listener) {
		this.removeListener(SelectionChangeEvent.EVENT_ID,
				SelectionChangeEvent.class, listener);
		this.getState().listenToSelectionChanges = true; //The AceEditor must listen always for the correct cursorposition
	}

	public void setBasePath(final String path) {
		this.setAceConfig("basePath", path);
	}

	/**
	 * Sets the cursor position to be pos characters from the beginning of the
	 * text.
	 *
	 * @param pos
	 */
	public void setCursorPosition(final int pos) {
		this.setSelection(pos, pos);
	}

	/**
	 * Sets the cursor on the given row and column.
	 *
	 * @param row
	 *            starting from 0
	 * @param col
	 *            starting from 0
	 */
	public void setCursorRowCol(final int row, final int col) {
		this.setSelectionRowCol(row, col, row, col);
	}

	public void setDoc(final AceDoc doc) {
		if (this.doc.equals(doc)) {
			return;
		}
		this.doc = doc;
		final boolean wasReadOnly = this.isReadOnly();
		this.setReadOnly(false);
		this.setValue(doc.getText());
		this.setReadOnly(wasReadOnly);
		this.markAsDirty();
	}

	public void setMode(final AceMode mode) {
		this.getState().mode = mode.toString();
	}

	public void setMode(final String mode) {
		this.getState().mode = mode;
	}

	public void setModePath(final String path) {
		this.setAceConfig("modePath", path);
	}

	/**
	 * Sets the selection to be between characters [start,end).
	 *
	 * The cursor will be at the end.
	 *
	 * @param start
	 * @param end
	 */
	public void setSelection(final int start, final int end) {
		this.setSelectionToClient(new Integer[] { start, end });
		this.setInternalSelection(new TextRange(this.getValue(), start, end));
	}

	/**
	 * Sets the selection to be between the given (startRow,startCol) and
	 * (endRow, endCol).
	 *
	 * The cursor will be at the end.
	 *
	 * @param startRow
	 *            starting from 0
	 * @param startCol
	 *            starting from 0
	 * @param endRow
	 *            starting from 0
	 * @param endCol
	 *            starting from 0
	 */
	public void setSelectionRowCol(final int startRow, final int startCol, final int endRow,
			final int endCol) {
		this.setSelectionToClient(new Integer[] { startRow, startCol, endRow, endCol });
		this.setInternalSelection(new TextRange(this.doc.getText(), startRow, startCol,
				endRow, endCol));
	}

	/**
	 * Scrolls to the given row. First row is 0.
	 *
	 */
	public void scrollToRow(final int row) {
		this.getState().scrollToRow = row;
	}

	/**
	 * Scrolls the to the given position (characters from the start of the
	 * file).
	 *
	 */
	public void scrollToPosition(final int pos) {
		final int[] rowcol = Util.lineColFromCursorPos(this.getValue(), pos, 0);
		this.scrollToRow(rowcol[0]);
	}

	public void setTheme(final AceTheme theme) {
		this.getState().theme = theme.toString();
	}

	public void setTheme(final String theme) {
		this.getState().theme = theme;
	}

	public void setThemePath(final String path) {
		this.setAceConfig("themePath", path);
	}

	public void setUseWorker(final boolean useWorker) {
		this.getState().useWorker = useWorker;
	}

	public void setWordWrap(final boolean ww) {
		this.getState().wordwrap = ww;
	}

	public void setShowGutter(final boolean showGutter) {
		this.getState().showGutter = showGutter;
	}

	public boolean isShowGutter() {
		return this.getState(false).showGutter;
	}

	public void setShowPrintMargin(final boolean showPrintMargin) {
		this.getState().showPrintMargin = showPrintMargin;
	}

	public boolean isShowPrintMargin() {
		return this.getState(false).showPrintMargin;
	}

	public void setHighlightActiveLine(final boolean highlightActiveLine) {
		this.getState().highlightActiveLine = highlightActiveLine;
	}

	public boolean isHighlightActiveLine() {
		return this.getState(false).highlightActiveLine;
	}

	public void setWorkerPath(final String path) {
		this.setAceConfig("workerPath", path);
	}

	/**
	 * Use "auto" if you want to detect font size from CSS
	 *
	 * @param size
	 *            auto or font size
	 */
	public void setFontSize(final String size) {
		this.getState().fontSize = size;
	}

	public String getFontSize() {
		return this.getState(false).fontSize;
	}

	public void setHighlightSelectedWord(final boolean highlightSelectedWord) {
		this.getState().highlightSelectedWord = highlightSelectedWord;
	}

	public boolean isHighlightSelectedWord() {
		return this.getState(false).highlightSelectedWord;
	}

	public void setShowInvisibles(final boolean showInvisibles) {
		this.getState().showInvisibles = showInvisibles;
	}

	public boolean isShowInvisibles() {
		return this.getState(false).showInvisibles;
	}

	public void setDisplayIndentGuides(final boolean displayIndentGuides) {
		this.getState().displayIndentGuides = displayIndentGuides;
	}

	public boolean isDisplayIndentGuides() {
		return this.getState(false).displayIndentGuides;
	}

	public void setTabSize(final int size) {
		this.getState().tabSize = size;
	}

	public void setUseSoftTabs(final boolean softTabs) {
		this.getState().softTabs = softTabs;
	}

	protected void clientChanged(final TransportDiff diff, final TransportRange trSelection,
			final boolean focused) {
		this.diffFromClient(diff);
		this.selectionFromClient(trSelection);
		if (this.latestFocus != focused) {
			this.latestFocus = focused;
			if (focused) {
				this.fireFocus();
			} else {
				this.fireBlur();
			}
		}

		this.clearStateFromServerToClient();
	}

	// Here we clear the selection etc. we sent earlier.
	// The client has already received the values,
	// and we must clear them at some point to not keep
	// setting the same selection etc. over and over.
	private void clearStateFromServerToClient() {
		this.getState().selection = null;
		this.getState().scrollToRow = -1;
	}

	@Override
	protected AceEditorState getState() {
		return (AceEditorState) super.getState();
	}

	@Override
	protected AceEditorState getState(final boolean markAsDirty) {
		return (AceEditorState) super.getState(markAsDirty);
	}

	@Override
	public void setValue(final String newValue) {
		super.setValue(newValue);
		this.doc = this.doc.withText(newValue);
	}

	@Override
	public String getValue() {
		return this.value;
	}

	private void diffFromClient(final TransportDiff d) {
		final String previousText = this.doc.getText();
		final ServerSideDocDiff diff = ServerSideDocDiff.fromTransportDiff(d);
		this.shadow = diff.applyTo(this.shadow);
		this.doc = diff.applyTo(this.doc);
		if (!TextUtils.equals(this.doc.getText(), previousText)) {
			this.setValue(this.doc.getText(), true);
			this.fireTextChangeEvent();
		}
		if (!diff.isIdentity()) {
			this.fireDiff(diff);
		}
		this.onRoundtrip = true;
		this.markAsDirty();
	}

	private void fireBlur() {
		this.fireEvent(new BlurEvent(this));
	}

	private void fireDiff(final ServerSideDocDiff diff) {
		this.fireEvent(new DiffEvent(this, diff));
	}

	private void fireFocus() {
		this.fireEvent(new FocusEvent(this));
	}

	private void fireSelectionChanged() {
		this.fireEvent(new SelectionChangeEvent(this));
	}

	private void fireTextChangeEvent() {
		if (!this.isFiringTextChangeEvent) {
			this.isFiringTextChangeEvent = true;
			try {
				this.fireEvent(new TextChangeEventImpl(this, this.getValue()));
			} finally {
				this.isFiringTextChangeEvent = false;
			}
		}
	}

	private String newMarkerId() {
		return "m" + (++this.latestMarkerId);
	}

	private void selectionFromClient(final TransportRange sel) {
		final TextRange newSel = new TextRange(this.doc.getText(),
				AceRange.fromTransport(sel));
		if (newSel.equals(this.selection)) {
			return;
		}
		this.setInternalSelection(newSel);
		this.fireSelectionChanged();
	}

	private void setAceConfig(final String key, final String value) {
		this.getState().config.put(key, value);
	}

	private void setInternalSelection(final TextRange selection) {
		this.selection = selection;
		this.getState().selection = selection.asTransport();
	}

	private void setSelectionToClient(final Integer[] stc) {
		this.selectionToClient = stc;
		this.markAsDirty();
	}

}
