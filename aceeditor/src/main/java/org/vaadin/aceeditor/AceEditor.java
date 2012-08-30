package org.vaadin.aceeditor;

import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.aceeditor.gwt.ace.AceTheme;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractTextField;

/**
 * Vaadin Ace editor.
 * 
 * @see <a href="http://ace.ajax.org">Ace Editor</a>
 * 
 */
@SuppressWarnings({ "serial", "unchecked" })
@com.vaadin.ui.ClientWidget(org.vaadin.aceeditor.gwt.client.VAceEditor.class)
public class AceEditor extends AbstractTextField {

	private static final String DEFAULT_WIDTH = "400px";
	private static final String DEFAULT_HEIGHT = "300px";

	private AceMode mode = null;
	private AceTheme theme = null;
	private String fontSize = "12px";
	private Boolean hScrollVisible = false;
	private Boolean useWrapMode = false;
	private String modeFileURL;
	private String themeFileURL;

	/**
	 * Initializes the editor with an empty string as content.
	 */
	public AceEditor() {
		super();
		setValue("");
		setWidth(DEFAULT_WIDTH);
		setHeight(DEFAULT_HEIGHT);
	}

	/**
	 * 
	 */
	public AceMode getMode() {
		return mode;
	}

	/**
	 * Sets the Ace mode.
	 * 
	 * <p>
	 * NOTE: The corresponding mode JavaScript file must be loaded. If it's not
	 * already, use {@link #setMode(AceMode, String)} with the file URL.
	 * </p>
	 * 
	 * @param mode
	 */
	public void setMode(AceMode mode) {
		setMode(mode, null);
	}

	/**
	 * Sets the Ace mode after loading it from the given URL (if necessary).
	 * 
	 * @param mode
	 * @param modeFileURL
	 */
	public void setMode(AceMode mode, String modeFileURL) {
		this.mode = mode;
		this.modeFileURL = modeFileURL;
		requestRepaint();
	}

	/**
	 * 
	 */
	public AceTheme getTheme() {
		return theme;
	}

	/**
	 * Sets the Ace theme.
	 * 
	 * <p>
	 * NOTE: The corresponding theme JavaScript file must be loaded. If it's not
	 * already, use {@link #setTheme(AceTheme, String)} with the file URL.
	 * </p>
	 * 
	 * @param theme
	 */
	public void setTheme(AceTheme theme) {
		setTheme(theme, null);
	}

	/**
	 * Sets the Ace theme after loading it from the given URL (if necessary).
	 * 
	 * @param theme
	 * @param themeFileURL
	 */
	public void setTheme(AceTheme theme, String themeFileURL) {
		this.theme = theme;
		this.themeFileURL = themeFileURL;
		requestRepaint();
	}

	/**
	 * 
	 */
	public String getFontSize() {
		return fontSize;
	}

	/**
	 * 
	 * @param fontSize
	 */
	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
		requestRepaint();
	}

	/**
	 * 
	 */
	public boolean gethScrollVisible() {
		return hScrollVisible;
	}

	/**
	 * 
	 * @param hScrollVisible
	 */
	public void sethScrollVisible(Boolean hScrollVisible) {
		this.hScrollVisible = hScrollVisible;
		requestRepaint();
	}
	
	public Boolean getUseWrapMode() {
		return useWrapMode;
	}

	public void setUseWrapMode(Boolean useWrapMode) {
		this.useWrapMode = useWrapMode;
		requestRepaint();
	}

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);
		if (mode != null) {
			target.addAttribute("ace-mode", mode.toString());
		}
		if (modeFileURL != null) {
			target.addAttribute("ace-mode-url", modeFileURL);
		}
		if (theme != null) {
			target.addAttribute("ace-theme", theme.toString());
		}
		if (themeFileURL != null) {
			target.addAttribute("ace-theme-url", themeFileURL);
		}
		if (fontSize != null) {
			target.addAttribute("ace-font-size", fontSize);
		}
		target.addAttribute("ace-hscroll-visible", hScrollVisible);
		target.addAttribute("ace-use-wrapmode", useWrapMode);
	}

}
