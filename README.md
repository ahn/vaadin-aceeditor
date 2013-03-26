
Vaadin AceEditor
================

**[Ace code editor](http://ace.ajax.org) wrapped inside a TextField-like [Vaadin](http://vaadin.com) component.**

Available as an [add-on in Vaadin Directory](http://vaadin.com/addon/aceeditor).

From version 0.7.0 onwards this add-on requires Vaadin 7. The earlier Vaadin 6 version most likely won't be maintained any more. The new Vaadin 7 version is somewhat changed from the earlier version. Some things are still missing but hopefully will be implemented at some point.

## Demo

[Online demo of Vaadin AceEditor](http://antti.virtuallypreinstalled/aceeditor). The source code of the demo available [here](...).

## Getting started

1. Start a Vaadin 7 project.
2. Download a Vaadin 7 version of [AceEditor from Vaadin directory](http://vaadin.com/addon/aceeditor) and put the `aceeditor-x.x.x.jar` in your `WEB-INF/lib` directory. Or, use the Maven dependency.
3. Compile widgetset.
4. See [below](#how-to-use) for instructions on how to use the `AceEditor` component. 

## How to use

These instructions are for Vaadin 7 version of AceEditor.

### Basics

    AceEditor editor = new AceEditor();
    editor.setValue("Hello world!");
    layout.addComponent(editor);
    // ...
    String s = editor.getValue();

### Mode & theme

*Mode* defines the programming language used in the editor. *Theme* is the appearance of the editor.

    editor.setMode(AceMode.python);
    editor.setTheme(AceTheme.eclipse);
    
    // Use worker (if available for the current mode)
    editor.setUseWorker(true);

**NOTE**: to be able to use workers, you must host the worker files on the same server (same-origin policy restriction.) See [below](#ace-file-paths).

### Ace file paths

By default, Vaadin AceEditor gets the mode, theme and worker files from [this location](http://d1n0x3qji82z53.cloudfront.net/src-min-noconflict) [(example)](http://d1n0x3qji82z53.cloudfront.net/src-min-noconflict/theme-eclipse.js). I guess it's sort of a semi-official location for Ace files, at least it's used in [an Ace tutorial](http://ace.ajax.org/#nav=embedding).

If you want to use some other location, for example to host the files on your own server, that's possible.

Example: host the Ace files within your Vaadin app. First, put the Ace *src-min-noconflict* mode, theme and worker files (found in the `ace` directory in the
[Vaadin Directory download package](http://vaadin.com/addon/aceeditor)
or in the `src-min-noconflict` directory
[here](https://github.com/ajaxorg/ace-builds))
to the `webapp/static/ace` directory.
The structure should look something like this:

    webapp/
      META-INF/
      static/
        ace/
          mode-abap.js
          ...
      VAADIN/
      WEB-INF/

Then, tell the editor to use the files in the location:

    editor.setThemePath("/static/ace");
    editor.setModePath("/static/ace");
    editor.setWorkerPath("/static/ace");    

### Other settings

    editor.setWordWrap(false);
    editor.setReadOnly(false);
    // TODO: more
    
### Listeners

##### TextChangeListener

    ed.addTextChangeListener(new TextChangeListener() {
        @Override
		public void textChange(TextChangeEvent event) {
			Notification.show("Text: " + event.getText());
		}
	});

##### SelectionChangeListener

    ed.addSelectionChangeListener(new SelectionChangeListener() {
    	@Override
		public void selectionChanged(SelectionChangeEvent e) {
            int cursor = e.getSelection().getCursorPosition();
			Notification.show("Cursor at: " + cursor);
		}
	});
    
### Markers

Ace supports custom markers within the code. The marker appearance is defined by a css class.

    String cssClass = "mymarker1";
    AceRange range = editor.getSelection();    
    AceMarker.Type type = AceMarker.Type.text; // text or line
    boolean inFront = false; // whether in front or behind the text
    AceMarker.OnTextChange onChange = AceMarker.OnTextChange.ADJUST;
    AceMarker m = new AceMarker(range, cssClass, type, inFront, onChange);
    editor.addMarker(m);
    // ...
    editor.removeMarker(m);

The cssClass must be defined in some css file, for example `mymarkers.css`:

    .ace_marker-layer .mymarker1 {
        background: red;
    	border-bottom: 2px solid black;
    	position: absolute;
    }

...and then use the file:

    @StyleSheet("mymarkers.css")
    public class MyUI extends UI {
    
The `OnTextChange` defines how the marker behaves when the editor text changes.

* DEFAULT: stay in the original position. That's what Ace does by default.
* ADJUST: adjust the marker position when text changes. For example, if a line is added before the marker, the marker is moved one line down, thus keeping its "logical" position within the text.
* REMOVE: remove the marker on text change.
 

### Annotations

Ace supports annotations, i.e little notes on the side of the editor.

Vaadin AceEditor has two types of Annotations: *row annotations* and *marker annotations*. Only one type of annotations is possible to be used on an AceEditor at a time.

*Row annotations* are standard Ace annotations that are added to a certain row and Ace handles their position from there on. (Their possibly changed positions can't be retrieved later from Ace, which is the cause for this two kinds of annotations mess in Vaadin AceEditor.)

*Marker annotations* are attached to a marker. If the marker changes position the annotation follows.

    String msg = "Warning!!!";
    AceAnnotation.Type type = AceAnnotation.Type.warning;
    AceAnnotation ann = new AceAnnotation(msg, type);
    if (rowAnnotations) {
        editor.addRowAnnotation(ann, 2);
    }
    else {
        editor.addMarker(marker);
        editor.addMarkerAnnotation(ann, marker);
    }
    // ...
    editor.clearRowAnnotations();
    editor.clearMarkerAnnotations();


## Compiling this project

To package and install the Vaadin AceEditor addon to your local Maven repository, run

    cd aceeditor
    mvn install

To run a demo application in http://localhost:8080

    cd aceeditor-demo
    mvn vaadin:compile jetty:run
    
To create an addon package that can be uploaded to Vaadin Directory

    cd aceeditor
    mvn clean package assembly:single




