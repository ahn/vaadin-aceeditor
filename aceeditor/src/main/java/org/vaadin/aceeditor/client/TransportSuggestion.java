package org.vaadin.aceeditor.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TransportSuggestion implements Serializable {
	public String group;
	public Boolean disabled;
	public String displayText;
	public String descriptionText;
	public String suggestionText;
	public Integer selectionStart;
	public Integer selectionEnd;
	public int index;
}
