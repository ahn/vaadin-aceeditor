package org.vaadin.aceeditor.client;

import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.client.AceEditorWidget.FocusChangeListener;
import org.vaadin.aceeditor.client.AceEditorWidget.SelectionChangeListener;
import org.vaadin.aceeditor.client.AceEditorWidget.TextChangeListener;
import org.vaadin.aceeditor.client.gwt.GwtAceEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ComputedStyle;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(AceEditor.class)
public class AceEditorConnector extends AbstractHasComponentsConnector
implements TextChangeListener, SelectionChangeListener, FocusChangeListener {

	protected AceEditorServerRpc serverRpc =
			RpcProxy.create(AceEditorServerRpc.class, this);


	protected enum TextChangeEventMode {
		EAGER, TIMEOUT, LAZY
	}

	protected TextChangeEventMode changeMode = null;
	protected int changeTimeout = 400;

	protected class SendTimer extends Timer {
		private boolean scheduled;
		private SendCond send = SendCond.NO;

		public void schedule(final int ms, final SendCond sendCond) {
			super.schedule(ms);
			this.send = this.send.or(sendCond);
			this.scheduled = true;
		}

		public void scheduleIfNotAlready(final int ms, final SendCond sendCond) {
			if (!this.scheduled) {
				this.schedule(ms, sendCond);
			}
		}

		@Override
		public void run() {
			this.scheduled = false;
			AceEditorConnector.this.sendToServerImmediately(this.send);
			this.send = SendCond.NO;
		}

		@Override
		public void cancel() {
			super.cancel();
			this.send = SendCond.NO;
		}
	}

	protected SendTimer sendTimer = null;

	protected AceDoc shadow;

	protected boolean onRoundtrip = false;

	protected enum SendCond {
		NO, IF_CHANGED, ALWAYS;
		public SendCond or(final SendCond sw2) {
			return this.ordinal() > sw2.ordinal() ? this : sw2;
		}
	}

	protected SendCond sendAfterRoundtrip = SendCond.NO;

	protected AceEditorClientRpc clientRpc = new AceEditorClientRpc() {
		@Override
		public void diff(final TransportDiff ad) {
			final ClientSideDocDiff diff = ClientSideDocDiff.fromTransportDiff(ad);
			AceEditorConnector.this.shadow = diff.applyTo(AceEditorConnector.this.shadow);

			final AceDoc doc1 = AceEditorConnector.this.getWidget().getDoc();
			final AceDoc doc2 = diff.applyTo(doc1);

			AceEditorConnector.this.getWidget().setDoc(doc2);

			if (AceEditorConnector.this.selectionAfterApplyingDiff!=null) {
				AceEditorConnector.this.getWidget().setSelection(AceEditorConnector.this.selectionAfterApplyingDiff);
				AceEditorConnector.this.selectionAfterApplyingDiff = null;
			}

			if (AceEditorConnector.this.scrollToRowAfterApplyingDiff != -1) {
				AceEditorConnector.this.getWidget().scrollToRow(AceEditorConnector.this.scrollToRowAfterApplyingDiff);
				AceEditorConnector.this.scrollToRowAfterApplyingDiff = -1;
			}

			if (!doc1.getText().equals(doc2.getText())) {
				AceEditorConnector.this.sendAfterRoundtrip = AceEditorConnector.this.sendAfterRoundtrip.or(SendCond.ALWAYS);
			}
			AceEditorConnector.this.setOnRoundtrip(false);
		}

		@Override
		public void changedOnServer() {
			if (!AceEditorConnector.this.isOnRoundtrip()) {
				AceEditorConnector.this.sendToServer(SendCond.ALWAYS, true);
			}
			// else ? should we send after roundtrip or not?
		}

	};

	protected boolean listenToSelectionChanges;
	protected boolean listenToFocusChanges;

	// When setting selection or scrollToRow, we must make
	// sure that the text value is set before that.
	// That is, we must make the diff sync roundtrip and set
	// these things after that.
	// That's why this complication.
	// TODO: this may not be the cleanest way to do it...
	protected int scrollToRowAfterApplyingDiff = -1;
	protected AceRange selectionAfterApplyingDiff;

	public AceEditorConnector() {
		super();
		this.registerRpc(AceEditorClientRpc.class, this.clientRpc);
	}

	@Override
	public void init() {
		super.init();

		// Needed if inside a resizable subwindow.
		// Should we check that and only listen if yes?
		this.getLayoutManager().addElementResizeListener(this.getWidget().getElement(), new ElementResizeListener() {
			@Override
			public void onElementResize(final ElementResizeEvent e) {
				AceEditorConnector.this.getWidget().resize();
			}
		});
	}

	@Override
	public void onStateChanged(final StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		this.setTextChangeEventMode(this.getState().changeMode);
		this.setTextChangeTimeout(this.getState().changeTimeout);

		ClientSideDocDiff.dmp.setDiff_EditCost(this.getState().diff_editCost);

		// TODO: are these needed?
		//		widget.setHideErrors(getState().hideErrors);
		//		widget.setRequired(getState().required);
		//		widget.setModified(getState().modified);

		final boolean firstTime = !this.getWidget().isInitialized();
		if (firstTime) {
			// To make sure Ace config is applied before the editor is created,
			// we delay the initialization till then first call to onStateChanged,
			// not initializing in createWidget() right away.
			AceEditorConnector.applyConfig(this.getState().config);
			this.getWidget().initialize();
		}

		this.getWidget().setMode(this.getState().mode);
		this.getWidget().setTheme(this.getState().theme);
		this.listenToSelectionChanges = this.getState().listenToSelectionChanges;
		this.listenToFocusChanges = this.getState().listenToFocusChanges;
		this.getWidget().setUseWorker(this.getState().useWorker);
		this.getWidget().setWordwrap(this.getState().wordwrap);

		this.getWidget().setShowGutter(this.getState().showGutter);
		this.getWidget().setShowPrintMargin(this.getState().showPrintMargin);
		this.getWidget().setHighlightActiveLineEnabled(this.getState().highlightActiveLine);

		this.getWidget().setEnabled(this.getState().enabled);
		//        getWidget().setPropertyReadOnly(getState().propertyReadOnly);
		this.getWidget().setTabIndex(this.getState().tabIndex);
		this.getWidget().setReadOnly(this.getState().readOnly);

		if (stateChangeEvent.hasPropertyChanged("fontSize")) {
			String fontSize = this.getState().fontSize;

			if ("auto".equals(fontSize)) {
				// detect font size from CSS
				final Element fontSizeMeasureElement = Document.get().createDivElement();
				fontSizeMeasureElement.setClassName("ace_editor");
				fontSizeMeasureElement.getStyle().setPosition(Style.Position.FIXED);
				fontSizeMeasureElement.getStyle().setVisibility(Style.Visibility.HIDDEN);
				this.getWidget().getElement().appendChild(fontSizeMeasureElement);

				final ComputedStyle cs = new ComputedStyle(fontSizeMeasureElement);
				fontSize = cs.getProperty("fontSize");

				this.getWidget().getElement().removeChild(fontSizeMeasureElement);
			}

			this.getWidget().setFontSize(fontSize);
		}

		this.getWidget().setHighlightSelectedWord(this.getState().highlightSelectedWord);
		this.getWidget().setShowInvisibles(this.getState().showInvisibles);
		this.getWidget().setDisplayIndentGuides(this.getState().displayIndentGuides);

		this.getWidget().setUseSoftTabs(this.getState().softTabs);
		this.getWidget().setTabSize(this.getState().tabSize);

		// TODO: How should we deal with immediateness. Since there's already textChangeEventMode...
		//immediate = getState().immediate;

		if (firstTime) {
			this.shadow = AceDoc.fromTransport(this.getState().initialValue);
			this.getWidget().setDoc(this.shadow);
		}

		if (this.getState().selection != null) {
			final AceRange sel = AceRange.fromTransport(this.getState().selection);
			if (firstTime) {
				this.getWidget().setSelection(sel);
			}
			else {
				this.selectionAfterApplyingDiff = sel;
			}
		}

		if (this.getState().scrollToRow != -1) {
			if (firstTime) {
				this.getWidget().scrollToRow(this.getState().scrollToRow);
			}
			else {
				this.scrollToRowAfterApplyingDiff = this.getState().scrollToRow;
			}
		}
	}

	protected static void applyConfig(final Map<String, String> config) {
		for (final Entry<String, String> e : config.entrySet()) {
			GwtAceEditor.setAceConfig(e.getKey(), e.getValue());
		}
	}

	@Override
	protected Widget createWidget() {
		final AceEditorWidget widget = GWT.create(AceEditorWidget.class);
		widget.addTextChangeListener(this);
		widget.addSelectionChangeListener(this);
		widget.setFocusChangeListener(this);
		return widget;
	}

	@Override
	public AceEditorWidget getWidget() {
		return (AceEditorWidget) super.getWidget();
	}

	@Override
	public AceEditorState getState() {
		return (AceEditorState) super.getState();
	}

	@Override
	public void focusChanged(final boolean focused) {
		// TODO: it'd be better if we didn't register as listener
		// if !listenToFocusChanges in the first place...
		if (!this.listenToFocusChanges) {
			return;
		}

		if (this.isOnRoundtrip()) {
			this.sendAfterRoundtrip = SendCond.ALWAYS;
		}
		else {
			this.sendToServerImmediately(SendCond.ALWAYS);
		}
	}

	public void setTextChangeEventMode(final String mode) {
		final TextChangeEventMode newMode = TextChangeEventMode.valueOf(mode);
		if (newMode!=this.changeMode) {
			this.changeTextChangeEventMode(newMode);
		}
	}

	protected void setTextChangeTimeout(final int timeout) {
		this.changeTimeout = timeout;
	}

	protected void changeTextChangeEventMode(final TextChangeEventMode newMode) {
		if (this.sendTimer != null) {
			this.sendTimer.cancel();
			this.sendTimer = null;
		}
		this.changeMode = newMode;
	}

	protected void sendChangeAccordingToMode(final SendCond send) {
		this.sendChangeAccordingToMode(send, this.changeMode);
	}

	protected void sendChangeAccordingToMode(final SendCond send, final TextChangeEventMode mode) {
		if (mode == TextChangeEventMode.EAGER) {
			if (this.sendTimer != null) {
				this.sendTimer.cancel();
			}
			this.sendToServerImmediately(send);
		} else if (mode == TextChangeEventMode.LAZY) {
			if (this.sendTimer == null) {
				this.sendTimer = new SendTimer();
			}
			this.sendTimer.schedule(this.changeTimeout, send);
		} else if (mode == TextChangeEventMode.TIMEOUT) {
			if (this.sendTimer == null) {
				this.sendTimer = new SendTimer();
			}
			this.sendTimer.scheduleIfNotAlready(this.changeTimeout, send);
		}
	}

	protected void sendToServer(final SendCond send, final boolean immediately) {
		if (send==SendCond.NO) {
			return;
		}

		final AceDoc doc = this.getWidget().getDoc();
		final ClientSideDocDiff diff = ClientSideDocDiff.diff(this.shadow, doc);
		if (send==SendCond.ALWAYS) {
			// Go on...
		}
		else if (send==SendCond.IF_CHANGED && !diff.isIdentity()) {
			// Go on...
		}
		else {
			return;
		}

		final TransportDiff td = diff.asTransport();

		if (immediately) {
			this.serverRpc.changed(td, this.getWidget().getSelection().asTransport(), this.getWidget().isFocused());
		} else {
			this.serverRpc.changedDelayed(td, this.getWidget().getSelection().asTransport(), this.getWidget().isFocused());
		}

		this.shadow = doc;
		this.setOnRoundtrip(true); // What if delayed???
		this.sendAfterRoundtrip = SendCond.NO;
	}

	protected void sendToServerDelayed(final SendCond send) {
		this.sendToServer(send, false);
	}

	public void sendToServerImmediately() {
		this.sendToServerImmediately(SendCond.ALWAYS);
	}

	protected void sendToServerImmediately(final SendCond send) {
		this.sendToServer(send, true);
	}

	@Override
	public void flush() {
		super.flush();
		this.sendWhenPossible(SendCond.ALWAYS, TextChangeEventMode.EAGER); // ???
	}

	@Override
	public void changed() {
		if (this.isOnRoundtrip()) {
			this.sendAfterRoundtrip = this.sendAfterRoundtrip.or(SendCond.IF_CHANGED);
		}
		else {
			this.sendChangeAccordingToMode(SendCond.IF_CHANGED);
		}
	}

	@Override
	public void updateCaption(final ComponentConnector connector) {

	}

	@Override
	public void onConnectorHierarchyChange(
			final ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
	}

	@Override
	public void selectionChanged() {
		// TODO: it'd be better if we didn't register as listener
		// if !listenToSelectionChanges in the first place...
		if (this.listenToSelectionChanges) {
			this.sendWhenPossible(SendCond.ALWAYS);
		}
	}

	protected void sendWhenPossible(final SendCond send) {
		if (this.isOnRoundtrip()) {
			this.sendAfterRoundtrip = this.sendAfterRoundtrip.or(send);
		}
		else {
			this.sendChangeAccordingToMode(send);
		}
	}

	protected void sendWhenPossible(final SendCond send, final TextChangeEventMode mode) {
		if (this.isOnRoundtrip()) {
			this.sendAfterRoundtrip = this.sendAfterRoundtrip.or(send);
		}
		else {
			this.sendChangeAccordingToMode(send, mode);
		}
	}

	// TODO XXX not sure if this roundtrip thing is correct, seems to work ok...
	private void setOnRoundtrip(final boolean on) {
		if (on==this.onRoundtrip) {
			return;
		}
		this.onRoundtrip = on;
		if (!this.onRoundtrip) {
			this.sendToServerImmediately(this.sendAfterRoundtrip);
		}
	}

	public boolean isOnRoundtrip() {
		return this.onRoundtrip;
	}
}