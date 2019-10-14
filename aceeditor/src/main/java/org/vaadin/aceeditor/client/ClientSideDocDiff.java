package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.GwtTextDiff.Diff;
import org.vaadin.aceeditor.client.GwtTextDiff.Patch;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;

import com.google.gwt.core.client.JsArray;



public class ClientSideDocDiff {

	public static final DiffMatchPatchJSNI dmp = DiffMatchPatchJSNI.newInstance();

	private final JsArray<GwtTextDiff.Patch> textPatches;
	private final MarkerSetDiff markerSetDiff;
	private final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff;
	private final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff;

	public static ClientSideDocDiff fromTransportDiff(final TransportDiff ad) {

		final JsArray<Patch> patches = ClientSideDocDiff.dmp.patch_fromText(ad.patchesAsString);
		final MarkerSetDiff msd = MarkerSetDiff.fromTransportDiff(ad.markerSetDiff);

		final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnns = ad.rowAnnDiff==null ? null :
			SetDiff.fromTransport(ad.rowAnnDiff);
		final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnns =  ad.markerAnnDiff==null ? null :
			SetDiff.fromTransport(ad.markerAnnDiff);

		return new ClientSideDocDiff(patches, msd, rowAnns, markerAnns);
	}

	public static ClientSideDocDiff diff(final AceDoc doc1, final AceDoc doc2) {
		final JsArray<GwtTextDiff.Patch> patches = ClientSideDocDiff.dmp.patch_make(doc1.getText(), doc2.getText());
		final MarkerSetDiff msd = MarkerSetDiff.diff(doc1.getMarkers(), doc2.getMarkers(), doc2.getText());

		final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff = ClientSideDocDiff.diffRA(doc1.getRowAnnotations(), doc2.getRowAnnotations());
		final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff = ClientSideDocDiff.diffMA(doc1.getMarkerAnnotations(), doc2.getMarkerAnnotations());

		return new ClientSideDocDiff(patches, msd, rowAnnDiff, markerAnnDiff);
	}


	//TODO XXX
	private static SetDiff<MarkerAnnotation, TransportMarkerAnnotation> diffMA(
			Set<MarkerAnnotation> anns1,
			Set<MarkerAnnotation> anns2) {
		if (anns2 == null && anns1 != null) {
			return null;
		}
		if (anns1==null) {
			anns1 = Collections.emptySet();
		}
		if (anns2==null) {
			anns2 = Collections.emptySet();
		}
		return new SetDiff.Differ<MarkerAnnotation,TransportMarkerAnnotation>().diff(anns1, anns2);
	}

	//TODO XXX
	private static SetDiff<RowAnnotation, TransportRowAnnotation> diffRA(
			Set<RowAnnotation> anns1,
			Set<RowAnnotation> anns2) {
		if (anns2 == null && anns1 != null) {
			return null;
		}
		if (anns1==null) {
			anns1 = Collections.emptySet();
		}
		if (anns2==null) {
			anns2 = Collections.emptySet();
		}
		return new SetDiff.Differ<RowAnnotation,TransportRowAnnotation>().diff(anns1, anns2);
	}

	private ClientSideDocDiff(final JsArray<GwtTextDiff.Patch> patches, final MarkerSetDiff markerSetDiff,
			final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff,
			final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff) {
		this.textPatches = patches;
		this.markerSetDiff = markerSetDiff;
		this.rowAnnDiff = rowAnnDiff;
		this.markerAnnDiff = markerAnnDiff;
	}

	public String getPatchesString() {
		return ClientSideDocDiff.dmp.patch_toText(this.textPatches);
	}

	public AceDoc applyTo(final AceDoc doc) {
		final String text = ClientSideDocDiff.dmp.patch_apply(this.textPatches, doc.getText());
		final Map<String, AceMarker> markers = this.markerSetDiff.applyTo(doc.getMarkers(), text);

		final Set<RowAnnotation> rowAnns = this.rowAnnDiff==null ? null : this.rowAnnDiff.applyTo(doc.getRowAnnotations());
		final Set<MarkerAnnotation> markerAnns = this.markerAnnDiff==null ? null : this.markerAnnDiff.applyTo(doc.getMarkerAnnotations());

		return new AceDoc(text, markers, rowAnns, markerAnns);
	}

	public TransportDiff asTransport() {
		final TransportDiff d = new TransportDiff();
		d.patchesAsString = this.getPatchesString();
		d.markerSetDiff = this.markerSetDiff.asTransportDiff();
		d.rowAnnDiff = this.rowAnnDiff==null ? null : this.rowAnnDiff.asTransportRowAnnotations();
		d.markerAnnDiff = this.markerAnnDiff==null ? null : this.markerAnnDiff.asTransportMarkerAnnotations();
		return d;
	}

	public boolean isIdentity() {
		return this.textPatches == null || this.textPatches.length()==0;
	}

	@Override
	public String toString() {
		return this.getPatchesString() + "\nMSD: " + this.markerSetDiff.toString() + "\nrad:" + this.rowAnnDiff + ", mad:" + this.markerAnnDiff;
	}

	public static class Adjuster {
		private final String s1;
		private final String s2;
		private String[] lines1;
		private String[] lines2;
		private JsArray<Diff> diffs;
		private final boolean stringsEqual;
		private boolean calcDone;
		public Adjuster(final String s1, final String s2) {
			this.s1 = s1;
			this.s2 = s2;
			this.stringsEqual = s1.equals(s2);
		}
		public AceRange adjust(final AceRange r) {
			if (this.stringsEqual) {
				return r;
			}
			if (!this.calcDone) {
				this.calc();
			}
			final boolean zeroLength = r.isZeroLength();
			final int start1 = Util.cursorPosFromLineCol(this.lines1, r.getStartRow(), r.getStartCol(), 0);
			final int end1 = zeroLength ? start1 : Util.cursorPosFromLineCol(this.lines1, r.getEndRow(), r.getEndCol(), 0);
			final int start2 = ClientSideDocDiff.dmp.diff_xIndex(this.diffs, start1);
			final int end2 = zeroLength ? start2 : ClientSideDocDiff.dmp.diff_xIndex(this.diffs, end1);
			final int[] startRowCol = Util.lineColFromCursorPos(this.lines2, start2, 0);
			final int[] endRowCol = zeroLength ? startRowCol : Util.lineColFromCursorPos(this.lines2, end2, 0);
			return new AceRange(startRowCol[0], startRowCol[1], endRowCol[0], endRowCol[1]);
		}
		private void calc() {
			this.lines1 = this.s1.split("\n", -1);
			this.lines2 = this.s2.split("\n", -1);
			this.diffs = ClientSideDocDiff.dmp.diff_main(this.s1, this.s2);
		}
	}
}
