package org.vaadin.aceeditor;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.SuggesterClientRpc;
import org.vaadin.aceeditor.client.SuggesterServerRpc;
import org.vaadin.aceeditor.client.SuggesterState;
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

	private Suggester suggester;

	private String suggStartText;
	private int suggStartCursor;
	private List<Suggestion> suggestions;
	private AceRange suggRange;

	public SuggestionExtension(Suggester suggester) {
		super();
		this.suggester = suggester;
	}

	private SuggesterServerRpc serverRpc = new SuggesterServerRpc() {

		@Override
		public void suggest(String text, TransportRange sel) {
			suggStartText = text;
			suggRange = AceRange.fromTransport(sel);
			suggStartCursor = new TextRange(text, suggRange).getEnd();
			suggestions = suggester.getSuggestions(text, suggStartCursor);
			getRpcProxy(SuggesterClientRpc.class).showSuggestions(
					asTransport(suggestions));
		}

		@Override
		public void suggestionSelected(int index) {

			Suggestion sugg = suggestions.get(index);
			String text2 = suggester.applySuggestion(sugg, suggStartText,
					suggStartCursor);
			// XXX too much work
			AceDoc doc1 = new AceDoc(suggStartText);
			AceDoc doc2 = new AceDoc(text2);
			ServerSideDocDiff diff = ServerSideDocDiff.diff(doc1, doc2);
			getRpcProxy(SuggesterClientRpc.class).applySuggestionDiff(
					diff.asTransport());
		}
	};

	@Override
	public SuggesterState getState() {
		return (SuggesterState) super.getState();
	}

	private List<TransportSuggestion> asTransport(List<Suggestion> suggs) {
		LinkedList<TransportSuggestion> tl = new LinkedList<TransportSuggestion>();
		int i = 0;
		for (Suggestion s : suggs) {
			tl.add(s.asTransport(i++));
		}
		return tl;
	}

	public void setSuggestOnDot(boolean on) {
		getState().suggestOnDot = on;
	}

	public void extend(AceEditor editor) {
		super.extend(editor);
		registerRpc(serverRpc);
	}

}
