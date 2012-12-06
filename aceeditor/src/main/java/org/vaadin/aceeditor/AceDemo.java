package org.vaadin.aceeditor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vaadin.aceeditor.gwt.ace.AceMode;
import org.vaadin.aceeditor.gwt.ace.AceTheme;
import org.vaadin.aceeditor.gwt.shared.Marker;
import org.vaadin.aceeditor.gwt.shared.Suggestion;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Demonstrates the functionality of
 * {@link org.vaadin.codeeditor.AceSuggestibleEditor}.
 * 
 */
@SuppressWarnings("serial")
public class AceDemo extends Application {
	
	private AceSuggestibleEditor ace = new AceSuggestibleEditor();

	private static final Pattern wtfLolPlz = Pattern.compile("WTF|LOL|PLZ",
			Pattern.CASE_INSENSITIVE);

	private static class MyErrorChecker implements ErrorChecker {
		/* @Override */
		public Collection<Marker> getErrors(String source) {
			Matcher mr = wtfLolPlz.matcher(source);
			LinkedList<Marker> errors = new LinkedList<Marker>();
			int i = 0;
			while (true) {
				if (mr.find(i)) {
					errors.add(Marker.newErrorMarker(mr.start(), mr.end(), "'"
							+ mr.group() + "' is a banned word!"));
					i = mr.end();
				} else {
					break;
				}
			}
			return errors;
		}
	}

	private static class MySuggester implements Suggester {
		/* @Override */
		public List<Suggestion> getSuggestions(String text, int cursor) {
			LinkedList<Suggestion> suggs = new LinkedList<Suggestion>();
			suggs.add(new Suggestion("/* Hi! */"));
			suggs.add(new Suggestion("/* Moi! */"));
			suggs.add(new Suggestion("/* Hello! */"));
			return suggs;
		}
	}

	ErrorCheckListener ecl = new ErrorCheckListener(ace, new MyErrorChecker());

	private Window mainWindow = new Window("Ace Demo");

	protected boolean listening = false;

	@Override
	public void init() {

		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();

		mainWindow.addComponent(mainLayout);
		mainWindow.getContent().setSizeFull();

		mainLayout.addComponent(createSideBar());

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		mainLayout.addComponent(layout);
		mainLayout.setExpandRatio(layout, 1);

		ace.setSizeFull();

		ace.setValue("var x;\nvar y = 'LOL!';\n//This is a long line, a very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very long line to be wrapped!\n");

		ShortcutAction suggestAction = new ShortcutAction("Suggest",
				ShortcutAction.KeyCode.SPACEBAR,
				new int[] { ShortcutAction.ModifierKey.CTRL });

		ace.setSuggester(new MySuggester(), suggestAction);

		layout.addComponent(ace);
		setMainWindow(mainWindow);
	}

	private Component createSideBar() {
		VerticalLayout sideBar = new VerticalLayout();
		sideBar.setWidth("200px");

		sideBar.addComponent(createGetSet());
		sideBar.addComponent(createGetCursor());
		sideBar.addComponent(createSelectAll());
		sideBar.addComponent(createImmediate());
		sideBar.addComponent(createReadonly());
		sideBar.addComponent(createSearch());
		sideBar.addComponent(createLockLine());
		sideBar.addComponent(createClearMarkers());
		sideBar.addComponent(createCheckErrors());
		sideBar.addComponent(createSelectTCEM());
		sideBar.addComponent(createSelectTCT());
		sideBar.addComponent(createSelectMode());
		sideBar.addComponent(createSelectTheme());
		sideBar.addComponent(createSelectFontSize());
		sideBar.addComponent(createUseWrapMode());

		return sideBar;
	}

	private Component createUseWrapMode() {
		CheckBox useWrapMode = new CheckBox("Wrap Mode");
		useWrapMode.setImmediate(true);
		useWrapMode.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(ClickEvent event) {
				boolean useWrapMode = event.getButton().booleanValue();
				ace.setUseWrapMode(useWrapMode);
			}
		});
		return useWrapMode;
	}

	private Component createGetSet() {
		VerticalLayout la = new VerticalLayout();
		final TextArea ta = new TextArea(null, (String) ace.getValue());
		ta.setWidth("100%");
		la.addComponent(ta);
		HorizontalLayout hl = new HorizontalLayout();
		Button get = new Button("<- getValue");
		Button set = new Button("setValue ->");
		hl.addComponent(get);
		hl.addComponent(set);
		get.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(Button.ClickEvent event) {
				ta.setValue(ace.getValue());
			}
		});
		set.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(Button.ClickEvent event) {
				ace.setValue(ta.getValue());
			}
		});
		la.addComponent(hl);
		return la;
	}

	private Component createGetCursor() {
		Button getCursor = new Button("Get Cursor");
		getCursor.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(Button.ClickEvent event) {
				mainWindow.showNotification("Cursor: "
						+ ace.getCursorPosition());
			}
		});
		getCursor.setWidth("100%");
		return getCursor;
	}

	private Component createSelectAll() {
		Button selectAll = new Button("Select All");
		selectAll.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(Button.ClickEvent event) {
				ace.selectAll();
			}
		});
		selectAll.setWidth("100%");
		return selectAll;
	}

	private Component createImmediate() {
		CheckBox imme = new CheckBox("Immediate");
		imme.setImmediate(true);
		imme.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(ClickEvent event) {
				boolean imm = event.getButton().booleanValue();
				ace.setImmediate(imm);
			}
		});
		return imme;
	}

	private Component createReadonly() {
		CheckBox readonly = new CheckBox("Read-only");
		readonly.setImmediate(true);
		readonly.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(ClickEvent event) {
				boolean ro = event.getButton().booleanValue();
				ace.setReadOnly(ro);
			}
		});
		return readonly;
	}

	private Component createSearch() {
		VerticalLayout la = new VerticalLayout();
		final TextField tf = new TextField("Find Text");
		tf.setWidth("100%");
		la.addComponent(tf);
		Button searchButton = new Button("Find");
		searchButton.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(ClickEvent event) {
				addSearchMarkers((String) ace.getValue(),
						(String) tf.getValue());
			}
		});
		searchButton.setWidth("100%");
		la.addComponent(searchButton);
		return la;
	}

	private Component createLockLine() {
		Button lockLine = new Button("Lock Cursor Line");
		lockLine.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(Button.ClickEvent event) {
				int cursor = ace.getCursorPosition();
				String text = (String) ace.getValue();
				int start = text.lastIndexOf("\n", cursor - 1) + 1;
				int end = text.indexOf("\n", start + 1);
				if (end == -1)
					end = text.length();
				ace.addMarker(Marker.newLockMarker(start, end));
			}
		});
		lockLine.setWidth("100%");
		return lockLine;
	}

	private Component createClearMarkers() {
		Button clearMarkers = new Button("Clear Markers");
		clearMarkers.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(Button.ClickEvent event) {
				ace.clearMarkers();
			}
		});
		clearMarkers.setWidth("100%");
		return clearMarkers;
	}

	private Component createCheckErrors() {
		VerticalLayout la = new VerticalLayout();
		CheckBox checkErrors = new CheckBox("Custom Error Checker");
		checkErrors.setImmediate(true);
		checkErrors.setValue(false);
		checkErrors.addListener(new Button.ClickListener() {
			/* @Override */
			public void buttonClick(ClickEvent event) {
				boolean che = event.getButton().booleanValue();
				if (che) {
					ecl.checkErrors((String) ace.getValue()); // first check
					ace.addListener(ecl);
				} else {
					ace.removeListener(ecl);
					ace.clearMarkersOfType(Marker.Type.ERROR);
				}
			}
		});
		la.addComponent(checkErrors);
		la.addComponent(new Label("(interferes with builtin (if any) :-( )"));
		return la;
	}

	private Component createSelectTCEM() {
		LinkedList<String> modes = new LinkedList<String>();
		for (TextChangeEventMode v : TextChangeEventMode.values()) {
			modes.add(v.toString());
		}
		final OptionGroup ce = new OptionGroup("TextChangeEventMode", modes);
		ce.setImmediate(true);
		ce.select("LAZY");
		ce.addListener(new Property.ValueChangeListener() {
			/* @Override */
			public void valueChange(ValueChangeEvent event) {
				String v = (String) ce.getValue();
				ace.setTextChangeEventMode(TextChangeEventMode.valueOf(v));
			}
		});
		return ce;
	}

	private Component createSelectTCT() {
		VerticalLayout la = new VerticalLayout();
		final TextField tctf = new TextField("TextChangeTimeout (ms)", "400");
		tctf.setWidth("100%");
		la.addComponent(tctf);
		Button tctButton = new Button("Set TextChangeTimeout");
		tctButton.addListener(new Button.ClickListener() {
//			@Override
			public void buttonClick(ClickEvent event) {
				Integer i = Integer.valueOf((String) tctf.getValue());
				if (i != null) {
					ace.setTextChangeTimeout(i);
				}
			}
		});
		tctButton.setWidth("100%");
		la.addComponent(tctButton);
		return la;
	}

	private Component createSelectMode() {
		NativeSelect ns = new NativeSelect("Ace Mode");
		ns.setNullSelectionAllowed(false);
		for (AceMode v : AceMode.values()) {
			ns.addItem(v.toString());
		}
		ns.setImmediate(true);
		ns.addListener(new Property.ValueChangeListener() {
//			@Override
			public void valueChange(ValueChangeEvent event) {
				AceMode mode = AceMode.valueOf((String) event.getProperty()
						.getValue());
				ace.setMode(mode);
			}
		});
		ns.select("javascript");
		return ns;
	}

	private Component createSelectTheme() {
		NativeSelect ns = new NativeSelect("Ace Theme");
		ns.setNullSelectionAllowed(false);
		for (AceTheme v : AceTheme.values()) {
			ns.addItem(v.toString());
		}
		ns.setImmediate(true);
		ns.addListener(new Property.ValueChangeListener() {
//			@Override
			public void valueChange(ValueChangeEvent event) {
				AceTheme theme = AceTheme.valueOf((String) event.getProperty()
						.getValue());
				ace.setTheme(theme);
			}
		});
		ns.select("eclipse");
		return ns;
	}

	private Component createSelectFontSize() {
		NativeSelect ns = new NativeSelect("Ace Font Size");
		ns.setNullSelectionAllowed(false);
		for (int i = 8; i < 24; ++i) {
			ns.addItem(i + "px");
		}
		ns.setImmediate(true);
		ns.addListener(new Property.ValueChangeListener() {
//			@Override
			public void valueChange(ValueChangeEvent event) {
				String font = (String) event.getProperty().getValue();
				ace.setFontSize(font);
			}
		});
		ns.select("12px");
		return ns;
	}

	private void addSearchMarkers(String text, String word) {
		ace.clearMarkersOfType(Marker.Type.SEARCH);
		if (word.isEmpty()) {
			return;
		}
		int i = text.indexOf(word);
		while (i != -1) {
			Marker m = Marker.newSearchMarker(i, i + word.length());
			ace.addMarker(m);
			i = text.indexOf(word, i + 1);
		}
	}

}
