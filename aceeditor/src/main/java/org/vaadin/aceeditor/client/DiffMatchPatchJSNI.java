package org.vaadin.aceeditor.client;


import org.vaadin.aceeditor.client.GwtTextDiff.Diff;
import org.vaadin.aceeditor.client.GwtTextDiff.Patch;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class DiffMatchPatchJSNI extends JavaScriptObject {

	protected DiffMatchPatchJSNI() {
	}

	native final static public DiffMatchPatchJSNI newInstance() /*-{
		return new $wnd.diff_match_patch();
	}-*/;

	native final public JsArray<Diff> diff_main(String text1, String text2) /*-{
		return this.diff_main(text1, text2);
	}-*/;

	native final public int match_main(String text, String pattern, int loc) /*-{
		return this.match_main(text, pattern, loc);
	}-*/;

	native final public String patch_apply(JsArray<Patch> patches, String text) /*-{
		return this.patch_apply(patches, text)[0];
	}-*/;

	native final public JsArray<Patch> patch_fromText(String text) /*-{
		return this.patch_fromText(text);
	}-*/;

	native final public String patch_toText(JsArray<Patch> patches) /*-{
		return this.patch_toText(patches);
	}-*/;

	native final public int diff_xIndex(JsArray<Diff> diffs, int pos) /*-{
		return this.diff_xIndex(diffs, pos);
	}-*/;

	native final public int diff_xIndex_patches(JsArray<Patch> patches, int pos) /*-{
		for (var i=0; i<patches.length; i++) {
			pos = this.diff_xIndex(patches[i].diffs, pos);
		}
		return pos;
	}-*/;

	native final public JsArray<Patch> patch_make_diff_main(String text1, String text2) /*-{
		return this.patch_make(text1, this.diff_main(text1,text2));
	}-*/;
	
	native final public JsArray<Patch> patch_make(String text1, String text2) /*-{
		return this.patch_make(text1, text2);
	}-*/;

	native final public void setMatch_Threshold(double d) /*-{
		this.Match_Threshold = d;
	}-*/;
	
	native final public void setPatch_Margin(int m) /*-{
		this.Patch_Margin = m;
	}-*/;
	
	native final public void setMatch_Distance(int m) /*-{
		this.Match_Distance = m;
	}-*/;
	
	native final public void setDiff_EditCost(int c) /*-{
		this.Diff_EditCost = c;
	}-*/;
	
	
}
