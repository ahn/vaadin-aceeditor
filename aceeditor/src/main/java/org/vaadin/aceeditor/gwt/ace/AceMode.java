package org.vaadin.aceeditor.gwt.ace;

/**
 * Ace mode defines the language used in the editor.
 * 
 * <p>
 * To use a mode, the corresponding mode js file must've been loaded. You can do
 * the loading (among other ways) either by
 * 
 * 1) defining it in a .gwt.xml file, or
 * 
 * 2) giving the script file URL to
 * {@link org.vaadin.codeeditor.AceEditor#setMode(AceMode, String)}
 * </p>
 */
public enum AceMode {
	c_cpp, clojure, coffee, coldfusion, csharp, css, groovy, haxe, html, java, javascript, json, latex, lua, markdown, ocaml, perl, pgsql, php, powershell, python, ruby, scad, scala, scss, sql, svg, textile, xml;

	public static AceMode forFile(String filename) {
		int lastDot = filename.lastIndexOf(".");
		if (lastDot == -1) {
			return null;
		}
		return forFileEnding(filename.substring(lastDot + 1).toLowerCase());
	}

	public static AceMode forFileEnding(String ending) {
		if (ending.equals("js")) {
			return javascript;
		}
		if (ending.equals("json")) {
			return json;
		}
		if (ending.equals("c") || ending.equals("cpp") || ending.equals("h")
				|| ending.equals("hpp"))
			return c_cpp;
		if (ending.equals("java"))
			return java;
		if (ending.equals("py"))
			return python;
		if (ending.equals("tex"))
			return latex;
		// TODO: more

		return null;
	}

}
