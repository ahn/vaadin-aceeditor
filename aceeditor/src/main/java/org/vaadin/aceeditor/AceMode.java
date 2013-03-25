package org.vaadin.aceeditor;

/**
 * Ace mode defines the language used in the editor.
 * 
 */
public enum AceMode {
	abap,
	asciidoc,
	c_cpp,
	c9search,
	clojure,
	coffee,
	coldfusion,
	csharp,
	css,
	curly,
	dart,
	diff,
	django,
	dot,
	glsl,
	golang,
	groovy,
	haml,
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
	lisp,
	livescript,
	lua,
	luapage,
	markdown,
	objectivec,
	ocaml,
	perl,
	pgsql,
	php,
	powershell,
	python,
	r,
	rdoc,
	rhtml,
	ruby,
	scad,
	scala,
	scheme,
	scss,
	sh,
	sql,
	stylus,
	svg,
	tcl,
	tex,
	text,
	textile,
	tm_snippet,
	typescript,
	xml,
	xquery,
	yaml;

	public static AceMode forFile(String filename) {
		int lastDot = filename.lastIndexOf(".");
		if (lastDot == -1) {
			return text;
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
		if (ending.equals("c") || ending.equals("cpp") || ending.equals("cc") ||
				ending.equals("h") || ending.equals("hpp") || ending.equals("hh"))
			return c_cpp;
		if (ending.equals("java"))
			return java;
		if (ending.equals("py"))
			return python;
		if (ending.equals("tex"))
			return latex;
		// TODO: more

		return text;
	}

}
