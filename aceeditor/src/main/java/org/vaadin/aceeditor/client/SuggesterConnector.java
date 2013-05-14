package org.vaadin.aceeditor.client;

import java.util.List;

import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.aceeditor.client.AceEditorWidget.SelectionChangeListener;
import org.vaadin.aceeditor.client.SuggestPopup.SuggestionSelectedListener;
import org.vaadin.aceeditor.client.gwt.GwtAceKeyboardEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceKeyboardHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(SuggestionExtension.class)
public class SuggesterConnector extends AbstractExtensionConnector implements
		GwtAceKeyboardHandler, SuggestionSelectedListener, SelectionChangeListener {

	protected static final int Y_OFFSET = 20;

//	private final Logger logger = Logger.getLogger(SuggesterConnector.class.getName());


	private AceEditorConnector connector;
	private AceEditorWidget widget;

	private SuggesterServerRpc serverRpc = RpcProxy.create(
			SuggesterServerRpc.class, this);

	private String suggStartText;
	private AceRange suggStartCursor;
	
	private SuggesterClientRpc clientRpc = new SuggesterClientRpc() {
		@Override
		public void showSuggestions(List<TransportSuggestion> suggs) {
			setSuggs(suggs);
		}

		@Override
		public void applySuggestionDiff(TransportDiff td) {
			stopSuggesting();
			ClientSideDocDiff diff = ClientSideDocDiff.fromTransportDiff(td);
			widget.setTextAndAdjust(diff.applyTo(widget.getDoc()).getText());
			widget.fireTextChanged(); // XXX we need to do this here to alert AceEditorConnector...
		}
	};

	private boolean suggesting = false;

	private SuggestPopup popup;

	private Integer suggestionStartId;

	private boolean startSuggestingOnNextSelectionChange;

	private boolean suggestOnDot = true;


	public SuggesterConnector() {
		super();
		registerRpc(SuggesterClientRpc.class, clientRpc);
	}
	
	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		suggestOnDot  = getState().suggestOnDot;
	}
	
	@Override
	public SuggesterState getState() {
		return (SuggesterState) super.getState();
	}

	private void setSuggs(List<TransportSuggestion> suggs) {
		if (suggesting) {
			popup.setSuggestions(suggs);
		}
	}

	private SuggestPopup createSuggestionPopup() {
		SuggestPopup sp = new SuggestPopup();
		sp.setOwner(widget);
		updatePopupPosition(sp);
		sp.setSuggestionSelectedListener(this);
		sp.show();
		return sp;
	}

	@Override
	protected void extend(ServerConnector target) {
		connector = (AceEditorConnector) target;
		widget = connector.getWidget();
		widget.setKeyboardHandler(this);
	}

	@Override
	public Command handleKeyboard(JavaScriptObject data, int hashId,
			String keyString, int keyCode, GwtAceKeyboardEvent e) {
		if (suggesting) {
			return keyPressWhileSuggesting(keyCode);
		}
		if (e == null) {
			return Command.DEFAULT;
		}
//		logger.info("handleKeyboard(" + data + ", " + hashId + ", " + keyString
//				+ ", " + keyCode + ", " + e.getKeyCode() + "---"
//				+ e.isCtrlKey() + ")");

		if (keyCode == 32 && e.isCtrlKey()) {
			startSuggesting();
			return Command.NULL;
		} else if (suggestOnDot && ".".equals(keyString)) {
			startSuggestingOnNextSelectionChange = true;
			widget.addSelectionChangeListener(this);
			return Command.DEFAULT;
		}

		return Command.DEFAULT;
	}

	private void startSuggesting() {

		suggStartText = widget.getText();
		suggStartCursor = widget.getSelection();
		serverRpc.suggest(suggStartText, suggStartCursor.asTransport());

		suggestionStartId = widget.addInvisibleMarker(suggStartCursor);
		widget.addSelectionChangeListener(this);
		popup = createSuggestionPopup();
		suggesting = true;
	}

	@Override
	public void suggestionSelected(TransportSuggestion s) {
		connector.setOnRoundtrip(true);
//		AceRange suggMarker = widget.getInvisibleMarker(suggestionStartId);
		serverRpc.suggestionSelected(s.index);
		stopAskingForSuggestions();
	}

	@Override
	public void noSuggestionSelected() {
		stopAskingForSuggestions();
	}

	private void stopAskingForSuggestions() {
		widget.removeSelectionChangeListener(this);
		suggesting = false;
		widget.setFocus(true);
	}
	
	private void stopSuggesting() {
		if (popup!=null) {
			popup.hide();
			popup = null;
		}
		if (suggestionStartId != null) {
			widget.removeContentsOfInvisibleMarker(suggestionStartId);
			widget.removeInvisibleMarker(suggestionStartId);
		}
	}

	private Command keyPressWhileSuggesting(int keyCode) {
		if (keyCode == 38 /* UP */) {
			popup.up();
		} else if (keyCode == 40 /* DOWN */) {
			popup.down();
		} else if (keyCode == 13 /* ENTER */) {
			popup.select();
		} else if (keyCode == 27 /* ESC */) {
			popup.close();
		} else {
			return Command.DEFAULT;
		}
		return Command.NULL;
	}

	private String getWord(String text, int row, int col1, int col2) {
		if (col1 == col2) {
			return "";
		}
		String[] lines = text.split("\n", -1);
		int start = Util.cursorPosFromLineCol(lines, row, col1, 0);
		int end = Util.cursorPosFromLineCol(lines, row, col2, 0);
		return text.substring(start, end);
	}

	@Override
	public void selectionChanged() {
		if (startSuggestingOnNextSelectionChange) {
			widget.removeSelectionChangeListener(this);
			startSuggesting();
			startSuggestingOnNextSelectionChange = false;
			return;
		}
		
		AceRange sel = widget.getSelection();
		
		AceRange sug = widget.getInvisibleMarker(suggestionStartId);
		if (sug.getStartRow()!=sug.getEndRow()) {
			popup.close();
		}
		else if (sel.getEndRow() != sug.getStartRow() || sel.getEndRow() != sug.getEndRow()) {
			popup.close();
		} else if (sel.getEndCol()<sug.getStartCol() || sel.getEndCol()>sug.getEndCol()) {
			popup.close();
		} else {
			updatePopupPosition(popup);
			String s = getWord(widget.getText(), sug.getEndRow(),
					sug.getStartCol(), sug.getEndCol());
			popup.setStartOfValue(s);
		}
	}

	private void updatePopupPosition(SuggestPopup popup) {
		int[] coords = widget.getCursorCoords();
		int wx = Window.getClientWidth();
		int wy = Window.getClientHeight();
		int sx = Window.getScrollLeft();
		int sy = Window.getScrollTop();
		int x = coords[0] - sx;
		int y = coords[1] - sy + Y_OFFSET;
		int maxx = wx - SuggestPopup.WIDTH - SuggestPopup.DESCRIPTION_WIDTH;
		if (x > maxx) {
			x -= SuggestPopup.WIDTH + SuggestPopup.DESCRIPTION_WIDTH + 50;
		}
		int maxy = wy - SuggestPopup.HEIGHT;
		if (y > maxy) {
			y -= SuggestPopup.HEIGHT + 50;
		}
		popup.setPopupPosition(x, y);
	}

}
