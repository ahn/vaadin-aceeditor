package org.vaadin.aceeditor.gwt.client;

import org.vaadin.aceeditor.gwt.client.EditorFacade.CursorChangeListener;
import org.vaadin.aceeditor.gwt.client.EditorFacade.TextChangeListener;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.ContainerResizedListener;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.VTextField;

public class VAceEditor extends Composite implements Paintable,
		TextChangeListener, CursorChangeListener, ContainerResizedListener {

	private String text;
	private int cursor;

	private enum TextChangeEventMode {
		EAGER, TIMEOUT, LAZY
	}

	private TextChangeEventMode iem = null;
	private int iet = 400;

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
			client.sendPendingVariableChanges();
			scheduled = false;
		}
	};

	private SendTimer sendTimer = null;

	protected ApplicationConnection client;
	protected String paintableId;

	protected AceEditorFacade editor;
	private boolean aceInitialized = false;

	private boolean immediate = false;

	public VAceEditor() {
		this(true);
	}

	protected VAceEditor(boolean initAceWidget) {
		super();
		if (initAceWidget) {
			editor = new AceEditorFacade();
			initWidget(editor.getWidget());
		}
	}

	protected String getText() {
		return text;
	}

	protected int getCursor() {
		return cursor;
	}

//	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		this.client = client;
		paintableId = uidl.getId();

		if (!aceInitialized) {
			aceInitialized = editor.initializeEditor();
			if (!aceInitialized) {
				VConsole.log("Could not initialize Ace editor!");
				return;
			}
			editor.addListener((TextChangeListener) this);
			editor.addListener((CursorChangeListener) this);
		}

		textFieldSettingsFromUIDL(uidl);

		editor.settingsFromUIDL(uidl);

		boolean nvc = uidl.hasAttribute("nvc")
				&& uidl.getBooleanAttribute("nvc");
		if (!nvc && textUpdateAllowed() && uidl.hasVariable("text")) {
			String text = uidl.getStringVariable("text");
			if (!text.equals(this.text)) {
				this.text = text;
				editor.setText(text, false);
			}
		}

		if (uidl.hasAttribute("selpos")) {
			int selPos = uidl.getIntAttribute("selpos");
			int selLen = uidl.getIntAttribute("sellen");
			editor.setSelection(selPos, selPos + selLen, false, false);
		}

	}

	protected boolean textUpdateAllowed() {
		return true;
	}

	private void textFieldSettingsFromUIDL(UIDL uidl) {
		if (uidl.hasAttribute("immediate")) {
			immediate = uidl.getBooleanAttribute("immediate");
		} else {
			immediate = false;
		}

		if (uidl.hasAttribute("readonly")) {
			editor.setReadOnly(uidl.getBooleanAttribute("readonly"));
		} else {
			editor.setReadOnly(false);
		}

		if (uidl.hasAttribute("iem")) {
			setIEM(TextChangeEventMode.valueOf(uidl.getStringAttribute("iem")));
		} else {
			setIEM(null);
		}

		if (uidl.hasAttribute("iet")) {
			setIET(uidl.getIntAttribute("iet"));
		}
	}

//	@Override
	public void textChanged() {
		text = editor.getText();
		client.updateVariable(paintableId, "text", text, false);
		if (iem == TextChangeEventMode.EAGER || immediate) {
			client.sendPendingVariableChanges();
		} else if (iem == TextChangeEventMode.LAZY) {
			if (sendTimer == null) {
				sendTimer = new SendTimer();
			}
			sendTimer.schedule(iet);
		} else if (iem == TextChangeEventMode.TIMEOUT) {
			if (sendTimer == null) {
				sendTimer = new SendTimer();
			}
			sendTimer.scheduleIfNotAlready(iet);
		}
	}

//	@Override
	public void cursorChanged() {
		cursor = editor.getCursor();
		client.updateVariable(paintableId, VTextField.VAR_CURSOR, cursor, false);
	}

	private void setIEM(TextChangeEventMode iem) {
		if (this.iem == iem) {
			return;
		}
		if (sendTimer != null) {
			sendTimer.cancel();
			sendTimer = null;
		}
		this.iem = iem;
	}

	private void setIET(int iet) {
		this.iet = iet;
	}

	// Implementing ContainerResizedListener, to make the editor
	// recalculate its layout when its parent subwindow resizes, etc.
	public void iLayout() {
		editor.resize();
	}

}
