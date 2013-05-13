package org.vaadin.aceeditor;

import java.util.List;

import org.vaadin.aceeditor.client.AceRange;
import org.vaadin.aceeditor.client.SuggesterClientRpc;
import org.vaadin.aceeditor.client.SuggesterServerRpc;
import org.vaadin.aceeditor.client.Suggestion;
import org.vaadin.aceeditor.client.TransportDoc.TransportRange;

import com.vaadin.server.AbstractExtension;

@SuppressWarnings("serial")
public class AceSuggestionExtension extends AbstractExtension {
	
	private Suggester suggester;

	public AceSuggestionExtension(Suggester suggester) {
		super();
		this.suggester = suggester;
	}
	
	private SuggesterServerRpc serverRpc = new SuggesterServerRpc() {
		@Override
		public void suggest(String text, TransportRange sel) {
			getRpcProxy(SuggesterClientRpc.class).showSuggestions(getSuggestions(text,AceRange.fromTransport(sel)));
		}

		@Override
		public void suggestionSelected(Suggestion s) {
			System.out.println("suggestionSelected " + s);
		}
	};
	
	public void extend(AceEditor editor) {
        super.extend(editor);
        registerRpc(serverRpc);
    }

	private List<Suggestion> getSuggestions(String text, AceRange sel) {
		return suggester.getSuggestions(text, sel);
	}
	
	
}
