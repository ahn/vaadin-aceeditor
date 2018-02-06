package org.vaadin.aceeditor;
import java.util.LinkedList;
import java.util.List;



public class MySuggester implements Suggester {

	// We can use either Suggestion class directly or its subclass as in here.
	// With subclass we can set additional data to the class that's helpful
	// when applying the suggestion.
	private static class MySuggestion extends Suggestion {
		private final String insertThis;
		MySuggestion(final String displayText, final String descriptionText, final String suggestionText) {
			super("", displayText, descriptionText, suggestionText);
			this.insertThis = suggestionText;
		}
	}

	@Override
	public List<Suggestion> getSuggestions(final String text, final int cursor) {
		final LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();
		for (int i=1; i<=20 && i<=cursor; i++) {
			final String w = text.substring(cursor-i,cursor);
			final String descr = "<strong>"+w+"</strong><br />Repeat "+i+" previous characters ("+w+")";
			suggs.add(new MySuggestion(w, descr, w));
		}
		return suggs;
	}

	@Override
	public String applySuggestion(final Suggestion sugg, final String text, final int cursor) {
		// sugg is one of the objects returned by getSuggestions -> it's a MySuggestion.
		final String ins = ((MySuggestion)sugg).insertThis;
		final String s1 = text.substring(0,cursor) + ins + text.substring(cursor);
		return s1;
	}

}
