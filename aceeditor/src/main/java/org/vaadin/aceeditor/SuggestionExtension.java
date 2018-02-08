package org.vaadin.aceeditor;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.SuggesterClientRpc;
import org.vaadin.aceeditor.client.SuggesterServerRpc;
import org.vaadin.aceeditor.client.SuggesterState;
import org.vaadin.aceeditor.client.TransportDiff;
import org.vaadin.aceeditor.client.TransportDoc.TransportRange;
import org.vaadin.aceeditor.client.TransportSuggestion;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.AbstractExtension;

/**
 * Extends {@link AceEditor} with suggestion possibility.
 *
 * By default Ctrl+Space and dot (".") triggers a suggester.
 *
 * A {@link Suggester} is queried for {@link Suggestion}s.
 *
 */
@StyleSheet("suggestionpopup.css")
@SuppressWarnings("serial")
public class SuggestionExtension extends AbstractExtension {

	protected Suggester suggester;

	protected String suggStartText;
	protected int suggStartCursor;
	protected List<Suggestion> suggestions;
	protected AceRange suggRange;

	public SuggestionExtension(final Suggester suggester) {
		this.suggester = suggester;
	}

	protected SuggesterServerRpc serverRpc = new SuggesterServerRpc() {

		@Override
		public void suggest(final String text, final TransportRange sel) {
			SuggestionExtension.this.suggStartText = text;
			SuggestionExtension.this.suggRange = AceRange.fromTransport(sel);
			SuggestionExtension.this.suggStartCursor = new TextRange(text, SuggestionExtension.this.suggRange).getEnd();
			SuggestionExtension.this.suggestions = SuggestionExtension.this.suggester.getSuggestions(text, SuggestionExtension.this.suggStartCursor);
			SuggestionExtension.this.getRpcProxy(SuggesterClientRpc.class).showSuggestions(
					SuggestionExtension.this.asTransport(SuggestionExtension.this.suggestions));
		}

		@Override
		public void suggestionSelected(final int index) {

			final Suggestion sugg = SuggestionExtension.this.suggestions.get(index);
			final String text2 = SuggestionExtension.this.suggester.applySuggestion(sugg, SuggestionExtension.this.suggStartText,
					SuggestionExtension.this.suggStartCursor);
			// XXX too much work
			final AceDoc doc1 = new AceDoc(SuggestionExtension.this.suggStartText);
			final AceDoc doc2 = new AceDoc(text2);
			final ServerSideDocDiff diff = ServerSideDocDiff.diff(doc1, doc2);
			final TransportDiff asTransport = diff.asTransport();
			if (sugg.getSelectionStart() != null) {
				asTransport.selectionStart = SuggestionExtension.this.suggStartCursor + sugg.getSelectionStart();
				asTransport.selectionEnd = SuggestionExtension.this.suggStartCursor + sugg.getSelectionEnd();
			}
			SuggestionExtension.this.getRpcProxy(SuggesterClientRpc.class).applySuggestionDiff(
					asTransport);
		}
	};

	@Override
	public SuggesterState getState() {
		return (SuggesterState) super.getState();
	}

	@Override
	protected SuggesterState getState(final boolean markAsDirty) {
		return (SuggesterState) super.getState(markAsDirty);
	}

	protected List<TransportSuggestion> asTransport(final List<Suggestion> suggs) {
		final LinkedList<TransportSuggestion> tl = new LinkedList<>();
		int i = 0;
		for (final Suggestion s : suggs) {
			tl.add(s.asTransport(i++));
		}
		return tl;
	}

	public Suggester getSuggester() {
		return this.suggester;
	}

	public void setSuggestOnDot(final boolean on) {
		this.getState().suggestOnDot = on;
	}

	public void extend(final AceEditor editor) {
		super.extend(editor);
		this.registerRpc(this.serverRpc);
	}

	public void setShowDescriptions(final boolean showDescriptions) {
		this.getState().showDescriptions = showDescriptions;
	}

	public boolean isShowDescriptions() {
		return this.getState(false).showDescriptions;
	}

	public void setPopupWidth(final int width){
		this.getState().popupWidth = width;
	}

	public void setPopupHeight(final int height){
		this.getState().popupHeight = height;
	}

	public void setpopupDescriptionWidth(final int width){
		this.getState().popupDescriptionWidth = width;
	}
}