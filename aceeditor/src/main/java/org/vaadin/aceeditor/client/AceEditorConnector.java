package org.vaadin.aceeditor.client;

import java.util.Map;
import java.util.Map.Entry;

import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.client.AceEditorWidget.ChangeListener;
import org.vaadin.aceeditor.client.AceEditorWidget.FocusChangeListener;
import org.vaadin.aceeditor.client.gwt.GwtAceEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.VConsole;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractHasComponentsConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

@SuppressWarnings("serial")
@Connect(AceEditor.class)
public class AceEditorConnector extends AbstractHasComponentsConnector
		implements ChangeListener, FocusChangeListener {

	private AceEditorServerRpc rpc = RpcProxy.create(AceEditorServerRpc.class,
			this);

	
	private enum TextChangeEventMode {
		EAGER, TIMEOUT, LAZY
	}

	private TextChangeEventMode changeMode = null;
	private int changeTimeout = 400;

	private class SendTimer extends Timer {
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
	};
	private SendTimer sendTimer = null;

	private AceEditorWidget widget;

	private boolean immediate;

	public AceEditorConnector() {
		
	}
	
	@Override
	public void init() {
		super.init();
		
		// Needed if inside a resizable subwindow.
		// Should we check that and only listen if yes?
		getLayoutManager().addElementResizeListener(widget.getElement(), new ElementResizeListener() {
			@Override
			public void onElementResize(ElementResizeEvent e) {
				widget.resize();
			}
		});
		
		
	}
	
	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);
		
		setTextChangeEventMode(getState().changeMode);
		setTextChangeTimeout(getState().changeTimeout);

		// TODO: are these needed?
//		widget.setHideErrors(getState().hideErrors);
//		widget.setRequired(getState().required);
//		widget.setModified(getState().modified);
		
		boolean firstTime = !widget.isInitialized();
		if (firstTime) {
			// To make sure Ace config is applied before the editor is created,
			// we delay the initialization till first call to onStateChanged,
			// not initializing in createWidget() right away.
			applyConfig(getState().config);
			widget.initialize();
			widget.setMode(getState().mode);
			widget.setTheme(getState().theme);
			widget.setListenToSelectionChanges(getState().listenToSelectionChanges);
			widget.setUseWorker(getState().useWorker);
			widget.setWordwrap(getState().wordwrap);
		}
		
		widget.setPropertyReadOnly(getState().propertyReadOnly);
		widget.setTabIndex(getState().tabIndex);
		widget.setReadOnly(getState().readOnly);
		
		immediate = getState().immediate;
		
		AceDocument doc = getState().document;
		if (firstTime || doc.isLatestChangeByServer()) {
			widget.setText(doc.getText());
			widget.setMarkers(doc.getMarkers());
			widget.setRowAnnotations(doc.getRowAnnotations());
			widget.setMarkerAnnotations(doc.getMarkerAnnotations());
		}
		
		if (!widget.isFocused()) {
			widget.setSelection(getState().selection);
		}
	}
	
	private static void applyConfig(Map<String, String> config) {
		for (Entry<String, String> e : config.entrySet()) {
			GwtAceEditor.setAceConfig(e.getKey(), e.getValue());
		}
		
	}

	@Override
	protected Widget createWidget() {
		widget = GWT.create(AceEditorWidget.class);
		widget.setChangeListener(this);
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
		VConsole.log("focusChanged("+focused+")");
		if (!focused) {
			if (immediate) {
				sendToServerImmediately();
			}
			else {
				sendToServerDelayed();
			}
		}
		
	}
	
	public void setTextChangeEventMode(String mode) {
		TextChangeEventMode newMode = TextChangeEventMode.valueOf(mode);
		if (newMode!=changeMode) {
			changeTextChangeEventMode(newMode);
		}
	}
	
	private void setTextChangeTimeout(int timeout) {
		changeTimeout = timeout;
	}
	
	private void changeTextChangeEventMode(TextChangeEventMode newMode) {
		if (sendTimer != null) {
			sendTimer.cancel();
			sendTimer = null;
		}
		this.changeMode = newMode;
	}
	
	public void textChanged() {
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
	
	private void sendToServerDelayed() {
		String text = widget.getText();
		AceDocument doc = new AceDocument();
		doc.setText(text);
		doc.setMarkers(widget.getMarkers());
		doc.setMarkerAnnotations(widget.getMarkerAnnotations());
		doc.setLatestChangeByServer(false);
		
		AceClientRange sel = widget.getSelection();
		boolean focus = widget.isFocused();
		rpc.changedDelayed(doc, sel, focus);
	}
	
	private void sendToServerImmediately() {
		sendToServerDelayed();
		rpc.sendNow();
	}
	
	@Override
	public void flush() {
		super.flush();
		sendToServerDelayed();
	}

	@Override
	public void changed() {
		textChanged();
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

}
