package org.vaadin.aceeditor;
import java.util.LinkedList;
import java.util.List;



public class MySuggester implements Suggester {
	
	private static class MySuggestion extends Suggestion {
		private String insertThis;
		MySuggestion(String displayText, String descriptionText, String suggestionText) {
			super(displayText, descriptionText, suggestionText);
			insertThis = suggestionText;
		}
	}

	@Override
	public List<Suggestion> getSuggestions(String text, int cursor) {
		LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();
		for (int i=1; i<=20 && i<=cursor; i++) {	
			String w = text.substring(cursor-i,cursor);
			String descr = "<strong>"+w+"</strong><br />Repeat "+i+" previous characters ("+w+")";
			suggs.add(new MySuggestion(w, descr, w));
		}
		return suggs;
	}

	@Override
	public String applySuggestion(Suggestion sugg, String text, int cursor) {
		String ins = ((MySuggestion)sugg).insertThis;
		String s1 = text.substring(0,cursor) + ins + text.substring(cursor);
		System.out.println(s1);
		return s1;
	}

}
