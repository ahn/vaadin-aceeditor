package org.vaadin.aceeditor;

import java.util.HashMap;

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
	
	private static HashMap<String, AceMode> endingModeMap = new HashMap<String, AceMode>();
	static {
		endingModeMap.put("js", javascript);
		endingModeMap.put("json", json);
		endingModeMap.put("c", c_cpp);
		endingModeMap.put("cpp", c_cpp);
		endingModeMap.put("cc", c_cpp);
		endingModeMap.put("h", c_cpp);
		endingModeMap.put("hpp", c_cpp);
		endingModeMap.put("hh", c_cpp);
		endingModeMap.put("java", java);
		endingModeMap.put("py", python);
		endingModeMap.put("tex", latex);
		endingModeMap.put("css", css);
		// TODO: more
	}

	/**
	 * 
	 * @param ending, example: "js"
	 */
	public static AceMode forFileEnding(String ending) {
		AceMode mode = endingModeMap.get(ending);
		return mode!=null ? mode : text;
	}

}
