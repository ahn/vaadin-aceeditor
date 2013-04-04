package org.vaadin.aceeditor.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class AceClientRange implements Serializable {
	
	protected int row1;
	protected int col1;
	protected int row2;
	protected int col2;
	
	public AceClientRange() {
	}
	
	public AceClientRange(int row1, int col1, int row2, int col2) {
		this.row1 = row1;
		this.col1 = col1;
		this.row2 = row2;
		this.col2 = col2;
	}
	

	private boolean equalsSelection(int row1, int col1, int row2, int col2) {
		return this.row1 == row1 && this.col1 == col1 && this.row2 == row2
				&& this.col2 == col2;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AceClientRange) {
			AceClientRange ose = (AceClientRange) o;
			return equalsSelection(ose.row1, ose.col1, ose.row2, ose.col2);
		}
		return false;
	}
	
	public int getStartRow() {
		return row1;
	}
	
	public void setStartRow(int row) {
		row1 = row;
	}
	
	public int getStartCol() {
		return col1;
	}
	
	public void setStartCol(int col) {
		col1 = col;
	}
	
	public int getEndRow() {
		return row2;
	}
	
	public void setEndRow(int row) {
		row2 = row;
	}
	
	public int getEndCol() {
		return col2;
	}
	
	public void setEndCol(int col) {
		col2 = col;
	}
	
	public int getCursorRow() {
		return row2;
	}
	
	public int getCursorCol() {
		return col2;
	}
	
	@Override
	public String toString() {
		return "["+row1+","+col1+"-"+row2+","+col2+"]";
	}
	
	public boolean isBackwards() {
		return row1 > row2 || (row1==row2 && col1>col2);
	}
	
	public AceClientRange reversed() {
		return new AceClientRange(row2, col2, row1, col1);
	}
}