package org.vaadin.aceeditor;

import java.lang.reflect.Method;
import java.util.Collections;
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

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.BlurNotifier;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.FieldEvents.FocusNotifier;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.FieldEvents.TextChangeNotifier;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.util.ReflectTools;

/**
 * 
 * AceEditor wraps an Ace code editor inside a TextField-like Vaadin component.
 *
 */
@SuppressWarnings("serial")
@JavaScript({"client/js/ace/ace.js", "client/js/diff_match_patch.js"})
@StyleSheet("client/css/ace-gwt.css")
public class AceEditor extends AbstractField<String> implements BlurNotifier,
		FocusNotifier, TextChangeNotifier {

	private static final String DEFAULT_ACE_PATH = "http://d1n0x3qji82z53.cloudfront.net/src-min-noconflict";
	
	private Logger logger = Logger.getLogger(AceEditor.class.getName());
	{
		logger.setLevel(Level.INFO);
	}
	
	public static class SelectionChangeEvent extends Event {
		public static String EVENT_ID = "aceeditor-selection";
		private final AceRange selection;

		public SelectionChangeEvent(AceEditor ed) {
			super(ed);
			this.selection = ed.getSelection();
		}

		public AceRange getSelection() {
			return selection;
		}
	}
	
	public interface SelectionChangeListener {
		public static final Method selectionChangedMethod = ReflectTools
				.findMethod(SelectionChangeListener.class, "selectionChanged",
						SelectionChangeEvent.class);

		public void selectionChanged(SelectionChangeEvent e);
	}
	
	public static class DiffEvent extends Event {
		public static String EVENT_ID = "aceeditor-diff";
		private final AceDocDiff diff;
		
		public DiffEvent(AceEditor ed, AceDocDiff diff) {
			super(ed);
			this.diff = diff;
		}
		
		public AceDocDiff getDiff() {
			return diff;
		}
	}
	
	public interface DiffListener {
		public static final Method diffMethod = ReflectTools
				.findMethod(DiffListener.class, "diff",
						DiffEvent.class);
		public void diff(DiffEvent e);
	}
	
	private AceDoc doc = new AceDoc();
	private AceDoc shadow = new AceDoc();

	private long latestMarkerId = 0L;
	
	private TextRange selection = new TextRange("", 0, 0, 0, 0);
	
	// {startPos,endPos} or {startRow,startCol,endRow,endCol}
	private Integer[] selectionToClient = null;

	private boolean isFiringTextChangeEvent;
	private boolean latestFocus = false;

	private AceEditorServerRpc rpc = new AceEditorServerRpc() {


		@Override
		public void changed(TransportDiff diff, TransportRange selection, boolean focused) {
			clientChanged(diff, selection, focused);
		}

		@Override
		public void changedDelayed(TransportDiff diff, TransportRange selection, boolean focused) {
			clientChanged(diff, selection, focused);
		}
		
	};
	
	private boolean onRoundTrip = false;
	
	protected void clientChanged(TransportDiff diff, TransportRange selection,
			boolean focused) {
		diffFromClient(diff);
		selectionFromClient(selection);
		if (latestFocus != focused) {
			latestFocus = focused;
			if (focused) {
				fireFocus();
			}
			else {
				fireBlur();
			}
		}
	}
	
	private void diffFromClient(TransportDiff d) {
		String previousText = doc.getText();
		AceDocDiff diff = AceDocDiff.fromTransportDiff(d);
		shadow = diff.applyTo(shadow);
		doc = diff.applyTo(doc);
		setInternalValue(doc.getText());
		if (!doc.getText().equals(previousText)) {
			fireTextChangeEvent();
		}
		if (!diff.isIdentity()) {
			fireDiff(diff);
		}
		onRoundTrip = true;
		markAsDirty();
	}
	
	private void selectionFromClient(TransportRange sel) {
		setInternalSelection(new TextRange(doc.getText(), AceRange.fromTransport(sel)));
		fireSelectionChanged();
	}

	public AceEditor() {
		super();
		setWidth("300px");
		setHeight("200px");

		setModePath(DEFAULT_ACE_PATH);
		setThemePath(DEFAULT_ACE_PATH);
		setWorkerPath(DEFAULT_ACE_PATH);

		registerRpc(rpc);
	}

	private void fireSelectionChanged() {
		fireEvent(new SelectionChangeEvent(this));
	}

	private void fireBlur() {
		fireEvent(new BlurEvent(this));
	}

	private void fireFocus() {
		fireEvent(new FocusEvent(this));
	}
	
	private void fireDiff(AceDocDiff diff) {
		fireEvent(new DiffEvent(this, diff));
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	protected AceEditorState getState() {
		return (AceEditorState) super.getState();
	}

	@Override
	protected void setInternalValue(String newValue) {
		super.setInternalValue(newValue);
		doc = doc.withText(newValue);
	}
	
	public void setDoc(AceDoc doc) {
		this.doc = doc;
		setValue(doc.getText());
		
		// TODO: Only needed if doc changed.
		markAsDirty();
	}
	
	@Override
	public void setValue(String newFieldValue) {
		super.setValue(newFieldValue);
	}

	@Override
	public void addTextChangeListener(TextChangeListener listener) {
		addListener(TextChangeListener.EVENT_ID, TextChangeEvent.class,
				listener, TextChangeListener.EVENT_METHOD);
	}

	@Override
	@Deprecated
	public void addListener(TextChangeListener listener) {
		addTextChangeListener(listener);
	}

	@Override
	public void removeTextChangeListener(TextChangeListener listener) {
		removeListener(TextChangeListener.EVENT_ID, TextChangeEvent.class,
				listener);
	}

	@Override
	@Deprecated
	public void removeListener(TextChangeListener listener) {
		removeTextChangeListener(listener);
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener,
				FocusListener.focusMethod);
	}

	@Override
	@Deprecated
	public void addListener(FocusListener listener) {
		addFocusListener(listener);
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
	}

	@Override
	@Deprecated
	public void removeListener(FocusListener listener) {
		removeFocusListener(listener);
	}

	@Override
	public void addBlurListener(BlurListener listener) {
		addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener,
				BlurListener.blurMethod);
	}

	@Override
	@Deprecated
	public void addListener(BlurListener listener) {
		addBlurListener(listener);
	}

	@Override
	public void removeBlurListener(BlurListener listener) {
		removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
	}

	@Override
	@Deprecated
	public void removeListener(BlurListener listener) {
		removeBlurListener(listener);
	}

	public void addSelectionChangeListener(SelectionChangeListener listener) {
		addListener(SelectionChangeEvent.EVENT_ID, SelectionChangeEvent.class,
				listener, SelectionChangeListener.selectionChangedMethod);
		getState().listenToSelectionChanges = true;
	}

	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		removeListener(SelectionChangeEvent.EVENT_ID,
				SelectionChangeEvent.class, listener);
		getState().listenToSelectionChanges = !getListeners(
				SelectionChangeEvent.class).isEmpty();
	}
	
	public void addDiffListener(DiffListener listener) {
		addListener(DiffEvent.EVENT_ID, DiffEvent.class,
				listener, DiffListener.diffMethod);
	}
	
	public void removeDiffListener(DiffListener listener) {
		removeListener(DiffEvent.EVENT_ID, DiffEvent.class, listener);
	}
	
	/**
	 * Adds an ace marker with a generated id. The id is unique within this editor.
	 * 
	 * @param range
	 * @param cssClass
	 * @param type
	 * @param inFront
	 * @param onChange
	 * @return marker id
	 */
	public String addMarker(AceRange range, String cssClass, Type type, boolean inFront, OnTextChange onChange) {
		return addMarker(new AceMarker(newMarkerId(), range, cssClass, type, inFront, onChange));
	}
	
	/**
	 * Adds an ace marker. The id of the marker must be unique within this editor.
	 * 
	 * @param marker
	 * @return marker id
	 */
	public String addMarker(AceMarker marker) {
		logger.info("addMarker("+marker+")");
		doc = doc.withAdditionalMarker(marker);
		markAsDirty();
		return marker.getMarkerId();
	}

	public void removeMarker(AceMarker marker) {
		removeMarker(marker.getMarkerId());
	}

	public void removeMarker(String markerId) {
		doc = doc.withoutMarker(markerId);
		markAsDirty();
	}
	
	public void clearMarkers() {
		doc = doc.withoutMarkers();
		markAsDirty();
	}

	public void addRowAnnotation(AceAnnotation ann, int row) {
		doc = doc.withAdditionalRowAnnotation(new RowAnnotation(row, ann));
		markAsDirty();
	}

	public void addMarkerAnnotation(AceAnnotation ann, AceMarker marker) {
		addMarkerAnnotation(ann, marker.getMarkerId());
	}

	public void addMarkerAnnotation(AceAnnotation ann, String markerId) {
		doc = doc.withAdditionalMarkerAnnotation(new MarkerAnnotation(markerId, ann));
		markAsDirty();
	}

	public void clearRowAnnotations() {
		Set<RowAnnotation> ranns = Collections.emptySet();
		doc = doc.withRowAnnotations(ranns);
		markAsDirty();
	}

	public void clearMarkerAnnotations() {
		Set<MarkerAnnotation> manns = Collections.emptySet();
		doc = doc.withMarkerAnnotations(manns);
		markAsDirty();
	}

	private String newMarkerId() {
		return "TODO"+(++latestMarkerId); // TODO
	}

	/**
	 * Sets the mode how the TextField triggers {@link TextChangeEvent}s.
	 * 
	 * @param inputEventMode
	 *            the new mode
	 * 
	 * @see TextChangeEventMode
	 */
	public void setTextChangeEventMode(TextChangeEventMode inputEventMode) {
		getState().changeMode = inputEventMode.toString();
	}

	/**
	 * The text change timeout modifies how often text change events are
	 * communicated to the application when {@link #setTextChangeEventMode} is
	 * {@link TextChangeEventMode#LAZY} or {@link TextChangeEventMode#TIMEOUT}.
	 * 
	 *
	 * @param timeoutMs
	 *            the timeout in milliseconds
	 */
	public void setTextChangeTimeout(int timeoutMs) {
		getState().changeTimeout = timeoutMs;

	}

	public static class TextChangeEventImpl extends TextChangeEvent {
		private final String text;
		private final TextRange selection;

		private TextChangeEventImpl(final AceEditor ace, String text,
				AceRange selection) {
			super(ace);
			this.text = text;
			this.selection = new TextRange(text, selection);
		}

		@Override
		public AbstractTextField getComponent() {
			return (AbstractTextField) super.getComponent();
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public int getCursorPosition() {
			return selection.getEnd();
		}
	}

	public TextRange getSelection() {
		return selection;
	}

	public int getCursorPosition() {
		return selection.getEnd();
	}

	/**
	 *  Sets the cursor position to be pos characters from
	 *  the beginning of the text.
	 *  
	 * @param pos 
	 */
	public void setCursorPosition(int pos) {
		setSelection(pos, pos);
	}
	
	/**
	 * Sets the cursor on the given row and column.
	 * 
	 * @param row starting from 0
	 * @param col starting from 0
	 */
	public void setCursorRowCol(int row, int col) {
		setSelectionRowCol(row, col, row, col);
	}
	
	/**
	 * Sets the selection to be between characters [start,end).
	 * 
	 * The cursor will be at the end.
	 * 
	 * @param start
	 * @param end
	 */
	// TODO
	public void setSelection(int start, int end) {
		setSelectionToClient(new Integer[]{start,end});
		setInternalSelection(new TextRange(getInternalValue(), start, end));
	}
	
	
	/**
	 * Sets the selection to be between the given (startRow,startCol) and
	 * (endRow, endCol).
	 * 
	 * The cursor will be at the end.
	 * 
	 * @param startRow starting from 0
	 * @param startCol starting from 0
	 * @param endRow starting from 0
	 * @param endCol starting from 0
	 */
	public void setSelectionRowCol(int startRow, int startCol, int endRow, int endCol) {
		setSelectionToClient(new Integer[]{startRow,startCol,endRow,endCol});
		setInternalSelection(new TextRange(doc.getText(), startRow, startCol, endRow, endCol));
	}
	
	private void setInternalSelection(TextRange selection) {
		this.selection = selection;
		getState().selection = selection.asTransport();
	}
	
	private void setSelectionToClient(Integer[] stc) {
		selectionToClient = stc;
		markAsDirty();
	}

	private void fireTextChangeEvent() {
		if (!isFiringTextChangeEvent) {
			isFiringTextChangeEvent = true;
			try {
				fireEvent(new TextChangeEventImpl(this, getInternalValue(), selection));
			} finally {
				isFiringTextChangeEvent = false;
			}
		}
	}

	public void setWordWrap(boolean ww) {
		getState().wordwrap = ww;
	}

	public void setThemePath(String path) {
		setAceConfig("themePath", path);
	}

	public void setModePath(String path) {
		setAceConfig("modePath", path);
	}

	public void setWorkerPath(String path) {
		setAceConfig("workerPath", path);
	}

	private void setAceConfig(String key, String value) {
		getState().config.put(key, value);
	}

	public void setMode(AceMode mode) {
		getState().mode = mode.toString();
	}

	public void setMode(String mode) {
		getState().mode = mode;
	}

	public void setTheme(AceTheme theme) {
		getState().theme = theme.toString();
	}

	public void setTheme(String theme) {
		getState().theme = theme;
	}

	public void setUseWorker(boolean useWorker) {
		getState().useWorker = useWorker;
	}
	
	@Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);
                
        if (initial) {
        	getState().initialValue = doc.asTransport();
        	if (!doc.equals(shadow)) {
        		shadow = doc;
        	}
        }
        else if (onRoundTrip) {
        	AceDocDiff diff = AceDocDiff.diff(shadow, doc);
        	shadow = doc;
        	TransportDiff td = diff.asTransport();
    		getRpcProxy(AceEditorClientRpc.class).diff(td);
    		
    		onRoundTrip = false;
    	}
        else if (true /* TODO !shadow.equals(doc)*/) {
        	getRpcProxy(AceEditorClientRpc.class).changedOnServer();
        }

        if (selectionToClient != null) {
        	getState().selectionFromServer++;
        	// {startPos,endPos}
        	if (selectionToClient.length==2) {
        		AceRange r = AceRange.fromPositions(selectionToClient[0], selectionToClient[1], doc.getText());
        		getState().selection = r.asTransport();
        	}
        	// {startRow,startCol,endRow,endCol}
        	else if (selectionToClient.length==4) {
        		TransportRange tr = new TransportRange(
        				selectionToClient[0],
        				selectionToClient[1],
        				selectionToClient[2],
        				selectionToClient[3]);
        		getState().selection = tr;
        	}
        	selectionToClient = null;
        }
        else {
        	getState().selectionFromServer = 0;
        }
    }

	public AceDoc getDoc() {
		return doc;
	}

}
