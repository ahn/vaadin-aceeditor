package org.vaadin.aceeditor.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class GwtTextDiff{

	public static final int DIFF_DELETE = -1;
	public static final int DIFF_INSERT = 1;
	public static final int DIFF_EQUAL = 0;

	public static final class Patch extends JavaScriptObject {

		protected Patch() {

		}

		native int getStart1() /*-{
			return this.start1;
		}-*/;

		native public JsArray<Diff> getDiffsJsArray() /*-{
			return this.diffs;
		}-*/;

		public List<Diff> getDiffs() {
			final JsArray<Diff> diffs = this.getDiffsJsArray();
			final LinkedList<Diff> dili = new LinkedList<>();
			for (int i=0; i<diffs.length(); ++i) {
				dili.add(diffs.get(i));
			}
			return dili;
		}
	}

	public static final class Diff extends JavaScriptObject {

		protected Diff() {

		}

		public native int getOperation() /*-{
			return this[0];
		}-*/;

		public native String getText() /*-{
			return this[1];
		}-*/;
	}


	private static final DiffMatchPatchJSNI dmp = DiffMatchPatchJSNI
			.newInstance();

	private final JsArray<Patch> patches;

	public static GwtTextDiff diff(final String v1, final String v2) {
		return new GwtTextDiff(v1, v2);
	}

	private static GwtTextDiff fromPatches(final JsArray<Patch> patches) {
		return new GwtTextDiff(patches);
	}

	private GwtTextDiff(final JsArray<Patch> patches) {
		this.patches = patches;
	}

	private GwtTextDiff(final String v1, final String v2) {
		this.patches = GwtTextDiff.dmp.patch_make_diff_main(v1, v2);
	}

	public String applyTo(final String value) {
		return GwtTextDiff.dmp.patch_apply(this.patches, value);
	}

	public boolean isIdentity() {
		return this.patches.length() == 0;
	}

	public String getDiffString() {
		return GwtTextDiff.getDMP().patch_toText(this.patches);
	}

	public static int positionInNewText(final String text1, final int cursorPos,
			final String text2) {
		// TODO: calculating the difference every time is a bit slow
		return GwtTextDiff.dmp.diff_xIndex(GwtTextDiff.dmp.diff_main(text1, text2), cursorPos);
	}

	public static DiffMatchPatchJSNI getDMP() {
		return GwtTextDiff.dmp;
	}

	public static GwtTextDiff fromString(final String s) {
		return GwtTextDiff.fromPatches(GwtTextDiff.dmp.patch_fromText(s));
	}

	public int adjustPosition(final int pos) {
		return GwtTextDiff.dmp.diff_xIndex_patches(this.patches, pos);
	}

	public List<Patch> getPatches() {
		final LinkedList<Patch> pali = new LinkedList<>();
		for (int i=0; i<this.patches.length(); ++i) {
			pali.add(this.patches.get(i));
		}
		return pali;
	}



}
