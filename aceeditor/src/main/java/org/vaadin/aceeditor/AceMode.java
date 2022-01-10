package org.vaadin.aceeditor;

import java.util.HashMap;

/**
 * Ace mode defines the language used in the editor.
 *
 */
public enum AceMode {
	abap,
	abc,
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
	elixir,
	elm,
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
	lean,
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
	mips_assembler,
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

	public static AceMode forFile(final String filename) {
		final int lastDot = filename.lastIndexOf(".");
		if (lastDot == -1) {
			return text;
		}
		return AceMode.forFileEnding(filename.substring(lastDot + 1).toLowerCase());
	}

	private static HashMap<String, AceMode> endingModeMap = new HashMap<>();
	static {
		// Only the file endings that != language name
		AceMode.endingModeMap.put("js", javascript);
		AceMode.endingModeMap.put("c", c_cpp);
		AceMode.endingModeMap.put("cpp", c_cpp);
		AceMode.endingModeMap.put("cc", c_cpp);
		AceMode.endingModeMap.put("h", c_cpp);
		AceMode.endingModeMap.put("hpp", c_cpp);
		AceMode.endingModeMap.put("hh", c_cpp);
		AceMode.endingModeMap.put("py", python);
		AceMode.endingModeMap.put("tex", latex);
		AceMode.endingModeMap.put("txt", text);
		AceMode.endingModeMap.put("htm", html);
		AceMode.endingModeMap.put("hs", haskell);
		// TODO: more
	}

	/**
	 *
	 * @param ending, example: "js"
	 */
	public static AceMode forFileEnding(final String ending) {
		try {
			return AceMode.valueOf(ending);
		}
		catch(final IllegalArgumentException e) {
			final AceMode mode = AceMode.endingModeMap.get(ending);
			return mode!=null ? mode : text;
		}
	}

}
