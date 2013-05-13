package org.vaadin.aceeditor.client;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.GwtTextDiff.Diff;
import org.vaadin.aceeditor.client.GwtTextDiff.Patch;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRange;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;

import com.google.gwt.core.client.JsArray;



public class ClientDiff {

	// TODO???

	public static final DiffMatchPatchJSNI dmp = DiffMatchPatchJSNI.newInstance();

	private final JsArray<GwtTextDiff.Patch> textPatches;
	private final MarkerSetDiff markerSetDiff;
	private final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff;
	private final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff;

	public static ClientDiff fromTransportDiff(TransportDiff ad) {
		
		JsArray<Patch> patches = dmp.patch_fromText(ad.patchesAsString);
		MarkerSetDiff msd = MarkerSetDiff.fromTransportDiff(ad.markerSetDiff);
		
		SetDiff<RowAnnotation,TransportRowAnnotation> rowAnns = ad.rowAnnDiff==null ? null : 
				SetDiff.fromTransport(ad.rowAnnDiff);
		SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnns =  ad.markerAnnDiff==null ? null : 
				SetDiff.fromTransport(ad.markerAnnDiff);
		
		return new ClientDiff(patches, msd, rowAnns, markerAnns);
	}
	
	public static ClientDiff diff(AceDoc doc1, AceDoc doc2) {
		JsArray<GwtTextDiff.Patch> patches = dmp.patch_make(doc1.getText(), doc2.getText());
		MarkerSetDiff msd = MarkerSetDiff.diff(doc1.getMarkers(), doc2.getMarkers(), doc2.getText());

		SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff = diffRA(doc1.getRowAnnotations(), doc2.getRowAnnotations());		
		SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff = diffMA(doc1.getMarkerAnnotations(), doc2.getMarkerAnnotations());
		
		return new ClientDiff(patches, msd, rowAnnDiff, markerAnnDiff);
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
	
	private ClientDiff(JsArray<GwtTextDiff.Patch> patches, MarkerSetDiff markerSetDiff,
			SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff,
			SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff) {
		this.textPatches = patches;
		this.markerSetDiff = markerSetDiff;
		this.rowAnnDiff = rowAnnDiff;
		this.markerAnnDiff = markerAnnDiff;
	}
	
	public String getPatchesString() {
		return dmp.patch_toText(textPatches);
	}
	
	public AceDoc applyTo(AceDoc doc) {
		String text = dmp.patch_apply(textPatches, doc.getText());
		Map<String, AceMarker> markers = markerSetDiff.applyTo(doc.getMarkers(), text);
		
		Set<RowAnnotation> rowAnns = rowAnnDiff==null ? null : rowAnnDiff.applyTo(doc.getRowAnnotations());
		Set<MarkerAnnotation> markerAnns = markerAnnDiff==null ? null : markerAnnDiff.applyTo(doc.getMarkerAnnotations());
		
		return new AceDoc(text, markers, rowAnns, markerAnns);
	}

	public TransportDiff asTransport() {
		TransportDiff d = new TransportDiff();
		d.patchesAsString = getPatchesString();
		d.markerSetDiff = markerSetDiff.asTransportDiff();
		d.rowAnnDiff = rowAnnDiff==null ? null : rowAnnDiff.asTransportRowAnnotations();
		d.markerAnnDiff = markerAnnDiff==null ? null : markerAnnDiff.asTransportMarkerAnnotations();
		return d;
	}

	public boolean isIdentity() {
		return textPatches == null || textPatches.length()==0;
	}
	
	@Override
	public String toString() {
		return getPatchesString() + "\nMSD: " + markerSetDiff.toString() + "\nrad:" + rowAnnDiff + ", mad:" + markerAnnDiff;
	}
	
	public static TransportRange adjustSelection(TransportRange sel, String text1, String text2) {
		// This is a bit stupid to transform back and forth but diff_match_patch doesn't deal with lines...
		if (text1.equals(text2)) {
			return sel;
		}
		String[] lines1 = text1.split("\n", -1);
		String[] lines2 = text2.split("\n", -1);
		boolean zeroLength = sel.row1==sel.row2 && sel.col1==sel.col2;
		int start1 = Util.cursorPosFromLineCol(lines1, sel.row1, sel.col1, 0);
		int end1 = zeroLength ? start1 : Util.cursorPosFromLineCol(lines1, sel.row2, sel.col2, 0);
		JsArray<Diff> diffs = dmp.diff_main(text1, text2);
		int start2 = dmp.diff_xIndex(diffs, start1);
		int end2 = zeroLength ? start2 : dmp.diff_xIndex(diffs, end1);
		int[] startRowCol = Util.lineColFromCursorPos(lines2, start2, 0);
		int[] endRowCol = zeroLength ? startRowCol : Util.lineColFromCursorPos(lines2, end2, 0);
		return new TransportRange(startRowCol[0], startRowCol[1], endRowCol[0], endRowCol[1]);
	}
	
//	public static AceMarker adjustMarkerBasedOnContext(MarkerAddition ma,
//			String text) {
//		int start = GwtTextDiff.getDMP().match_main(text, ma., ma.startPos);
//		if (start == -1) {
//			return null;
//		}
//		int end = GwtTextDiff.getDMP().match_main(text, ma.endContext, ma.endPos);
//		if (end == -1) {
//			return null;
//		}
//		return null;
//	}
}
