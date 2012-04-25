package org.vaadin.aceeditor.gwt.client;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * EditorFacade defines an interface for a code editor.
 * 
 * This was done so that it would be possible to use various
 * editors via this same interface. Don't know if this is bit of an overkill.
 * Currently implemented only by AceEditorFacade.
 */
public interface EditorFacade {

	/**
	 * Returns a {@link com.google.gwt.user.client.ui.Widget Widget} containing this editor.
	 * 
	 * @return
	 */
	public Widget getWidget();

	/**
	 * Initializes the editor.
	 * 
	 * Must be called before any other methods (except {@link #getWidget()}).
	 * 
	 * @return successful initialization
	 */
	// TODO: this might be refactored somehow, to get rid of the call-order-dependency...
	public boolean initializeEditor();

	public void settingsFromUIDL(UIDL uidl);

	public interface TextChangeListener {
		public void textChanged();
	}

	public interface CursorChangeListener {
		public void cursorChanged();
	}

	public interface SelectionChangeListener {
		public void selectionChanged();
	}

	public interface KeyPressHandler {
		public boolean keyPressed(Key key);
	}

	public static class Key {
		public final int keyCode;
		public final boolean alt;
		public final boolean shift;
		public final boolean ctrl;

		public Key(int keyCode) {
			this(keyCode, false, false, false);
		}

		public Key(int keyCode, boolean alt, boolean shift, boolean ctrl) {
			this.keyCode = keyCode;
			this.alt = alt;
			this.shift = shift;
			this.ctrl = ctrl;
		}

		@Override
		public String toString() {
			return (alt ? "alt-" : "") + (shift ? "shift-" : "")
					+ (ctrl ? "ctrl-" : "") + keyCode;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Key) {
				Key ok = (Key) other;
				return this.keyCode == ok.keyCode && this.alt == ok.alt
						&& this.shift == ok.shift && this.ctrl == ok.ctrl;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return keyCode;
		}
	}

	public void addListener(TextChangeListener li);

	public void addListener(CursorChangeListener li);

	public void addListener(SelectionChangeListener li);

	public void addHandler(KeyPressHandler ha);

	public boolean getFocus();

	public void setFocus(boolean focused);

	/**
	 * 
	 * @return the content of the editor
	 */
	public String getText();

	/**
	 * 
	 * @param text
	 * 
	 */
	public void setText(String text, boolean notifyListeners);

	/**
	 * Replace text at given position
	 * 
	 * @param startPos
	 * @param endPos
	 * @param text
	 *            the text to put between startPos and endPos
	 */
	public void replaceText(int startPos, int endPos, String text,
			boolean notifyListeners);

	public int getCursor();

	public void setCursor(int pos, boolean notifyListeners);

	/**
	 * 
	 * @param startPos
	 * @param endPos
	 * @param cursorAtStart
	 */
	public void setSelection(int startPos, int endPos, boolean cursorAtStart,
			boolean notifyListeners);

	/**
	 * 
	 * @return 2-array { startPos, endPos }
	 */
	public int[] getSelection();

	public void scrollTo(int position, boolean notifyListeners);

	void setTextCursorSelection(String text, int cursor, int selStartPos,
			int selEndPos, boolean notifyListeners);

	/**
	 * 
	 * @param readOnly
	 */
	public void setReadOnly(boolean readOnly);

	public int[] getCoordsOfPosition(int pos);

}
