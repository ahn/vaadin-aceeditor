package org.vaadin.aceeditor.client;

import java.util.List;

import org.vaadin.aceeditor.SuggestionExtension;
import org.vaadin.aceeditor.client.AceEditorWidget.SelectionChangeListener;
import org.vaadin.aceeditor.client.SuggestPopup.SuggestionSelectedListener;
import org.vaadin.aceeditor.client.gwt.GwtAceKeyboardEvent;
import org.vaadin.aceeditor.client.gwt.GwtAceKeyboardHandler;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/*
 * When a user requests suggestions an invisible marker is created at the cursor position
 * and a SuggestPopup is shown. When the user types while suggesting,
 * the invisible marker auto-adjusts to contain what's typed.
 * (This takes advantage of how AceEditorWidget.moveMarkerOnInsert happens
 * to be implemented. It's bit of a mess...)
 *
 * When a suggestion is selected what's inside of the invisible marker is deleted
 * before applying the suggestion.
 *
 *
 */
@SuppressWarnings("serial")
@Connect(SuggestionExtension.class)
public class SuggesterConnector extends AbstractExtensionConnector implements
GwtAceKeyboardHandler, SuggestionSelectedListener, SelectionChangeListener, ResizeHandler {

	protected static final int Y_OFFSET = 20;

	protected AceEditorConnector connector;
	protected AceEditorWidget widget;
	private String suggestText = ".";

	protected SuggesterServerRpc serverRpc = RpcProxy.create(
			SuggesterServerRpc.class, this);

	protected String suggStartText;
	protected AceRange suggStartCursor;

	private final SuggesterClientRpc clientRpc = new SuggesterClientRpc() {
		@Override
		public void showSuggestions(final List<TransportSuggestion> suggs) {
			SuggesterConnector.this.setSuggs(suggs);
		}

		@Override
		public void applySuggestionDiff(final TransportDiff td) {
			SuggesterConnector.this.stopSuggesting();
			final ClientSideDocDiff diff = ClientSideDocDiff.fromTransportDiff(td);
			final String text = diff.applyTo(SuggesterConnector.this.widget.getDoc()).getText();
			SuggesterConnector.this.widget.setTextAndAdjust(text);
			SuggesterConnector.this.widget.fireTextChanged(); // XXX we need to do this here to alert AceEditorConnector...
			if (td.selectionStart != null && td.selectionEnd != null) {
				SuggesterConnector.this.widget.setSelection(AceRange.fromPositions(td.selectionStart, td.selectionEnd, text));
			} else {
				final AceRange sel = SuggesterConnector.this.widget.getSelection();
				AceRange newSel;
				if (sel.getStartRow() < sel.getEndRow()) {
					newSel = new AceRange(sel.getEndRow(), sel.getEndCol(), sel.getEndRow(), sel.getEndCol());
				} else if (sel.getStartRow() == sel.getEndRow()) {
					if (sel.getStartCol() < sel.getEndCol()) {
						newSel = new AceRange(sel.getEndRow(), sel.getEndCol(), sel.getEndRow(), sel.getEndCol());
					} else {
						newSel = new AceRange(sel.getStartRow(), sel.getStartCol(), sel.getStartRow(), sel.getStartCol());
					}
				} else {
					newSel = new AceRange(sel.getStartRow(), sel.getStartCol(), sel.getStartRow(), sel.getStartCol());
				}
				SuggesterConnector.this.widget.setSelection(newSel);
			}
		}
	};

	protected boolean suggesting = false;

	protected SuggestPopup popup;

	protected Integer suggestionStartId;

	protected boolean startSuggestingOnNextSelectionChange;

	protected boolean suggestOnDot = true;

	protected boolean showDescriptions = true;

	protected int popupWidth = 150;
	protected Unit popupWidthUnit = Unit.PX;

	protected int popupHeight = 200;
	protected Unit popupHeightUnit = Unit.PX;

	protected int popupDescriptionWidth = 225;
	protected Unit popupDescriptionWidthUnit = Unit.PX;

	public SuggesterConnector() {
		this.registerRpc(SuggesterClientRpc.class, this.clientRpc);
		Window.addResizeHandler(this);
	}

	@Override
	public void onStateChanged(final StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		this.suggestOnDot = this.getState().suggestOnDot;
		this.showDescriptions = this.getState().showDescriptions;
		this.suggestText = this.getState().suggestText;

		this.popupWidth = this.getState().popupWidth;
		this.popupWidthUnit = this.fromType(this.getState().popupWidthUnit);
		this.popupHeight = this.getState().popupHeight;
		this.popupHeightUnit = this.fromType(this.getState().popupHeightUnit);
		this.popupDescriptionWidth = this.getState().popupDescriptionWidth;
		this.popupDescriptionWidthUnit = this.fromType(this.getState().popupDescriptionWidthUnit);
	}

	private Unit fromType(final String s) {
		for (final Unit u: Unit.values()) {
			if (u.getType().equals(s)) {
				return u;
			}
		}
		return Unit.PX;
	}

	@Override
	public SuggesterState getState() {
		return (SuggesterState) super.getState();
	}

	protected void setSuggs(final List<TransportSuggestion> suggs) {
		if (this.suggesting) {
			this.popup.setSuggestions(suggs);
		}
	}

	protected SuggestPopup createSuggestionPopup() {
		final SuggestPopup sp = new SuggestPopup();
		sp.setOwner(this.widget);
		this.updatePopupPosition(sp);
		sp.setSuggestionSelectedListener(this);
		sp.setWidth(this.popupWidth, this.popupWidthUnit);
		sp.setHeight(this.popupHeight, this.popupHeightUnit);
		sp.setDescriptionWidth(this.popupDescriptionWidth, this.popupDescriptionWidthUnit);
		sp.show();
		return sp;
	}

	@Override
	protected void extend(final ServerConnector target) {
		this.connector = (AceEditorConnector) target;
		this.widget = this.connector.getWidget();
		this.widget.setKeyboardHandler(this);
	}

	@Override
	public Command handleKeyboard(final JavaScriptObject data, final int hashId,
			final String keyString, final int keyCode, final GwtAceKeyboardEvent e) {
		if (this.suggesting) {
			return this.keyPressWhileSuggesting(keyCode);
		}
		if (e == null) {
			return Command.DEFAULT;
		}

		if (keyCode == 32 && e.isCtrlKey()) {
			this.startSuggesting();
			return Command.NULL;
		} else if (this.suggestOnDot && this.suggestText.equals(keyString)) {
			this.startSuggestingOnNextSelectionChange = true;
			this.widget.addSelectionChangeListener(this);
			return Command.DEFAULT;
		}

		return Command.DEFAULT;
	}

	protected void startSuggesting() {
		// ensure valid value of component on server before suggesting
		this.connector.sendToServerImmediately();

		final AceRange sel = this.widget.getSelection();

		this.suggStartText = this.widget.getText();
		this.suggStartCursor = new AceRange(sel.getEndRow(), sel.getEndCol(), sel.getEndRow(), sel.getEndCol());
		this.serverRpc.suggest(this.suggStartText, this.suggStartCursor.asTransport());

		this.suggestionStartId = this.widget.addInvisibleMarker(this.suggStartCursor);
		this.widget.addSelectionChangeListener(this);
		this.popup = this.createSuggestionPopup();
		this.popup.showDescriptions = this.showDescriptions;
		this.suggesting = true;
	}

	@Override
	public void suggestionSelected(final TransportSuggestion s) {
		// ???
		//connector.setOnRoundtrip(true);
		//		AceRange suggMarker = widget.getInvisibleMarker(suggestionStartId);
		this.serverRpc.suggestionSelected(s.index);
		this.stopAskingForSuggestions();
	}

	@Override
	public void noSuggestionSelected() {
		this.stopAskingForSuggestions();
	}

	protected void stopAskingForSuggestions() {
		this.widget.removeSelectionChangeListener(this);
		this.suggesting = false;
		this.widget.setFocus(true);
	}

	protected void stopSuggesting() {
		if (this.popup!=null) {
			this.popup.hide();
			this.popup = null;
		}
		if (this.suggestionStartId != null) {
			this.widget.removeContentsOfInvisibleMarker(this.suggestionStartId);
			this.widget.removeInvisibleMarker(this.suggestionStartId);
		}
	}

	protected Command keyPressWhileSuggesting(final int keyCode) {
		if (keyCode == KeyCodes.KEY_UP) {
			this.popup.up();
		} else if (keyCode == KeyCodes.KEY_DOWN) {
			this.popup.down();
		} else if (keyCode == KeyCodes.KEY_RIGHT) {
			this.popup.openOrCloseGroup(true);
		} else if (keyCode == KeyCodes.KEY_LEFT) {
			this.popup.openOrCloseGroup(false);
		} else if (keyCode == KeyCodes.KEY_ENTER) {
			this.popup.select();
		} else if (keyCode == KeyCodes.KEY_ESCAPE) {
			this.popup.close();
		} else {
			return Command.DEFAULT;
		}
		return Command.NULL;
	}

	protected String getWord(final String text, final int row, final int col1, final int col2) {
		if (col1 == col2) {
			return "";
		}
		final String[] lines = text.split("\n", -1);
		final int start = Util.cursorPosFromLineCol(lines, row, col1, 0);
		final int end = Util.cursorPosFromLineCol(lines, row, col2, 0);
		return text.substring(start, end);
	}

	@Override
	public void selectionChanged() {
		if (this.startSuggestingOnNextSelectionChange) {
			this.widget.removeSelectionChangeListener(this);
			this.startSuggesting();
			this.startSuggestingOnNextSelectionChange = false;
			return;
		}

		final AceRange sel = this.widget.getSelection();

		final AceRange sug = this.widget.getInvisibleMarker(this.suggestionStartId);
		if (sug.getStartRow()!=sug.getEndRow()) {
			this.popup.close();
		}
		else if (sel.getEndRow() != sug.getStartRow() || sel.getEndRow() != sug.getEndRow()) {
			this.popup.close();
		} else if (sel.getEndCol()<sug.getStartCol() || sel.getEndCol()>sug.getEndCol()) {
			this.popup.close();
		} else {
			this.updatePopupPosition(this.popup);
			final String s = this.getWord(this.widget.getText(), sug.getEndRow(),
					sug.getStartCol(), sug.getEndCol());
			this.popup.setStartOfValue(s);
		}
	}

	protected void updatePopupPosition(final SuggestPopup popupToUpdate) {
		final int[] coords = this.widget.getCursorCoords();
		final int sx = Window.getScrollLeft();
		final int sy = Window.getScrollTop();
		final int x = coords[0] - sx;
		final int y = coords[1] - sy + SuggesterConnector.Y_OFFSET;
		/*
		int wx = Window.getClientWidth();
		int wy = Window.getClientHeight();
		int maxx = wx - SuggestPopup.WIDTH - (showDescriptions ? SuggestPopup.DESCRIPTION_WIDTH : 0);
		if (x > maxx) {
			x -= SuggestPopup.WIDTH + (showDescriptions ? SuggestPopup.DESCRIPTION_WIDTH : 0) + 50;
		}
		int maxy = wy - SuggestPopup.HEIGHT;
		if (y > maxy) {
			y -= SuggestPopup.HEIGHT + 50;
		}
		 */
		popupToUpdate.setPopupPosition(x, y);
	}

	public String getSuggestText() {
		return this.suggestText;
	}

	public void setSuggestText(final String suggestText) {
		this.suggestText = suggestText;
	}

	@Override
	public void onResize(final ResizeEvent event) {
		if (this.popup != null) {
			this.popup.windowResized(event);
		}
	}

}