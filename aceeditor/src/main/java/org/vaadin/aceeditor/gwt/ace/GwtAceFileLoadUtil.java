package org.vaadin.aceeditor.gwt.ace;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Helper for loading Ace {@link AceMode modes} and {@link AceTheme themes}.
 * 
 */
public class GwtAceFileLoadUtil {

	public interface LoadListener {
		/**
		 * This is called when the load is complete, whether successful or not.
		 * 
		 * It's the listeners job to check if the loading was successful.
		 */
		public void loadComplete();
	}

	/**
	 * Tries to load script from the given URL. The listener is called when
	 * done.
	 * 
	 * @param url
	 * @param lis
	 */
	public static final native void loadScript(String url, LoadListener lis) /*-{
		var script = $doc.createElement("script")
		script.type = "text/javascript";
		
		if (script.readyState){  //IE
			script.onreadystatechange = function() {
				if (script.readyState == "loaded" || script.readyState == "complete"){
					script.onreadystatechange = null;
					lis.@org.vaadin.aceeditor.gwt.ace.GwtAceFileLoadUtil.LoadListener::loadComplete()();
				}
			};
		} else {  //Others
			script.onload = function(){
				lis.@org.vaadin.aceeditor.gwt.ace.GwtAceFileLoadUtil.LoadListener::loadComplete()();
			};
			script.onerror = function() {
				lis.@org.vaadin.aceeditor.gwt.ace.GwtAceFileLoadUtil.LoadListener::loadComplete()();
			};
		}
		
		script.src = url;
		$doc.getElementsByTagName("head")[0].appendChild(script);
	}-*/;

	public static final JavaScriptObject getObject(AceMode mode) {
		return getObject(mode.toString());
	}

	public static final boolean isAvailable(AceMode mode) {
		return isModeAvailable(mode.toString());
	}

	private static native JavaScriptObject getObject(String mode) /*-{
		var modePackage = $wnd.require("ace/mode/"+mode);
		if (!modePackage) {
			return null;
		}
		return new modePackage.Mode();
	}-*/;

	private static native boolean isModeAvailable(String mode) /*-{
		return !!($wnd.require("ace/mode/"+mode));
	}-*/;

	public static final String getThemeString(AceTheme theme) {
		return "ace/theme/" + theme.toString();
	}

	public static final boolean isAvailable(AceTheme theme) {
		return isThemeAvailable(getThemeString(theme));
	}

	private static final native boolean isThemeAvailable(String themeString) /*-{
		return !!($wnd.require(themeString));
	}-*/;

	public static final List<AceMode> getAvailableModes() {
		LinkedList<AceMode> ams = new LinkedList<AceMode>();
		for (AceMode mode : AceMode.values()) {
			if (isAvailable(mode)) {
				ams.add(mode);
			}
		}
		return ams;
	}

	public static final List<AceTheme> getAvailableThemes() {
		LinkedList<AceTheme> ats = new LinkedList<AceTheme>();
		for (AceTheme theme : AceTheme.values()) {
			if (isAvailable(theme)) {
				ats.add(theme);
			}
		}
		return ats;
	}

}
