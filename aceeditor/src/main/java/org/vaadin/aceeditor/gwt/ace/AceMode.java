package org.vaadin.aceeditor.gwt.ace;

/**
 * Ace mode defines the language used in the editor.
 * 
 */
public enum AceMode {
	ascii_doc,
	c_cpp,
	c9search,
	clojure,
	coffee,
	coldfusion,
	csharp,
	css,
	diff,
	glsl,
	golang,
	groovy,
	haxe,
	html,
	jade,
	java,
	javascript,
	json,
	jsp,
	jsx,
	latex,
	less,
	liquid,
	lua,
	luapage,
	markdown,
	ocaml,
	perl,
	pgsql,
	php,
	powershell,
	python,
	ruby,
	scad,
	scala,
	scss,
	sh,
	sql,
	svg,
	tcl,
	text,
	textile,
	typescript,
	xml,
	xquery,
	yaml;

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
