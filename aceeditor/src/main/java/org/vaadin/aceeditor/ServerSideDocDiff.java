package org.vaadin.aceeditor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.aceeditor.client.AceAnnotation.MarkerAnnotation;
import org.vaadin.aceeditor.client.AceAnnotation.RowAnnotation;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.MarkerSetDiff;
import org.vaadin.aceeditor.client.SetDiff;
import org.vaadin.aceeditor.client.TransportDiff;
import org.vaadin.aceeditor.client.TransportDiff.TransportSetDiffForMarkerAnnotations;
import org.vaadin.aceeditor.client.TransportDiff.TransportSetDiffForRowAnnotations;
import org.vaadin.aceeditor.client.TransportDoc.TransportMarkerAnnotation;
import org.vaadin.aceeditor.client.TransportDoc.TransportRowAnnotation;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;




public class ServerSideDocDiff {

	// We could use ThreadLocal but that causes a (valid) complaint
	// of memory leak by Tomcat. Creating a new diff_match_patch every
	// time (in getDmp()) fixes that. The creation is not a heavy operation so this it's ok.
	/*
	private static final ThreadLocal <diff_match_patch> dmp =
	         new ThreadLocal <diff_match_patch> () {
	             @Override protected diff_match_patch initialValue() {
	                 return new diff_match_patch();
	         }
	     };
	 */

	private static diff_match_patch getDmp() {
		return new diff_match_patch();
	}

	private final LinkedList<Patch> patches;
	private final MarkerSetDiff markerSetDiff;
	private final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff;
	private final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff;

	public static ServerSideDocDiff diff(final AceDoc doc1, final AceDoc doc2) {
		final LinkedList<Patch> patches = ServerSideDocDiff.getDmp().patch_make(doc1.getText(), doc2.getText());
		final MarkerSetDiff msd = MarkerSetDiff.diff(doc1.getMarkers(), doc2.getMarkers(), doc2.getText());
		final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff =
				ServerSideDocDiff.diffRA(doc1.getRowAnnotations(), doc2.getRowAnnotations());
		final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff =
				ServerSideDocDiff.diffMA(doc1.getMarkerAnnotations(), doc2.getMarkerAnnotations());
		return new ServerSideDocDiff(patches, msd, rowAnnDiff, markerAnnDiff);
	}

	public static ServerSideDocDiff diff(final String text1, final String text2) {
		final LinkedList<Patch> patches = ServerSideDocDiff.getDmp().patch_make(text1, text2);
		return new ServerSideDocDiff(patches);
	}


	// XXX Unnecessary copy-pasting
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

	// XXX Unnecessary copy-pasting
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


	public static ServerSideDocDiff fromTransportDiff(final TransportDiff diff) {
		return new ServerSideDocDiff(
				(LinkedList<Patch>) ServerSideDocDiff.getDmp().patch_fromText(diff.patchesAsString),
				MarkerSetDiff.fromTransportDiff(diff.markerSetDiff),
				ServerSideDocDiff.rowAnnsFromTransport(diff.rowAnnDiff),
				ServerSideDocDiff.markerAnnsFromTransport(diff.markerAnnDiff));
	}

	// XXX Unnecessary copy-pasting
	private static SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnsFromTransport(
			final TransportSetDiffForRowAnnotations rowAnnDiff) {
		return rowAnnDiff==null ? null : SetDiff.fromTransport(rowAnnDiff);
	}

	// XXX Unnecessary copy-pasting
	private static SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnsFromTransport(
			final TransportSetDiffForMarkerAnnotations markerAnnDiff) {
		return markerAnnDiff==null ? null :  SetDiff.fromTransport(markerAnnDiff);
	}

	public ServerSideDocDiff(final LinkedList<Patch> patches, final MarkerSetDiff markerSetDiff,
			final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff,
			final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> markerAnnDiff) {
		this.patches = patches;
		this.markerSetDiff = markerSetDiff;
		this.rowAnnDiff = rowAnnDiff;
		this.markerAnnDiff = markerAnnDiff;
	}

	public ServerSideDocDiff(final LinkedList<Patch> patches) {
		this(patches, null, null, null);
	}

	public String getPatchesString() {
		return ServerSideDocDiff.getDmp().patch_toText(this.patches);
	}

	public List<Patch> getPatches() {
		return Collections.unmodifiableList(this.patches);
	}


	public AceDoc applyTo(final AceDoc doc) {
		final String text = (String)ServerSideDocDiff.getDmp().patch_apply(this.patches, doc.getText())[0];
		final Map<String, AceMarker> markers = this.markerSetDiff==null ? doc.getMarkers() : this.markerSetDiff.applyTo(doc.getMarkers(), text);
		final Set<RowAnnotation> rowAnns = this.rowAnnDiff==null ? null : this.rowAnnDiff.applyTo(doc.getRowAnnotations());
		final Set<MarkerAnnotation> markerAnns = this.markerAnnDiff==null ? null : this.markerAnnDiff.applyTo(doc.getMarkerAnnotations());
		return new AceDoc(text, markers, rowAnns, markerAnns);
	}

	public String applyTo(final String text) {
		return (String)ServerSideDocDiff.getDmp().patch_apply(this.patches, text)[0];
	}

	public TransportDiff asTransport() {
		final TransportDiff d = new TransportDiff();
		d.patchesAsString = this.getPatchesString();
		d.markerSetDiff = this.markerSetDiff==null ? null : this.markerSetDiff.asTransportDiff();
		d.rowAnnDiff = this.rowAnnDiff==null ? null : this.rowAnnDiff.asTransportRowAnnotations();
		d.markerAnnDiff = this.markerAnnDiff==null ? null : this.markerAnnDiff.asTransportMarkerAnnotations();
		return d;
	}

	public boolean isIdentity() {
		return this.patches.isEmpty() && (this.markerSetDiff==null || this.markerSetDiff.isIdentity()); // TODO?
	}

	@Override
	public String toString() {
		return "---ServerSideDocDiff---\n" + this.getPatchesString()+"\n"+this.markerSetDiff+"\nrad:"+this.rowAnnDiff+", mad:"+this.markerAnnDiff;
	}


	public static ServerSideDocDiff newMarkersAndAnnotations(
			final MarkerSetDiff msd, final SetDiff<MarkerAnnotation,TransportMarkerAnnotation> mad) {
		final LinkedList<Patch> patches = new LinkedList<>();
		final SetDiff<RowAnnotation,TransportRowAnnotation> rowAnnDiff =
				new SetDiff<>();
		return new ServerSideDocDiff(patches, msd, rowAnnDiff, mad);
	}

}
