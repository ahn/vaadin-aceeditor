package org.vaadin.aceeditor.gwt.client;

import java.util.LinkedList;

import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.aceeditor.gwt.ace.AceTheme;
import org.vaadin.aceeditor.gwt.ace.GwtAceChangeCursorHandler;
import org.vaadin.aceeditor.gwt.ace.GwtAceChangeEvent;
import org.vaadin.aceeditor.gwt.ace.GwtAceChangeHandler;
import org.vaadin.aceeditor.gwt.ace.GwtAceChangeSelectionHandler;
import org.vaadin.aceeditor.gwt.ace.GwtAceEditor;
import org.vaadin.aceeditor.gwt.ace.GwtAceEditorWidget;
import org.vaadin.aceeditor.gwt.ace.GwtAceEvent;
import org.vaadin.aceeditor.gwt.ace.GwtAceKeyboardHandler;
import org.vaadin.aceeditor.gwt.ace.GwtAcePosition;
import org.vaadin.aceeditor.gwt.ace.GwtAceRange;
import org.vaadin.aceeditor.gwt.ace.GwtAceSelection;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.UIDL;

public class AceEditorFacade implements EditorFacade, GwtAceChangeHandler,
		GwtAceChangeCursorHandler, GwtAceChangeSelectionHandler, GwtAceKeyboardHandler {

	private GwtAceEditorWidget editorWidget;
	protected GwtAceEditor editor;

	private LinkedList<TextChangeListener> textListeners;
	private LinkedList<CursorChangeListener> cursorListeners;
	private LinkedList<SelectionChangeListener> selectionListeners;
	private LinkedList<KeyPressHandler> keyPressHandlers;

	// The positions at the beginning of each row is cached here
	// for faster conversion between pos <~~> (row,column).
	private int[] posAtBeginningOfRow;

	private int[] prevPosAtBeginningOfRow;
	private boolean ignoreAceEvents;
	private AceMode mode;
	private AceTheme theme;
	private String newLineChar;

	public AceEditorFacade() {
		super();
		editorWidget = new GwtAceEditorWidget();
		editor = editorWidget.getAceEditor();
	}

	/* @Override */
	public boolean initializeEditor() {
		editor = editorWidget.createEditor();
		editor.addChangeHandler(this);
		newLineChar = editor.getNewLineCharacter();
		return editor != null;
	}

	/* @Override */
	public Widget getWidget() {
		return editorWidget;
	}

	/* @Override */
	public void settingsFromUIDL(UIDL uidl) {
		if (uidl.hasAttribute("ace-mode")) {
			AceMode mode = AceMode.valueOf(uidl.getStringAttribute("ace-mode"));
			String url = uidl.getStringAttribute("ace-mode-url");
			setMode(mode, url);
		}
		if (uidl.hasAttribute("ace-theme")) {
			AceTheme theme = AceTheme.valueOf(uidl
					.getStringAttribute("ace-theme"));
			String url = uidl.getStringAttribute("ace-theme-url");
			setTheme(theme, url);
		}
		if (uidl.hasAttribute("ace-font-size")) {
			String size = uidl.getStringAttribute("ace-font-size");
			editor.setFontSize(size);
		}
		if (uidl.hasAttribute("ace-hscroll-visible")) {
			boolean visible = uidl.getBooleanAttribute("ace-hscroll-visible");
			editor.setHScrollBarAlwaysVisible(visible);
		}
		if (uidl.hasAttribute("ace-use-wrapmode")) {
			boolean useWrapMode = uidl.getBooleanAttribute("ace-use-wrapmode");
			editor.setUseWrapMode(useWrapMode);
		}
	}

	private void setMode(AceMode mode, String url) {
		if (this.mode == mode) {
			return;
		}
		if (url != null) {
			editor.setMode(mode, url);
		} else {
			editor.setMode(mode);
		}
		this.mode = mode;
	}

	private void setTheme(AceTheme theme, String url) {
		if (this.theme == theme) {
			return;
		}
		if (url != null) {
			editor.setTheme(theme, url);
		} else {
			editor.setTheme(theme);
		}
		this.theme = theme;
	}

	/* @Override */
	public void addListener(TextChangeListener li) {
		if (textListeners == null) {
			textListeners = new LinkedList<TextChangeListener>();
		}
		textListeners.add(li);
	}

	/* @Override */
	public void addListener(CursorChangeListener li) {
		if (cursorListeners == null) {
			cursorListeners = new LinkedList<CursorChangeListener>();
			editor.addChangeCursorHandler(this);
		}
		cursorListeners.add(li);
	}

	/* @Override */
	public void addListener(SelectionChangeListener li) {
		if (selectionListeners == null) {
			selectionListeners = new LinkedList<SelectionChangeListener>();
			editor.addChangeSelectionHandler(this);
		}
		selectionListeners.add(li);
	}

	/* @Override */
	public void addHandler(KeyPressHandler ha) {
		if (keyPressHandlers == null) {
			keyPressHandlers = new LinkedList<KeyPressHandler>();
			editor.setKeyboardHandler(this);
		}
		keyPressHandlers.add(ha);
	}

	/* @Override */
	public boolean getFocus() {
		return editor.isFocused();
	}

	/* @Override */
	public String getText() {
		return editor.getText();
	}

	/* @Override */
	public void setText(String text, boolean notifyListeners) {
		ignoreAceEvents = !notifyListeners;
		editor.setText(text);
		ignoreAceEvents = false;
	}

	/* @Override */
	public void replaceText(int startPos, int endPos, String text,
			boolean notifyListeners) {
		ignoreAceEvents = !notifyListeners;
		editor.replace(
				aceRangeFromStartEnd(startPos, endPos,
						getPosAtBeginningOfRows()), text);
		ignoreAceEvents = false;
	}

	/* @Override */
	public int getCursor() {
		return posFromAcePos(editor.getCursorPosition(),
				getPosAtBeginningOfRows());
	}

	/* @Override */
	public void setCursor(int pos, boolean notifyListeners) {
		ignoreAceEvents = !notifyListeners;
		editor.moveCursorToPosition(acePosFromPos(pos,
				getPosAtBeginningOfRows()));
		ignoreAceEvents = false;
	}

	/* @Override */
	public void setSelection(int startPos, int endPos, boolean reverse,
			boolean notifyListeners) {
		ignoreAceEvents = !notifyListeners;
		editor.setSelection(
				aceRangeFromStartEnd(startPos, endPos,
						getPosAtBeginningOfRows()), reverse);
		ignoreAceEvents = false;
	}

	/* @Override */
	public int[] getSelection() {
		GwtAceSelection sel = editor.getSelection();
		return fromAceRange(sel.getRange(), sel.isBackwards(),
				getPosAtBeginningOfRows());
	}

	/* @Override */
	public void scrollTo(int pos, boolean notifyListeners) {
		ignoreAceEvents = !notifyListeners;
		editor.scrollToRow(acePosFromPos(pos, getPosAtBeginningOfRows())
				.getRow());
		ignoreAceEvents = false;
	}

	/* @Override */
	public void setReadOnly(boolean readOnly) {
		editor.setReadOnly(readOnly);
	}

	/* @Override */
	public int[] getCoordsOfPosition(int pos) {
		JsArrayInteger cc = editor.getCoordsOf(acePosFromPos(pos,
				getPosAtBeginningOfRows()));
		return new int[] { cc.get(0), cc.get(1) };
	}

	/* @Override */
	public void onChange(GwtAceChangeEvent e) {
		prevPosAtBeginningOfRow = posAtBeginningOfRow;
		posAtBeginningOfRow = null;

		onChangeBeforeCallingListeners(e);

		if (!ignoreAceEvents && textListeners != null) {
			for (TextChangeListener li : textListeners) {
				li.textChanged();
			}
		}
	}

	protected void onChangeBeforeCallingListeners(GwtAceChangeEvent e) {
		// Empty. Subclass may want to override this to do something
		// after a text change before the change is forwarded to listeners.
	}

	/* @Override */
	public void onChangeCursor(GwtAceEvent e) {
		if (!ignoreAceEvents) {
			for (CursorChangeListener li : cursorListeners) {
				li.cursorChanged();
			}
		}
	}

	protected void onChangeCursorBeforeCallingListeners(GwtAceChangeEvent e) {
		// Empty for this class.
	}

	/* @Override */
	public void onChangeSelection(GwtAceEvent e) {
		if (!ignoreAceEvents) {
			for (SelectionChangeListener li : selectionListeners) {
				li.selectionChanged();
			}
		}
	}

	protected void onChangeSelectionBeforeCallingListeners(GwtAceChangeEvent e) {
		// Empty for this class.
	}

	/* @Override */
	public Command handleKeyboard(JavaScriptObject data, int hashId,
			String keyString, int keyCode, JavaScriptObject e) {
		Key key = keyFrom(keyCode, hashId);
		boolean propagate = true;
		if (keyPressHandlers != null) {
			for (KeyPressHandler handler : keyPressHandlers) {
				if (!handler.keyPressed(key)) {
					propagate = false;
				}
			}
		}
		return propagate ? Command.DEFAULT : Command.NULL;
	}

	protected static int[] fromAceRange(GwtAceRange range, boolean backwards,
			int[] pabor) {
		int start = posFromAcePos(range.getStart(), pabor);
		int end = posFromAcePos(range.getEnd(), pabor);
		if (backwards) {
			return new int[] { end, start };
		} else {
			return new int[] { start, end };
		}
	}

	protected int[] getPosAtBeginningOfRows() {
		if (posAtBeginningOfRow == null) {
			String[] lines = editor.getText().split(newLineChar, -1);
			// + 1 for extra "after last line" to simplify algorithms
			posAtBeginningOfRow = new int[lines.length + 1];

			int pos = 0;
			for (int li = 0; li < lines.length; ++li) {
				posAtBeginningOfRow[li] = pos;
				pos += lines[li].length() + newLineChar.length();
			}
			posAtBeginningOfRow[lines.length] = pos;
		}
		return posAtBeginningOfRow;
	}

	protected int[] getPrevPosAtBeginningOfRows() {
		return prevPosAtBeginningOfRow;
	}

	protected static GwtAcePosition acePosFromPos(int pos, int[] pabor) {
		int i = 1;
		for (; i < pabor.length; ++i) {
			if (pabor[i] > pos) {
				return GwtAcePosition.create(i - 1, pos - pabor[i - 1]);
			}
		}
		// pos not in text
		return GwtAcePosition.create(pabor.length, 0);
	}

	protected static int posFromAcePos(GwtAcePosition pos, int[] pabor) {
		return pabor[pos.getRow()] + pos.getColumn();
	}

	protected static GwtAceRange aceRangeFromStartEnd(int start, int end,
			int[] pabor) {
		int startRow = 0, startCol = 0, endRow = 0, endCol = 0;
		int i = 1;
		for (; i < pabor.length; ++i) {
			if (pabor[i] > start) {
				startRow = i - 1;
				startCol = start - pabor[i - 1];
				break;
			}
		}
		for (; i < pabor.length; ++i) {
			if (pabor[i] > end) {
				endRow = i - 1;
				endCol = end - pabor[i - 1];
				break;
			}
		}
		return GwtAceRange.create(startRow, startCol, endRow, endCol);
	}

	/* @Override */
	public void setFocus(boolean focused) {
		if (focused) {
			editor.focus();
		} else {
			editor.blur();
		}
	}

	private static Key keyFrom(int keyCode, int hashId) {
		boolean alt = (hashId & 2) != 0;
		boolean shift = (hashId & 4) != 0;
		boolean ctrl = (hashId & 1) != 0;
		return new Key(keyCode, alt, shift, ctrl);
	}

	/* @Override */
	public void setTextCursorSelection(String text, int cursor,
			int selStartPos, int selEndPos, boolean notifyListeners) {

		GwtAcePosition oldCursor = editor.getCursorPosition();

		double scrollTopBefore = editor.getScrollTopRow();

		ignoreAceEvents = !notifyListeners;
		editor.setText(text);

		GwtAceRange selection = aceRangeFromStartEnd(selStartPos, selEndPos,
				getPosAtBeginningOfRows());
		GwtAcePosition newCursor = acePosFromPos(cursor, getPosAtBeginningOfRows());

		editor.moveCursorToPosition(newCursor);
		editor.setSelection(selection, false);

		int cursorMovedRows = newCursor.getRow() - oldCursor.getRow();
		double newScrollTop = scrollTopBefore + cursorMovedRows;
		editor.scrollToRow(newScrollTop);

		ignoreAceEvents = false;
	}

	public void resize() {
		if (editor!=null) {
			editor.resize();
		}
		
	}

	// protected static native void consolelog(JavaScriptObject obj) /*-{
	// console.log(obj);
	// }-*/;

}
