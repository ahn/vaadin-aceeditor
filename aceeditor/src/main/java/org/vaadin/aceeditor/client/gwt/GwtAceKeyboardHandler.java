package org.vaadin.aceeditor.client.gwt;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Handles key presses.
 * 
 */
public interface GwtAceKeyboardHandler {
	/**
	 * A command returned by
	 * {@link GwtAceKeyboardHandler#handleKeyboard(JavaScriptObject, int, String, int, JavaScriptObject)}
	 * .
	 * 
	 */
	public enum Command {
		/**
		 * Let the editor handle the key press like it normally would.
		 */
		DEFAULT,
		/**
		 * Prevent the editor from handling the key press.
		 */
		NULL
	}

	/**
	 * Called on key press.
	 * 
	 * @param data
	 * @param hashId
	 * @param keyString
	 * @param keyCode
	 * @param e
	 * @return command
	 */
	public Command handleKeyboard(JavaScriptObject data, int hashId,
			String keyString, int keyCode, GwtAceKeyboardEvent e);
}
