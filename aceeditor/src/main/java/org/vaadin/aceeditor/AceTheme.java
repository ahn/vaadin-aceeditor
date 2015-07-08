package org.vaadin.aceeditor;

/**
 * Ace theme defines the appearance of the editor.
 * 
 */
public enum AceTheme {
	ambiance,
	chrome,
	clouds,
	clouds_midnight,
	cobalt,
	crimson_editor,
	dawn, dreamweaver,
	eclipse,
	github,
	idle_fingers,
	katzenmilch,
	kuroir,
	kr,
	merbivore,
	merbivore_soft,
	mono_industrial,
	monokai,
	pastel_on_dark,
	solarized_dark,
	solarized_light,
	terminal,
	textmate, tomorrow,
	tomorrow_night,
	tomorrow_night_blue,
	tomorrow_night_bright,
	tomorrow_night_eighties,
	twilight,
	vibrant_ink,
	xcode;
	
	public String getRequireString() {
		return "ace/theme/"+this.toString();
	}
}
