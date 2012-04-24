package org.vaadin.aceeditor.gwt.client;

import java.util.LinkedList;

import org.vaadin.aceeditor.gwt.client.EditorFacade.Key;
import org.vaadin.aceeditor.gwt.client.SuggestPopup.SuggestionSelectedListener;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.aceeditor.gwt.shared.Suggestion;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;
import com.vaadin.terminal.gwt.client.UIDL;

public class SuggestionHandler implements SuggestionSelectedListener {

	static final int Y_OFFSET = 16;
	
	private MarkerEditorFacade markerEditor;

	public interface SuggestionRequestedListener {
		public void suggestionRequested(int cursor);
	}

	public enum Phase {
		NOT_SUGGESTING, REQUEST_SUGGESTIONS_ON_NEXT_CHANGE, REQUESTING_SUGGESTIONS, CANCELLED_SUGGESTING_WHILE_REQUESTING, SUGGESTING
	}

	Phase phase = Phase.NOT_SUGGESTING;

	private Key suggestionKey;

	private SuggestPopup sp;

	private LinkedList<SuggestionRequestedListener> listeners = new LinkedList<SuggestionRequestedListener>();

	private static final Key dotKey = new Key(190);
	private static final RegExp noSuggestionAfterChar = RegExp.compile("[., ]");

	public static final String SUGGESTION_MARKER_ID = "SUGGMARKER";

	public SuggestionHandler(MarkerEditorFacade markerEditor) {
		this.markerEditor = markerEditor;
	}

	public void addListener(SuggestionRequestedListener srl) {
		listeners.add(srl);
	}

	public void updateFromUIDL(UIDL uidl) {
		if (uidl.hasAttribute("suggestion-key")) {
			int keyCode = uidl.getIntAttribute("suggestion-key");
			int[] mods = uidl.getIntArrayAttribute("suggestion-keymods");
			suggestionKey = new Key(keyCode, mods[0] == 1, mods[1] == 1,
					mods[2] == 1);
		}

		if (phase == Phase.REQUESTING_SUGGESTIONS) {
			suggestionsFromUIDL(uidl);
		} else if (phase == Phase.CANCELLED_SUGGESTING_WHILE_REQUESTING) {
			stopSuggesting();
		}
	}

	private void suggestionsFromUIDL(UIDL uidl) {
		if (uidl.hasAttribute("suggestion-values")) {
			String[] vals = uidl.getStringArrayAttribute("suggestion-values");
			String[] titles = uidl
					.getStringArrayAttribute("suggestion-displays");
			String[] descriptions = uidl
					.getStringArrayAttribute("suggestion-descriptions");
			int[] selStarts = uidl
					.getIntArrayAttribute("suggestion-sel-starts");
			int[] selEnds = uidl.getIntArrayAttribute("suggestion-sel-ends");
			int n = vals.length;
			LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();
			for (int i = 0; i < n; ++i) {
				Suggestion s = new Suggestion(vals[i], titles[i],
						descriptions[i], selStarts[i], selEnds[i]);
				suggs.add(s);
			}
			setPhase(Phase.SUGGESTING);
			showSuggestPopup(suggs);
		}
	}

	private void showSuggestPopup(LinkedList<Suggestion> suggs) {

		Marker suma = getSuggestionMarker();

		sp = new SuggestPopup(suggs, suma.substringOf(markerEditor.getText()));

		int[] coords = markerEditor.getCoordsOfPosition(suma.getEnd());
		int sy = Window.getScrollTop();
		int sx = Window.getScrollLeft();
		sp.setPopupPosition(coords[0] - sx, coords[1] - sy + Y_OFFSET);

		sp.setSuggestionSelectedListener(this);

		sp.open();
	}

	private Marker getSuggestionMarker() {
		return markerEditor.getMarker(SUGGESTION_MARKER_ID);
	}

	private void addSuggestionMarker(int cursor) {
		Marker m = Marker.newSuggestionMarker(cursor, cursor);
		markerEditor.putMarker(SUGGESTION_MARKER_ID, m);
	}

	private void removeSuggestionMarker() {
		markerEditor.removeMarker(SUGGESTION_MARKER_ID);
	}

	private void setPhase(Phase phase) {
		this.phase = phase;
	}

	private void stopSuggesting() {
		removeSuggestionMarker();
		setPhase(Phase.NOT_SUGGESTING);
		markerEditor.setFocus(true);
	}

	public Phase getPhase() {
		return phase;
	}

	public void textChanged() {
		if (getPhase() == SuggestionHandler.Phase.REQUEST_SUGGESTIONS_ON_NEXT_CHANGE) {
			// The cursorChanged event comes after textChanged,
			// and the cursor still lacks behind.
			// Have to adjust cursor 1 step forward.
			int cursor = markerEditor.getCursor() + 1;
			requestSuggestion(cursor);
		} else if (getPhase() == SuggestionHandler.Phase.SUGGESTING) {
			String suggMarkerContent = getSuggestionMarker().substringOf(
					markerEditor.getText());
			sp.setStartOfValue(suggMarkerContent);
		}
	}

	public void cursorChanged() {
		if (phase == Phase.SUGGESTING) {
			if (!getSuggestionMarker().touches(markerEditor.getCursor())) {
				sp.close();
			}
		}
		if (phase == Phase.REQUESTING_SUGGESTIONS) {
			if (!getSuggestionMarker().touches(markerEditor.getCursor())) {
				setPhase(Phase.CANCELLED_SUGGESTING_WHILE_REQUESTING);
			}
		}
	}

	public boolean keyPressed(Key key) {
		if (phase == Phase.NOT_SUGGESTING) {
			if (key.equals(suggestionKey)) {
				requestSuggestion(markerEditor.getCursor());
				return false;
			} else if (key.equals(dotKey)) {
				dotPress();
			}
		} else if (phase == Phase.SUGGESTING) {
			return keyPressWhileSuggesting(key);
		}

		return true;
	}

	private boolean keyPressWhileSuggesting(Key key) {
		if (key.keyCode == 38 /* UP */) {
			sp.up();
		} else if (key.keyCode == 40 /* DOWN */) {
			sp.down();
		} else if (key.keyCode == 13 /* ENTER */) {
			sp.select();
		} else if (key.keyCode == 27 /* ESC */) {
			sp.close();
		} else {
			return true;
		}
		return false;
	}

	private void dotPress() {
		int cursorBeforeDot = markerEditor.getCursor();
		if (cursorBeforeDot > 0) {
			char charBeforeDot = markerEditor.getText().charAt(
					cursorBeforeDot - 1);
			if (noSuggestionAfterChar.exec(String.valueOf(charBeforeDot)) == null) {
				setPhase(Phase.REQUEST_SUGGESTIONS_ON_NEXT_CHANGE);
			}
		}
	}

//	@Override
	public void suggestionSelected(Suggestion s) {
		Marker mm = getSuggestionMarker();
		markerEditor.replaceText(mm.getStart(), mm.getEnd(), s.getValueText(),
				true);

		// FIXME: the position seems to get messed up on multiline replacements,
		// probably because Ace gives different line endings on different
		// clients?
		// Only setting the selection for single line suggestions for now.
		if (s.getValueText().indexOf("\n") == -1) {
			int ss = mm.getStart() + s.getSelectionStart();
			int se = mm.getStart() + s.getSelectionEnd();
			markerEditor.setSelection(ss, se, false, true);
		}

		stopSuggesting();
	}

//	@Override
	public void noSuggestionSelected() {
		stopSuggesting();
	}

	private void requestSuggestion(int cursor) {
		addSuggestionMarker(cursor);
		for (SuggestionRequestedListener srl : listeners) {
			srl.suggestionRequested(cursor);
		}
		setPhase(Phase.REQUESTING_SUGGESTIONS);
	}

	public int getSuggestionCursor() {
		Marker m = getSuggestionMarker();
		if (m != null) {
			return m.getEnd();
		}
		return -1;
	}

	public void updatePopupPosition() {
		if (sp != null && sp.isOpen() && getSuggestionMarker() != null) {
			int[] coords = markerEditor
					.getCoordsOfPosition(getSuggestionMarker().getEnd());
			int sy = Window.getScrollTop();
			int sx = Window.getScrollLeft();
			sp.setPopupPosition(coords[0] - sx, coords[1] - sy + Y_OFFSET);
		}

	}

}
