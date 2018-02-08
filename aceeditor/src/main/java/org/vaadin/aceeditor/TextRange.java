package org.vaadin.aceeditor;

import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.Util;

public class TextRange extends AceRange {

	private static final long serialVersionUID = 1L;

	private final String text;
	int start = -1;
	int end = -1;

	public TextRange(final String text, final int row1, final int col1, final int row2, final int col2) {
		super(row1, col1, row2, col2);
		this.text = text;
	}

	public TextRange(final String text, final AceRange range) {
		this(text, range.getStartRow(), range.getStartCol(), range.getEndRow(), range.getEndCol());
	}

	public TextRange(final String text, final int start, final int end) {
		this(text, AceRange.fromPositions(start, end, text));
	}

	public int getStart() {
		if (this.start==-1) {
			this.start = Util.cursorPosFromLineCol(this.text, this.getStartRow(), this.getStartCol(), 0);
		}
		return this.start;
	}

	public int getEnd() {
		if (this.end==-1) {
			this.end = Util.cursorPosFromLineCol(this.text, this.getEndRow(), this.getEndCol(), 0);
		}
		return this.end;
	}

	public int getCursorPosition() {
		return this.getEnd();
	}

	public TextRange withNewText(final String newText) {
		return new TextRange(newText, this.getStart(), this.getEnd());
	}

}
