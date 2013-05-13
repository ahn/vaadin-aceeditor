package org.vaadin.aceeditor.client;

import org.vaadin.aceeditor.client.TransportDiff.TransportRangeDiff;

public class RangeDiff {
	private final int drow1;
	private final int dcol1;
	private final int drow2;
	private final int dcol2;
	
	public RangeDiff(int drow1, int dcol1, int drow2, int dcol2) {
		this.drow1 = drow1;
		this.dcol1 = dcol1;
		this.drow2 = drow2;
		this.dcol2 = dcol2;
	}
	
	public static RangeDiff diff(AceRange r1, AceRange r2) {
		return new RangeDiff(
				r2.getStartRow() - r1.getStartRow(),
				r2.getStartCol() - r1.getStartCol(),
				r2.getEndRow() - r1.getEndRow(),
				r2.getEndCol() - r1.getEndCol());
	}
	
	public boolean isIdentity() {
		return drow1==0 && dcol1==0 && drow2==0 && dcol2==0;
	}
	
	public AceRange applyTo(AceRange r) {
		return new AceRange(
				r.getStartRow() + drow1,
				r.getStartCol() + dcol1,
				r.getEndRow() + drow2,
				r.getEndCol() + dcol2);
	}
	
	@Override
	public String toString() {
		return "(("+drow1+","+dcol1+"), ("+drow2+","+dcol2+"))";
	}

	public TransportRangeDiff asTransport() {
		return new TransportRangeDiff(drow1, dcol1, drow2, dcol2);
	}

	public static RangeDiff fromTransport(TransportRangeDiff trd) {
		return new RangeDiff(trd.drow1, trd.dcol1, trd.drow2, trd.dcol2);
	}

}
