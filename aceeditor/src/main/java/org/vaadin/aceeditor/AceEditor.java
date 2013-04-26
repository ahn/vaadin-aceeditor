package org.vaadin.aceeditor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.vaadin.aceeditor.client.AceClientAnnotation;
import org.vaadin.aceeditor.client.AceClientMarker;
import org.vaadin.aceeditor.client.AceClientRange;
import org.vaadin.aceeditor.client.AceDocument;
import org.vaadin.aceeditor.client.AceEditorServerRpc;
import org.vaadin.aceeditor.client.AceEditorState;

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
@JavaScript("client/js/ace/ace.js")
@StyleSheet("client/css/ace-gwt.css")
public class AceEditor extends AbstractField<String> implements BlurNotifier,
		FocusNotifier, TextChangeNotifier {

	private static final String DEFAULT_ACE_PATH = "http://d1n0x3qji82z53.cloudfront.net/src-min-noconflict";

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
						SelectionChangeEvent.class);;

		public void selectionChanged(SelectionChangeEvent e);
	}

	private long latestMarkerId = 0L;
	
	private AceRange selection = new AceRange(0, 0, 0, 0, "");
	
	// {startPos,endPos} or {startRow,startCol,endRow,endCol}
	private Integer[] selectionToClient = null;

	private boolean isFiringTextChangeEvent;
	private boolean latestFocus = false;

	private AceEditorServerRpc rpc = new AceEditorServerRpc() {

		@Override
		public void changedDelayed(AceDocument doc, AceClientRange sel, boolean focus) {
			changed(doc, sel, focus);
		}

		@Override
		public void sendNow() {
			// nothing
		}

		private void changed(AceDocument doc, AceClientRange sel, boolean focus) {
			
			getState().document = doc;
			
			if (!doc.getText().equals(getInternalValue())) {

				// TODO: when to call setInternalValue??
				setInternalValue(doc.getText());

				fireTextChangeEvent();
			}

			if (latestFocus != focus) {
				latestFocus = focus;
				if (focus) {
					fireFocus();
				} else {
					fireBlur();
				}
			}
			if (!sel.equals(selection)) {
				selection = new AceRange(sel, doc.getText());
				getState().selection = sel;
				fireSelectionChanged();
			}
		}
	};
	
	public AceEditor() {
		super();
		setWidth("300px");
		setHeight("200px");

		setModePath(DEFAULT_ACE_PATH);
		setThemePath(DEFAULT_ACE_PATH);
		setWorkerPath(DEFAULT_ACE_PATH);

		registerRpc(rpc);
	}

	protected void fireSelectionChanged() {
		fireEvent(new SelectionChangeEvent(this));
	}

	protected void fireBlur() {
		fireEvent(new BlurEvent(this));
	}

	protected void fireFocus() {
		fireEvent(new FocusEvent(this));
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
		getState().document.setText(newValue);
	}
	
	@Override
	public void setValue(String newFieldValue) {
		super.setValue(newFieldValue);
		getState().document.incrementLatestChangeByServer();
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

	public long addMarker(AceMarker marker) {
		marker.serverId = newMarkerId();
		getState().document.getMarkers().add(marker);
		getState().document.incrementLatestChangeByServer();
		return marker.serverId;
	}

	public void removeMarker(AceMarker marker) {
		removeMarker(marker.serverId);
	}

	public void removeMarker(long serverId) {
		for (AceClientMarker m : getState().document.getMarkers()) {
			if (m.serverId==serverId) {
				getState().document.getMarkers().remove(m);
				getState().document.incrementLatestChangeByServer();
				break;
			}
		}
	}
	
	public void clearMarkers() {
		getState().document.getMarkers().clear();
		getState().document.incrementLatestChangeByServer();
	}

	public void addRowAnnotation(AceAnnotation ann, int row) {
		Set<AceClientAnnotation> manns = getState().document.getMarkerAnnotations();
		if (manns == null) {
			// ok
		} else if (manns.isEmpty()) {
			getState().document.setMarkerAnnotations(null);
		} else {
			throw new IllegalStateException(
					"AceEditor can contain either row annotations or marker annotations, not both.");
		}

		Set<AceClientAnnotation> ranns = getState().document.getRowAnnotations();
		if (ranns == null || ranns.isEmpty()) {
			getState().document.setRowAnnotations(new HashSet<AceClientAnnotation>());
		}
		AceClientAnnotation rann = new AceClientAnnotation(ann.getMessage(),
				AceClientAnnotation.Type.valueOf(ann.getType().toString()), row);
		getState().document.getRowAnnotations().add(rann);
		getState().document.incrementLatestChangeByServer();
	}

	public void addMarkerAnnotation(AceAnnotation ann, AceClientMarker marker) {
		addMarkerAnnotation(ann, marker.serverId);
	}

	public void addMarkerAnnotation(AceAnnotation ann, long markerId) {
		if (!hasMarker(markerId)) {
			throw new IllegalStateException("Editor does not contain marker with id " + markerId);
		}
		
		Set<AceClientAnnotation> ranns = getState().document.getRowAnnotations();
		if (ranns == null) {
			// ok
		} else if (ranns.isEmpty()) {
			getState().document.setRowAnnotations(null);
		} else {
			throw new IllegalStateException(
					"AceEditor can contain either row annotations or marker annotations, not both.");
		}

		Set<AceClientAnnotation> manns = getState().document.getMarkerAnnotations();
		if (manns == null || manns.isEmpty()) {
			getState().document.setMarkerAnnotations(new HashSet<AceClientAnnotation>());
		}
		AceClientAnnotation rann = new AceClientAnnotation(ann.getMessage(),
				AceClientAnnotation.Type.valueOf(ann.getType().toString()), 0);
		rann.markerId = markerId;
		getState().document.getMarkerAnnotations().add(rann);
		getState().document.incrementLatestChangeByServer();
	}

	private boolean hasMarker(long markerId) {
		for (AceClientMarker m : getState().document.getMarkers()) {
			if (m.serverId==markerId) {
				return true;
			}
		}
		return false;
	}

	public void clearRowAnnotations() {
		if (getState().document.getMarkerAnnotations() == null) {
			getState().document.setRowAnnotations(new HashSet<AceClientAnnotation>());
		} else {
			getState().document.setRowAnnotations(null);
		}
		getState().document.incrementLatestChangeByServer();
	}

	public void clearMarkerAnnotations() {
		if (getState().document.getRowAnnotations() == null) {
			getState().document.setMarkerAnnotations(new HashSet<AceClientAnnotation>());
		} else {
			getState().document.setMarkerAnnotations(null);
		}
		getState().document.incrementLatestChangeByServer();
	}

	private long newMarkerId() {
		return ++latestMarkerId;
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
		private final AceRange selection;

		private TextChangeEventImpl(final AceEditor ace, String text,
				AceRange selection) {
			super(ace);
			this.text = text;
			this.selection = selection;
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
			return selection.getCursorPosition();
		}
	}

	public AceRange getSelection() {
		return selection;
	}

	public int getCursorPosition() {
		return selection.getCursorPosition();
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
	public void setSelection(int start, int end) {
		setSelectionToClient(new Integer[]{start,end});
		setInternalSelection(AceRange.fromPositions(start, end, getInternalValue()));
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
		setInternalSelection(new AceRange(startRow, startCol, endRow, endCol, getInternalValue()));
	}
	
	private void setInternalSelection(AceRange selection) {
		this.selection = selection;
		getState().selection = selection;
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
        
        if (selectionToClient != null) {
        	getState().selectionFromServer++;
        	// {startPos,endPos}
        	if (selectionToClient.length==2) {
        		getState().selection = AceRange.fromPositions(
        				selectionToClient[0],
        				selectionToClient[1],
        				getInternalValue());
        	}
        	// {startRow,startCol,endRow,endCol}
        	else if (selectionToClient.length==4) {
        		getState().selection = new AceClientRange(
        				selectionToClient[0],
        				selectionToClient[1],
        				selectionToClient[2],
        				selectionToClient[3]);
        	}
        	selectionToClient = null;
        }
        else {
        	getState().selectionFromServer = 0;
        }
    }

}
