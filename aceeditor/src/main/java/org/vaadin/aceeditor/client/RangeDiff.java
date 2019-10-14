package org.vaadin.aceeditor.client;

import org.vaadin.aceeditor.client.TransportDiff.TransportRangeDiff;

public class RangeDiff {
	private final int drow1;
	private final int dcol1;
	private final int drow2;
	private final int dcol2;

	public RangeDiff(final int drow1, final int dcol1, final int drow2, final int dcol2) {
		this.drow1 = drow1;
		this.dcol1 = dcol1;
		this.drow2 = drow2;
		this.dcol2 = dcol2;
	}

	public static RangeDiff diff(final AceRange r1, final AceRange r2) {
		return new RangeDiff(
				r2.getStartRow() - r1.getStartRow(),
				r2.getStartCol() - r1.getStartCol(),
				r2.getEndRow() - r1.getEndRow(),
				r2.getEndCol() - r1.getEndCol());
	}

	public boolean isIdentity() {
		return this.drow1==0 && this.dcol1==0 && this.drow2==0 && this.dcol2==0;
	}

	public AceRange applyTo(final AceRange r) {
		return new AceRange(
				r.getStartRow() + this.drow1,
				r.getStartCol() + this.dcol1,
				r.getEndRow() + this.drow2,
				r.getEndCol() + this.dcol2);
	}

	@Override
	public String toString() {
		return "(("+this.drow1+","+this.dcol1+"), ("+this.drow2+","+this.dcol2+"))";
	}

	public TransportRangeDiff asTransport() {
		return new TransportRangeDiff(this.drow1, this.dcol1, this.drow2, this.dcol2);
	}

	public static RangeDiff fromTransport(final TransportRangeDiff trd) {
		return new RangeDiff(trd.drow1, trd.dcol1, trd.drow2, trd.dcol2);
	}

}
