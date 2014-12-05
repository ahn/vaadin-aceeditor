package org.vaadin.aceeditor;

import java.util.HashMap;

/**
 * Ace mode defines the language used in the editor.
 * 
 */
public enum AceMode {
	abap,
	actionscript,
	ada,
	apache_conf,
	asciidoc,
	assembly_x86,
	autohotkey,
	batchfile,
	c9search,
	clojure,
	cobol,
	coffee,
	coldfusion,
	csharp,
	css,
	curly,
	c_cpp,
	d,
	dart,
	diff,
	django,
	dot,
	ejs,
	erlang,
	forth,
	ftl,
	glsl,
	golang,
	groovy,
	haml,
	handlebars,
	haskell,
	haxe,
	html,
	html_completions,
	html_ruby,
	ini,
	jack,
	jade,
	java,
	javascript,
	json,
	jsoniq,
	jsp,
	jsx,
	julia,
	latex,
	less,
	liquid,
	lisp,
	livescript,
	logiql,
	lsl,
	lua,
	luapage,
	lucene,
	makefile,
	markdown,
	matlab,
	mel,
	mushcode,
	mushcode_high_rules,
	mysql,
	nix,
	objectivec,
	ocaml,
	pascal,
	perl,
	pgsql,
	php,
	plain_text,
	powershell,
	prolog,
	properties,
	protobuf,
	python,
	r,
	rdoc,
	rhtml,
	ruby,
	rust,
	sass,
	scad,
	scala,
	scheme,
	scss,
	sh,
	sjs,
	snippets,
	soy_template,
	space,
	sql,
	stylus,
	svg,
	tcl,
	tex,
	text,
	textile,
	toml,
	twig,
	typescript,
	vbscript,
	velocity,
	verilog,
	vhdl,
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
		// Only the file endings that != language name
		endingModeMap.put("js", javascript);
		endingModeMap.put("c", c_cpp);
		endingModeMap.put("cpp", c_cpp);
		endingModeMap.put("cc", c_cpp);
		endingModeMap.put("h", c_cpp);
		endingModeMap.put("hpp", c_cpp);
		endingModeMap.put("hh", c_cpp);
		endingModeMap.put("py", python);
		endingModeMap.put("tex", latex);
		endingModeMap.put("txt", text);
		endingModeMap.put("htm", html);
		endingModeMap.put("hs", haskell);
		// TODO: more
	}

	/**
	 * 
	 * @param ending, example: "js"
	 */
	public static AceMode forFileEnding(String ending) {
		try {
			return AceMode.valueOf(ending);
		}
		catch(IllegalArgumentException e) {
			AceMode mode = endingModeMap.get(ending);
			return mode!=null ? mode : text;
		}
	}

}
