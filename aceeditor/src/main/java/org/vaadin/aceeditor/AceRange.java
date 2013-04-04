package org.vaadin.aceeditor;

import org.vaadin.aceeditor.client.AceClientRange;
import org.vaadin.aceeditor.client.Util;

/**
 * 
 * A range that in addition to defining start and end as (column ,row)
 * can calculate the start and end *positions* based on text.
 *
 */
public class AceRange extends AceClientRange {
	private final String text;
	private int pos1 = -1;
	private int pos2 = -1;
	
	public AceRange(int pos1, int pos2, String text) {
		this.text = text;
		this.pos1 = pos1;
		this.pos2 = pos2;
		int[] rc1 = Util.lineColFromCursorPos(text, pos1, 0);
		row1 = rc1[0];
		col1 = rc1[1];
		int[] rc2 = Util.lineColFromCursorPos(text, pos2, 0);
		row2 = rc2[0];
		col2 = rc2[1];
	}
	
	public AceRange(int row1, int col1, int row2, int col2, String text) {
		super(row1,col1,row2,col2);
		this.text = text;
	}
	
	public AceRange(AceClientRange sel, String text) {
		this(sel.getStartRow(), sel.getStartCol(), sel.getEndRow(), sel.getEndCol(), text);
	}

	public int getCursorPosition() {
		return getEndPosition();
	}
	
	public int getStartPosition() {
		if (pos1 < 0) {
			pos1 = Util.cursorPosFromLineCol(text, row1, col1, 0);
		}
		return pos1;
	}
	
	public int getEndPosition() {
		if (pos2 < 0) {
			pos2 = Util.cursorPosFromLineCol(text, row2, col2, 0);
		}
		return pos2;
	}

	public static AceRange fromPositions(int start, int end, String text) {
		int[] rc1 = Util.lineColFromCursorPos(text, start, 0);
		int[] rc2 = Util.lineColFromCursorPos(text, end, 0);
		return new AceRange(rc1[0],rc1[1],rc2[0],rc2[1],text);
	}
}