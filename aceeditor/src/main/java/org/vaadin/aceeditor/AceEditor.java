package org.vaadin.aceeditor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.AceClientAnnotation;
import org.vaadin.aceeditor.client.AceEditorServerRpc;
import org.vaadin.aceeditor.client.AceEditorState;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceClientRange;

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

@SuppressWarnings("serial")
@JavaScript("client/js/ace/ace.js")
@StyleSheet("client/css/ace-gwt.css")
public class AceEditor extends AbstractField<String> implements BlurNotifier,
		FocusNotifier, TextChangeNotifier {

	private static final String DEFAULT_ACE_PATH = "http://d1n0x3qji82z53.cloudfront.net/src-min-noconflict";

	public interface SelectionChangeListener {
		public static final Method selectionChangedMethod = ReflectTools
				.findMethod(SelectionChangeListener.class, "selectionChanged",
						SelectionChangeEvent.class);;

		public void selectionChanged(SelectionChangeEvent e);
	}

	private long latestMarkerId = 0L;
	private String text = "";
	private AceRange selection = new AceRange(0, 0, 0, 0, "");

	private boolean isFiringTextChangeEvent;
	private boolean latestFocus = false;

	private Map<Long, AceMarker> markers = new HashMap<Long, AceMarker>();

	private AceEditorServerRpc rpc = new AceEditorServerRpc() {

		@Override
		public void changedDelayed(String t, AceClientRange sel, boolean focus) {
			changed(t, sel, focus);
		}

		@Override
		public void sendNow() {
			// nothing
		}

		private void changed(String t, AceClientRange sel, boolean focus) {

			if (!t.equals(text)) {
				text = t;

				// TODO: when to call setInternalValue??
				setInternalValue(text);

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
				selection = new AceRange(sel, text);
				getState().selection = sel;
				fireSelectionChanged();
			}
		}

		@Override
		public void markersChanged(List<AceMarker> markers) {

			for (AceMarker m : markers) {
				System.out.println("MMM " + m);
			}

			getState().markers = markers;

		}

		@Override
		public void annotationsChanged(
				Set<AceClientAnnotation> markerAnnotations) {
			getState().markerAnnotations = markerAnnotations;
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
		getState().text = newValue;
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
		markers.put(marker.serverId, marker);
		getState().markers = new LinkedList<AceMarker>(markers.values());
		return marker.serverId;
	}

	public AceMarker removeMarker(AceMarker marker) {
		if (marker.serverId < 0) {
			return null;
		}
		return removeMarker(marker.serverId);
	}

	public AceMarker removeMarker(long serverId) {
		AceMarker m = markers.remove(serverId);
		if (m != null) {
			getState().markers = new LinkedList<AceMarker>(markers.values());
		}
		return m;
	}

	public void clearMarkers() {
		markers = new HashMap<Long, AceMarker>();
		getState().markers = new LinkedList<AceMarker>(markers.values());
	}

	public void addRowAnnotation(AceAnnotation ann, int row) {
		Set<AceClientAnnotation> manns = getState().markerAnnotations;
		if (manns == null) {
			// ok
		} else if (manns.isEmpty()) {
			getState().markerAnnotations = null;
		} else {
			throw new IllegalStateException(
					"AceEditor can contain either row annotations or marker annotations, not both.");
		}

		AceClientAnnotation rann = new AceClientAnnotation(ann.getMessage(),
				AceClientAnnotation.Type.valueOf(ann.getType().toString()), row);
		if (getState().rowAnnotations == null
				|| getState().rowAnnotations.isEmpty()) {
			getState().rowAnnotations = new HashSet<AceClientAnnotation>();
		}
		getState().rowAnnotations.add(rann);
	}

	public void addMarkerAnnotation(AceAnnotation ann, AceMarker marker) {
		addMarkerAnnotation(ann, marker.serverId);
	}

	public void addMarkerAnnotation(AceAnnotation ann, long markerId) {
		Set<AceClientAnnotation> ranns = getState().rowAnnotations;
		if (ranns == null) {
			// ok
		} else if (ranns.isEmpty()) {
			getState().rowAnnotations = null;
		} else {
			throw new IllegalStateException(
					"AceEditor can contain either row annotations or marker annotations, not both.");
		}
		AceClientAnnotation cann = new AceClientAnnotation(ann.getMessage(),
				AceClientAnnotation.Type.valueOf(ann.getType().toString()), 0);
		cann.markerId = markerId;

		if (getState().markerAnnotations == null
				|| getState().markerAnnotations.isEmpty()) {
			getState().markerAnnotations = new HashSet<AceClientAnnotation>();
		}
		getState().markerAnnotations.add(cann);
	}

	public void clearRowAnnotations() {
		if (getState().markerAnnotations == null) {
			getState().rowAnnotations = Collections.emptySet();
		} else {
			getState().rowAnnotations = null;
		}
	}

	public void clearMarkerAnnotations() {
		if (getState().rowAnnotations == null) {
			getState().markerAnnotations = Collections.emptySet();
		} else {
			getState().markerAnnotations = null;
		}
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
	 * communicated to the application when {@link #getTextChangeEventMode()} is
	 * {@link TextChangeEventMode#LAZY} or {@link TextChangeEventMode#TIMEOUT}.
	 * 
	 * 
	 * @see #getTextChangeEventMode()
	 * 
	 * @param timeout
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

	public void setCursorPosition(int pos) {
		setSelection(pos, pos);
	}

	public void setSelection(int start, int end) {
		setSelection(AceRange.fromPositions(start, end, text));
		
	}
	
	public void setSelection(AceRange selection) {
		getState().selection = selection;
	}

	private void fireTextChangeEvent() {
		if (!isFiringTextChangeEvent) {
			isFiringTextChangeEvent = true;
			try {
				fireEvent(new TextChangeEventImpl(this, text, selection));
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

}
