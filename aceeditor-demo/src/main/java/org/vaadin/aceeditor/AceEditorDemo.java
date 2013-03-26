package org.vaadin.aceeditor;

import org.vaadin.aceeditor.AceEditor.SelectionChangeEvent;
import org.vaadin.aceeditor.AceEditor.SelectionChangeListener;
import org.vaadin.aceeditor.client.AceClientMarker;
import org.vaadin.aceeditor.client.AceClientMarker.OnTextChange;
import org.vaadin.aceeditor.client.AceClientMarker.Type;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;


@StyleSheet("ace-markers.css")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class AceEditorDemo extends UI {

	
	
	private AceEditor editor = new AceEditor();
	
	private NativeSelect markerAnnotationSelect = new NativeSelect("Marker");
	{
		markerAnnotationSelect.setNullSelectionAllowed(false);
	}

	public AceEditorDemo() {
		String s = "";
		for (int i=0; i<10; i++) {
			for (int k=0; k<i; k++) {
				s += "Hello! ";
			}
			s += "Hello!\n";
		}
		editor.setValue(s);
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
		
		editor.setThemePath("/static/ace");
		editor.setModePath("/static/ace");
		editor.setWorkerPath("/static/ace");
		
		// http://stackoverflow.com/a/3722122
//		editor.setThemePath("/aceeditor/static/static/ace");
//		editor.setModePath("/aceeditor/static/static/ace");
//		editor.setWorkerPath("/aceeditor/static/static/ace");
		

		
		
		leftBar.addComponent(createValueTextArea());
		leftBar.addComponent(createCursorPanel());
		leftBar.addComponent(createThemeModePanel());
		leftBar.addComponent(createOptionsPanel());
		leftBar.addComponent(createMarkerPanel());
		leftBar.addComponent(createRowAnnotationPanel());
		

		leftBar.addComponent(createMarkerAnnotationPanel());
		leftBar.addComponent(createErrorEditor());
		
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

	private Component createOptionsPanel() {
		VerticalLayout la = new VerticalLayout();
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
		
		la.addComponent(readOnly);
		la.addComponent(wordwrap);
		
		return new Panel("Settings", la);
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
		for (Type item : AceMarker.Type.values()) {
			typeSelect.addItem(item);
		}
		typeSelect.select(AceMarker.Type.line);
		typeSelect.setNullSelectionAllowed(false);
		final CheckBox inFrontCheck = new CheckBox("InFront");
		ho.addComponent(inFrontCheck);
		ho.setComponentAlignment(inFrontCheck, Alignment.BOTTOM_LEFT);
		
		final NativeSelect changeSelect = new NativeSelect("OnTextChange");
		ho.addComponent(changeSelect);
		for (OnTextChange item : AceClientMarker.OnTextChange.values()) {
			changeSelect.addItem(item);
		}
		changeSelect.select(AceClientMarker.OnTextChange.DEFAULT);
		changeSelect.setNullSelectionAllowed(false);
		
		layout.addComponent(ho);
		
		Button button = new Button("Add marker to selection");
		layout.addComponent(button);
		button.addClickListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				AceRange selection = editor.getSelection();
				if (selection.getEndPosition()==selection.getStartPosition()) {
					Notification.show("Select some text first");
					return;
				}
				String css = cssField.getValue();
				AceClientMarker.Type type = (AceClientMarker.Type)typeSelect.getValue();
				boolean inFront = inFrontCheck.getValue();
				AceClientMarker.OnTextChange onChange = (OnTextChange) changeSelect.getValue();
				AceMarker m = new AceMarker(selection, css, type, inFront, onChange);
				final long markerId = editor.addMarker(m);
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
				Long markerId = (Long) markerAnnotationSelect.getValue();
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
		modeSelect.select(AceMode.text);
		
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
	
	private Component createErrorEditor() {
		Button b = new Button("Open error-checker demo");
		b.setStyleName(BaseTheme.BUTTON_LINK);
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
				
				ErrorChecker checker = new ErrorChecker();
				checker.attachTo(ee);
			}
			
		});
		
		return new Panel("Error Checker", b);
	}

}
