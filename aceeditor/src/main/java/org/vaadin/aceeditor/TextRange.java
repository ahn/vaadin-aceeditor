package org.vaadin.aceeditor;

import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.Util;

public class TextRange extends AceRange {

	private final String text;
	int start = -1;
	int end = -1;
	
	public TextRange(String text, int row1, int col1, int row2, int col2) {
		super(row1, col1, row2, col2);
		this.text = text;
	}
	
	public TextRange(String text, AceRange range) {
		this(text, range.getStartRow(), range.getStartCol(), range.getEndRow(), range.getEndCol());
	}
	
	public TextRange(String text, int start, int end) {
		this(text, AceRange.fromPositions(start, end, text));
	}

	public int getStart() {
		if (start==-1) {
			start = Util.cursorPosFromLineCol(text, getStartRow(), getStartCol(), 0);
		}
		return start;
	}

	public int getEnd() {
		if (end==-1) {
			end = Util.cursorPosFromLineCol(text, getEndRow(), getEndCol(), 0);
		}
		return end;
	}
	
	public int getCursorPosition() {
		return getEnd();
	}
	
	public TextRange withNewText(String newText) {
		return new TextRange(newText, getStart(), getEnd());
	}
	
}
