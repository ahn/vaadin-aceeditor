package org.vaadin.aceeditor;

import org.vaadin.aceeditor.AceEditor.SelectionChangeEvent;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.client.AceAnnotation;
import org.vaadin.aceeditor.client.AceDoc;
import org.vaadin.aceeditor.client.AceMarker;
import org.vaadin.aceeditor.client.AceMarker.OnTextChange;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@Theme("reindeer")
@StyleSheet("ace-markers.css")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class AceEditorDemo extends UI {

	private AceEditor editor = new AceEditor();

	private long latestMarkerId = 0L;
	private String newMarkerId() {
		return "m"+(++latestMarkerId);
	}
	
	private NativeSelect markerAnnotationSelect = new NativeSelect("Marker");
	{
		markerAnnotationSelect.setNullSelectionAllowed(false);
	}

	public AceEditorDemo() {
		String s = "var s = \"Hello!\";\n"
				+ "while (true) {\n" +
				"    alert(s);\n" +
				"}\n";
		
		editor.setValue(s);
		
		editor.focus();
		
	}
	
	@Override
	protected void init(VaadinRequest request) {
		
		final HorizontalSplitPanel split = new HorizontalSplitPanel();
		split.setSizeFull();
		setContent(split);
		split.setSplitPosition(35f);

		final VerticalLayout leftBar = new VerticalLayout();
		leftBar.setMargin(true);
		
		split.setFirstComponent(leftBar);
		
		editor.setSizeFull();
		
		// The Ace files are at webapp/static/ace directory.
		
		// The path depends on the server config.

		editor.setThemePath("/static/ace");
		editor.setModePath("/static/ace");
		editor.setWorkerPath("/static/ace");
		
		// http://stackoverflow.com/a/3722122
//		editor.setThemePath("/aceeditor/static/ace");
//		editor.setModePath("/aceeditor/static/ace");
//		editor.setWorkerPath("/aceeditor/static/ace");
		
		
		
		leftBar.addComponent(createValueTextArea());
		
		leftBar.addComponent(createCursorPanel());
		leftBar.addComponent(createFocusPanel());
		
		leftBar.addComponent(createThemeModePanel());
		leftBar.addComponent(createOptionsPanel());
		
		leftBar.addComponent(createTabPanel());
		
		leftBar.addComponent(createScrollPanel());
		
		leftBar.addComponent(createMarkerPanel());
		leftBar.addComponent(createRowAnnotationPanel());
		

		leftBar.addComponent(createMarkerAnnotationPanel());
		leftBar.addComponent(createErrorCheckingDemo());
		leftBar.addComponent(createSuggestionDemo());
		leftBar.addComponent(createErrorCorrectionDemo());

		VerticalLayout linkLayout = new VerticalLayout();
		linkLayout.addComponent(new Link("Addon at Vaadin Directory", new ExternalResource("http://vaadin.com/addon/aceeditor")));
		linkLayout.addComponent(new Link("Source at Github", new ExternalResource("https://github.com/ahn/vaadin-aceeditor/")));
		leftBar.addComponent(new Panel("Links", linkLayout));
		
		split.setSecondComponent(editor);
		
	}

	private Component createCursorPanel() {
		HorizontalLayout la = new HorizontalLayout();
		
		final TextField cursor = new TextField();
		cursor.setValue("0");
		
		Button bu = new Button("Set cursor");
		bu.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					int v = Integer.valueOf(cursor.getValue());
					editor.setCursorPosition(v);
				}
				catch (NumberFormatException e) {
					return;
				}
			}
		});
		
		editor.addSelectionChangeListener(new SelectionChangeListener() {
			@Override
			public void selectionChanged(SelectionChangeEvent e) {
				cursor.setValue(""+editor.getCursorPosition());
				
			}
		});
		
		la.addComponent(cursor);
		la.addComponent(bu);
		
		return new Panel("Cursor", la);
	}
	
	private Component createFocusPanel() {
		HorizontalLayout la = new HorizontalLayout();
		
		final Label label = new Label();
		
		editor.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				label.setValue("Editor focused");
			}
		});
		
		editor.addBlurListener(new BlurListener() {
			@Override
			public void blur(BlurEvent event) {
				label.setValue("Editor not focused");
			}
		});
		
		Button bu = new Button("Focus");
		bu.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.focus();
			}
		});
		
		la.addComponent(bu);
		la.addComponent(label);
		return new Panel("Focus", la);
	}

	private Component createTabPanel() {
		VerticalLayout la = new VerticalLayout();

		final CheckBox softTabs = new CheckBox("Soft tabs");
		softTabs.setValue(true);
		softTabs.setImmediate(true);
		softTabs.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setUseSoftTabs(softTabs.getValue());
			}
		});
		
		HorizontalLayout ho = new HorizontalLayout();
		final TextField tabSize = new TextField();
		tabSize.setValue("4");
		Button setTabSize = new Button("Set tab size");
		setTabSize.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					editor.setTabSize(Integer.valueOf(tabSize.getValue()));
				}
				catch (NumberFormatException e) {
					// ...
					tabSize.setValue("4");
					editor.setTabSize(4);
				}
			}
		});
		ho.addComponent(tabSize);
		ho.addComponent(setTabSize);
		
		la.addComponent(softTabs);
		la.addComponent(ho);
		
		return new Panel("Tabs", la);
	}
	
	private Component createOptionsPanel() {
		VerticalLayout la = new VerticalLayout();

		final CheckBox enabled = new CheckBox("Enabled");
		enabled.setValue(true);
		enabled.setImmediate(true);
		enabled.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setEnabled(enabled.getValue());
			}
		});

		final CheckBox readOnly = new CheckBox("Read-only");
		readOnly.setImmediate(true);
		readOnly.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setReadOnly(readOnly.getValue());
			}
		});
		
		final CheckBox wordwrap = new CheckBox("Word wrap");
		wordwrap.setImmediate(true);
		wordwrap.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setWordWrap(wordwrap.getValue());
			}
		});
		
		final CheckBox invisibles = new CheckBox("Show Invisibles");
		invisibles.setValue(false);
		invisibles.setImmediate(true);
		invisibles.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setShowInvisibles(invisibles.getValue());
			}
		});
		
		final CheckBox indentGuidelines = new CheckBox("Indent guidelines");
		indentGuidelines.setValue(true);
		indentGuidelines.setImmediate(true);
		indentGuidelines.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setDisplayIndentGuides(indentGuidelines.getValue());
			}
		});

		la.addComponent(enabled);
		la.addComponent(readOnly);
		la.addComponent(wordwrap);
		la.addComponent(invisibles);
		la.addComponent(indentGuidelines);
		
		return new Panel("Settings", la);
	}
	
	private Component createScrollPanel() {
		VerticalLayout ve = new VerticalLayout();
		HorizontalLayout la1 = new HorizontalLayout();
		final TextField row = new TextField(null, "0");
		Button b = new Button("Scroll to row");
		b.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					int i = Integer.valueOf(row.getValue());
					editor.scrollToRow(i);
				}
				catch (NumberFormatException e) {
					Notification.show("Give an integer.");
				}
			}
		});
		la1.addComponent(row);
		la1.addComponent(b);
		ve.addComponent(la1);
		
		HorizontalLayout la2 = new HorizontalLayout();
		final TextField pos = new TextField(null, "0");
		Button b2 = new Button("Scroll to position");
		b2.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					int i = Integer.valueOf(pos.getValue());
					editor.scrollToPosition(i);
				}
				catch (NumberFormatException e) {
					Notification.show("Give an integer.");
				}
			}
		});
		la2.addComponent(pos);
		la2.addComponent(b2);
		ve.addComponent(la2);
		
		return new Panel("Scroll to", ve);
	}

	private Component createValueTextArea() {
		
		final TextArea ta = new TextArea();
		ta.setWidth("100%");
		Button get = new Button("<<< getValue");
		Button set = new Button("setValue >>>");
		
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(ta);
		
		HorizontalLayout ho = new HorizontalLayout();
		layout.addComponent(ho);
		
		ho.addComponent(get);
		ho.addComponent(set);
		
		get.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				ta.setValue(editor.getValue());
			}
		});
		
		set.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.setValue(ta.getValue());
			}
		});
		
		return new Panel("Value", layout);
	}

	private Component createMarkerPanel() {
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		final HorizontalLayout markerLayout = new HorizontalLayout();
		
		HorizontalLayout ho = new HorizontalLayout();
		final TextField cssField = new TextField("CSS Class", "mymarker1");
		ho.addComponent(cssField);
		final NativeSelect typeSelect = new NativeSelect("Type");
		ho.addComponent(typeSelect);
		for (AceMarker.Type item : AceMarker.Type.values()) {
			typeSelect.addItem(item);
		}
		typeSelect.select(AceMarker.Type.line);
		typeSelect.setNullSelectionAllowed(false);
		final CheckBox inFrontCheck = new CheckBox("InFront");
		ho.addComponent(inFrontCheck);
		ho.setComponentAlignment(inFrontCheck, Alignment.BOTTOM_LEFT);
		
		final NativeSelect changeSelect = new NativeSelect("OnTextChange");
		ho.addComponent(changeSelect);
		for (AceMarker.OnTextChange item : AceMarker.OnTextChange.values()) {
			changeSelect.addItem(item);
		}
		changeSelect.select(AceMarker.OnTextChange.DEFAULT);
		changeSelect.setNullSelectionAllowed(false);
		
		layout.addComponent(ho);
		
		Button button = new Button("Add marker to selection");
		layout.addComponent(button);
		button.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				TextRange selection = editor.getSelection();
				if (selection.isZeroLength()) {
					Notification.show("Select some text first");
					return;
				}
				String css = cssField.getValue();
				AceMarker.Type type = (AceMarker.Type)typeSelect.getValue();
				boolean inFront = inFrontCheck.getValue();
				AceMarker.OnTextChange onChange = (OnTextChange) changeSelect.getValue();
				final String markerId = newMarkerId();
				AceMarker m = new AceMarker(markerId, selection, css, type, inFront, onChange);
				editor.addMarker(m);
				final Button mb = new Button(""+markerId);
				markerLayout.addComponent(mb);
				markerAnnotationSelect.addItem(markerId);
				mb.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						editor.removeMarker(markerId);
						markerLayout.removeComponent(mb);
						markerAnnotationSelect.removeItem(markerId);
					}
				});
				
			}
		});
		
		
		layout.addComponent(markerLayout);
		
		return new Panel("Markers", layout);
	}
	

	
	private Component createRowAnnotationPanel() {
		
		VerticalLayout layout = new VerticalLayout();
		
		HorizontalLayout ho = new HorizontalLayout();
		
		final TextField msgField = new TextField("Message","Hello");
		ho.addComponent(msgField);
		
		final NativeSelect typeSelect = new NativeSelect("Type");
		typeSelect.setNullSelectionAllowed(false);
		for (AceAnnotation.Type type : AceAnnotation.Type.values()) {
			typeSelect.addItem(type);
		}
		typeSelect.select(AceAnnotation.Type.info);
		
		ho.addComponent(typeSelect);
		
		final TextField rowField = new TextField("Row","2");
		ho.addComponent(rowField);
		
		Button add = new Button("Add");
		add.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				AceAnnotation.Type type = (AceAnnotation.Type)typeSelect.getValue();
				int row = 0;
				try {
					row = Integer.valueOf(rowField.getValue());
				}
				catch (NumberFormatException e) {
					Notification.show(rowField.getValue() + " is not a number");
					return;
				}
				String msg = msgField.getValue();
				editor.addRowAnnotation(new AceAnnotation(msg, type), row);
			}
		});
		
		Button clear = new Button("Clear");
		clear.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.clearRowAnnotations();
			}
		});
		
		layout.addComponent(ho);
		layout.addComponent(add);
		layout.addComponent(clear);
		
		return new Panel("Row Annotations", layout);
	}
	
	private Component createMarkerAnnotationPanel() {
		
		VerticalLayout layout = new VerticalLayout();
		
		HorizontalLayout ho = new HorizontalLayout();
		
		final TextField msgField = new TextField("Message","Hello");
		ho.addComponent(msgField);
		
		final NativeSelect typeSelect = new NativeSelect("Type");
		typeSelect.setNullSelectionAllowed(false);
		for (AceAnnotation.Type type : AceAnnotation.Type.values()) {
			typeSelect.addItem(type);
		}
		typeSelect.select(AceAnnotation.Type.info);
		
		ho.addComponent(typeSelect);
		
		ho.addComponent(markerAnnotationSelect );
		
		Button add = new Button("Add");
		add.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				AceAnnotation.Type type = (AceAnnotation.Type)typeSelect.getValue();
				String msg = msgField.getValue();
				String markerId = (String) markerAnnotationSelect.getValue();
				if (markerId==null) {
					Notification.show("Select marker");
					return;
				}
				editor.addMarkerAnnotation(new AceAnnotation(msg, type), markerId);
			}
		});
		
		Button clear = new Button("Clear");
		clear.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				editor.clearRowAnnotations();
			}
		});
		
		layout.addComponent(ho);
		layout.addComponent(add);
		layout.addComponent(clear);
		
		return new Panel("Marker Annotations", layout);
	}

	private Component createThemeModePanel() {
		
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		
		HorizontalLayout ve = new HorizontalLayout();
		
		final NativeSelect themeSelect = new NativeSelect("Theme");
		ve.addComponent(themeSelect);
		for (AceTheme theme : AceTheme.values()) {
			themeSelect.addItem(theme);
		}
		themeSelect.setNullSelectionAllowed(false);
		themeSelect.setImmediate(true);
		themeSelect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setTheme((AceTheme)themeSelect.getValue());
			}
		});
		themeSelect.select(AceTheme.eclipse);
		
		final NativeSelect modeSelect = new NativeSelect("Mode");
		ve.addComponent(modeSelect);
		for (AceMode mode : AceMode.values()) {
			modeSelect.addItem(mode);
		}
		modeSelect.setNullSelectionAllowed(false);
		modeSelect.setImmediate(true);
		modeSelect.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setMode((AceMode)modeSelect.getValue());
			}
		});
		modeSelect.select(AceMode.javascript);

        final NativeSelect fontSelect = new NativeSelect("Font Size");
        ve.addComponent(fontSelect);
        fontSelect.addItem("10px");
        fontSelect.addItem("12px");
        fontSelect.addItem("14px");
        fontSelect.addItem("16px");
        fontSelect.addItem("18px");
        fontSelect.addItem("24px");
        fontSelect.setNullSelectionAllowed(false);
        fontSelect.setImmediate(true);
        fontSelect.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                editor.setFontSize((String)fontSelect.getValue());
            }
        });
        fontSelect.select("14px");

		layout.addComponent(ve);
		
		final CheckBox cb = new CheckBox("useWorker");
		cb.setImmediate(true);
		cb.setValue(true);
		cb.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				editor.setUseWorker(cb.getValue());
			}
		});
		
		ve.addComponent(cb);
		ve.setComponentAlignment(cb, Alignment.BOTTOM_LEFT);
		
		return new Panel("Theme & Mode", layout);
	}
	
	private Component createErrorCheckingDemo() {
		Button b = new Button("Open error-checker demo");
		b.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Window w = new Window("Don't write X's");
				w.setWidth("50%");
				w.setHeight("50%");
				w.center();
				AceEditor ee = new AceEditor();
				ee.setValue("Don't write X's!");
				ee.setSizeFull();
				w.setContent(ee);
				UI.getCurrent().addWindow(w);
				
				MyErrorChecker checker = new MyErrorChecker();
				checker.attachTo(ee);
			}
			
		});
		
		return new Panel("Error Checker", b);
	}
	
	private Component createSuggestionDemo() {
		Button b = new Button("Open suggestion demo");
		b.addClickListener(new ClickListener() {

			int width = 10;
			SuggestionExtension ext;
			
			@Override
			public void buttonClick(ClickEvent event) {
				Window w = new Window("Press Ctrl+Space for suggestions.");
				w.setWidth("50%");
				w.setHeight("50%");
				w.center();
				
				VerticalLayout wrapper = new VerticalLayout();
				wrapper.setSizeFull();
				wrapper.setSpacing(true);
				
				Button increase = new Button("increase", new IncreaseClickListener());
				
				AceEditor ee = new AceEditor();
				ee.setValue("Press Ctrl+Space for suggestions.\nOr type a dot.");
				ee.setSizeFull();
				
				wrapper.addComponents(increase, ee);
				
				w.setContent(wrapper);
				UI.getCurrent().addWindow(w);
				
				ext = new SuggestionExtension(new MySuggester());
				ext.extend(ee);
				//ext.setPopupWidth(width);
			}
			
			class IncreaseClickListener implements ClickListener{

				@Override
				public void buttonClick(ClickEvent event) {
					width += 100;
					ext.setPopupWidth(width);
					ext.setPopupHeight(width);
					ext.setpopupDescriptionWidth(width);
				}
				
			}
			
		});
		
		return new Panel("Suggestions", b);
	}

	private Component createErrorCorrectionDemo() {
		Button b = new Button("Open auto-correction demo");
		b.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				Window w = new Window("Leet-speakerizer");
				w.setWidth("50%");
				w.setHeight("50%");
				w.center();
				AceEditor ee = new AceEditor();
				ee.setTextChangeTimeout(200);
				ee.setWordWrap(true);
				ee.setValue("The text you type is auto-corrected to leet-speak on the server-side.\n\n" +
						"What's nice is that none of the stuff you type gets lost, " +
						"even when typed while visiting the server.");
				ee.setSizeFull();
				w.setContent(ee);
				UI.getCurrent().addWindow(w);
				
				LeetSpeakerizer leeter = new LeetSpeakerizer();
				leeter.attachTo(ee);
			}
			
		});
		
		return new Panel("Auto-correction", b);
	}

}
