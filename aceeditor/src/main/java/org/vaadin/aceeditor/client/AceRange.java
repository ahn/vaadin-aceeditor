package org.vaadin.aceeditor.client;

import java.io.Serializable;

import org.vaadin.aceeditor.client.TransportDoc.TransportRange;


/**
 *
 *
 */
public class AceRange implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int row1;
	private final int col1;
	private final int row2;
	private final int col2;


	public AceRange(final int row1, final int col1, final int row2, final int col2) {
		this.row1 = row1;
		this.col1 = col1;
		this.row2 = row2;
		this.col2 = col2;
	}

	public static AceRange fromPositions(final int start, final int end, final String text) {
		return AceRange.fromPositions(start, end, text.split("\n", -1));
	}

	public static AceRange fromPositions(final int start, final int end, final String[] lines) {
		final int[] rc1 = Util.lineColFromCursorPos(lines, start, 0);
		final int[] rc2 = start==end ? rc1 : Util.lineColFromCursorPos(lines, end, 0);
		return new AceRange(rc1[0], rc1[1], rc2[0], rc2[1]);
	}

	public int getStartRow() {
		return this.row1;
	}

	public int getStartCol() {
		return this.col1;
	}

	public int getEndRow() {
		return this.row2;
	}

	public int getEndCol() {
		return this.col2;
	}

	public int[] getPositions(final String text) {
		return this.getPositions(text.split("\n", -1));
	}

	public int[] getPositions(final String[] lines) {
		final int start = Util.cursorPosFromLineCol(lines, this.row1, this.col1, 0);
		final int end = this.isZeroLength() ? start : Util.cursorPosFromLineCol(lines, this.row2, this.col2, 0);
		return new int[]{start,end};
	}



	public TransportRange asTransport() {
		final TransportRange tr = new TransportRange();
		tr.row1 = this.getStartRow();
		tr.col1 = this.getStartCol();
		tr.row2 = this.getEndRow();
		tr.col2 = this.getEndCol();
		return tr;
	}

	@Override
	public boolean equals(final Object o) {
		if (o instanceof AceRange) {
			final AceRange or = (AceRange)o;
			return this.row1 == or.row1 && this.col1 == or.col1 && this.row2 == or.row2 && this.col2 == or.col2;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.row1+this.col1+this.row2+this.col2; // ?
	}

	public boolean isBackwards() {
		return this.row1> this.row2 || (this.row1==this.row2 && this.col1>this.col2);
	}

	public AceRange reversed() {
		return new AceRange(this.row2, this.col2, this.row1, this.col1);
	}

	public boolean isZeroLength() {
		return this.row1==this.row2 && this.col1==this.col2;
	}

	public static AceRange fromTransport(final TransportRange tr) {
		return new AceRange(tr.row1, tr.col1, tr.row2, tr.col2);
	}

	@Override
	public String toString() {
		return "[("+this.row1+","+this.col1+")-("+this.row2+","+this.col2+")]";
	}

}