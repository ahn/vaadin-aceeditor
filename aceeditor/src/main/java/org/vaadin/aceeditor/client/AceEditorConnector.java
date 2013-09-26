package org.vaadin.aceeditor.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.client.AceEditorWidget.FocusChangeListener;
import org.vaadin.aceeditor.client.AceEditorWidget.SelectionChangeListener;
import org.vaadin.aceeditor.client.AceEditorWidget.TextChangeListener;
import org.vaadin.aceeditor.client.gwt.GwtAceEditor;

import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
@Connect(AceEditor.class)
public class AceEditorConnector extends AbstractHasComponentsConnector
		implements TextChangeListener, SelectionChangeListener, FocusChangeListener {

//	private static Logger logger = Logger.getLogger(AceEditorConnector.class.getName());

    protected AceEditorServerRpc serverRpc =
            RpcProxy.create(AceEditorServerRpc.class, this);


    protected enum TextChangeEventMode {
		EAGER, TIMEOUT, LAZY
	}

    protected TextChangeEventMode changeMode = null;
    protected int changeTimeout = 400;

    protected class SendTimer extends Timer {
		private boolean scheduled;

		@Override
		public void schedule(int ms) {
			super.schedule(ms);
			scheduled = true;
		}

		public void scheduleIfNotAlready(int ms) {
			if (!scheduled) {
				schedule(ms);
			}
		}

		@Override
		public void run() {
			scheduled = false;
			sendToServerImmediately();
		}
	}

	protected SendTimer sendTimer = null;

    protected AceDoc shadow;

    protected boolean onRoundtrip = false;
    protected boolean docChangedWhileOnRountrip = false;

    protected AceEditorClientRpc clientRpc = new AceEditorClientRpc() {
		@Override
		public void diff(TransportDiff ad) {
			ClientSideDocDiff diff = ClientSideDocDiff.fromTransportDiff(ad);
			shadow = diff.applyTo(shadow);

			AceDoc doc1 = getWidget().getDoc();
			AceDoc doc2 = diff.applyTo(doc1);

            getWidget().setDoc(doc2);
			
			setOnRoundtrip(false);
		}

		@Override
		public void changedOnServer() {
			sendToServer(true, true);
		}

		@Override
		public void scrollToRow(int row) {
			getWidget().scrollToRow(row);
		}

	};

    protected boolean listenToSelectionChanges;

    protected boolean selectionChanged;

	
	public AceEditorConnector() {
		super();
		registerRpc(AceEditorClientRpc.class, clientRpc);
	}
	
	

	@Override
	public void init() {
		super.init();
		
		// Needed if inside a resizable subwindow.
		// Should we check that and only listen if yes?
		getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {
			@Override
			public void onElementResize(ElementResizeEvent e) {
                getWidget().resize();
			}
		});
	}
	
	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		
		setTextChangeEventMode(getState().changeMode);
		setTextChangeTimeout(getState().changeTimeout);
		
		ClientSideDocDiff.dmp.setDiff_EditCost(getState().diff_editCost);

		// TODO: are these needed?
//		widget.setHideErrors(getState().hideErrors);
//		widget.setRequired(getState().required);
//		widget.setModified(getState().modified);
		
		boolean firstTime = !getWidget().isInitialized();
		if (firstTime) {
			// To make sure Ace config is applied before the editor is created,
			// we delay the initialization till then first call to onStateChanged,
			// not initializing in createWidget() right away.
			applyConfig(getState().config);
            getWidget().initialize();
		}

        getWidget().setMode(getState().mode);
        getWidget().setTheme(getState().theme);
		listenToSelectionChanges = getState().listenToSelectionChanges;
        getWidget().setUseWorker(getState().useWorker);
        getWidget().setWordwrap(getState().wordwrap);

        getWidget().setPropertyReadOnly(getState().propertyReadOnly);
        getWidget().setTabIndex(getState().tabIndex);
        getWidget().setReadOnly(getState().readOnly);
		
		// TODO: How should we deal with immediateness. Since there's already textChangeEventMode...
		//immediate = getState().immediate;
		
		if (firstTime) {
			shadow = AceDoc.fromTransport(getState().initialValue);
            getWidget().setDoc(shadow);
		}
		
		if (firstTime || getState().selectionFromServer > 0) {
            getWidget().setSelection(AceRange.fromTransport(getState().selection));
		}
	}
	
	protected static void applyConfig(Map<String, String> config) {
		for (Entry<String, String> e : config.entrySet()) {
			GwtAceEditor.setAceConfig(e.getKey(), e.getValue());
		}
	}

	@Override
	protected Widget createWidget() {
        AceEditorWidget widget = GWT.create(AceEditorWidget.class);
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
	public void focusChanged(boolean focused) {
		if (!focused) {
			sendToServerImmediately(); // ???
		}
	}
	
	public void setTextChangeEventMode(String mode) {
		TextChangeEventMode newMode = TextChangeEventMode.valueOf(mode);
		if (newMode!=changeMode) {
			changeTextChangeEventMode(newMode);
		}
	}

    protected void setTextChangeTimeout(int timeout) {
		changeTimeout = timeout;
	}

    protected void changeTextChangeEventMode(TextChangeEventMode newMode) {
		if (sendTimer != null) {
			sendTimer.cancel();
			sendTimer = null;
		}
		this.changeMode = newMode;
	}
	
	public void sendChangeAccordingToPolicy() {
		if (changeMode == TextChangeEventMode.EAGER) {
			sendToServerImmediately();
		} else if (changeMode == TextChangeEventMode.LAZY) {
			if (sendTimer == null) {
				sendTimer = new SendTimer();
			}
			sendTimer.schedule(changeTimeout);
		} else if (changeMode == TextChangeEventMode.TIMEOUT) {
			if (sendTimer == null) {
				sendTimer = new SendTimer();
			}
			sendTimer.scheduleIfNotAlready(changeTimeout);
		}
	}

    protected void sendToServer(boolean immediately, boolean evenIfIdentity) {
		AceDoc doc = getWidget().getDoc();
		ClientSideDocDiff diff = ClientSideDocDiff.diff(shadow, doc);
		if (evenIfIdentity || !diff.isIdentity()) {
			
		}
		else if (listenToSelectionChanges && selectionChanged) {
			
		}
		else {
			return;
		}
		
		TransportDiff td = diff.asTransport();
		
		if (immediately) {
			serverRpc.changed(td, getWidget().getSelection().asTransport(), getWidget().isFocused());
		} else {
			serverRpc.changedDelayed(td, getWidget().getSelection().asTransport(), getWidget().isFocused());
		}
		
		shadow = doc;
		setOnRoundtrip(true); // What if delayed???
		docChangedWhileOnRountrip = false;
		selectionChanged = false;
	}

    protected void sendToServerDelayed() {
		sendToServer(false, false);
	}

    protected void sendToServerImmediately() {
		sendToServer(true, false);
	}
	
	@Override
	public void flush() {
		super.flush();
		sendToServerDelayed(); // ???
	}

	@Override
	public void changed() {
		if (isOnRoundtrip()) {
			docChangedWhileOnRountrip = true;
		}
		else {
			sendChangeAccordingToPolicy();
		}
	}

	@Override
	public void updateCaption(ComponentConnector connector) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectorHierarchyChange(
			ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged() {
		selectionChanged = true;
		if (listenToSelectionChanges) {
			sendChangeAccordingToPolicy();
		}
	}

	// TODO XXX not sure if this roundtrip thing is correct, seems to work ok...
	public void setOnRoundtrip(boolean on) {
		if (on==onRoundtrip) {
			return;
		}
		onRoundtrip = on;
		if (!onRoundtrip && docChangedWhileOnRountrip) {
			sendToServer(true, false);
		}
	}
	
	public boolean isOnRoundtrip() {
		return onRoundtrip;
	}
}