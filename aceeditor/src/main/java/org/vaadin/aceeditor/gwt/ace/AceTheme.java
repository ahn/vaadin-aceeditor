package org.vaadin.aceeditor.gwt.ace;

/**
 * Ace theme defines the appearance of the editor.
 * 
 * <p>
 * To use a theme, the corresponding theme js file must've been loaded. You can
 * do the loading (among other ways) either by
 * 
 * 1) defining it in a .gwt.xml file, or
 * 
 * 2) giving the script file URL to
 * {@link org.vaadin.codeeditor.AceEditor#setTheme(AceTheme, String)}
 * </p>
 */
public enum AceTheme {
	chrome, clouds, clouds_midnight, cobalt, crimson_editor, dawn, dreamweaver, eclipse, idle_fingers, kr_theme, merbivore, merbivore_soft, mono_industrial, monokai, pastel_on_dark, solarized_dark, solarized_light, textmate, tomorrow, tomorrow_night, tomorrow_night_blue, tomorrow_night_bright, tomorrow_night_eighties, twilight, vibrant_ink;
}
