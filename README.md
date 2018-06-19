Vaadin AceEditor
================

**[Ace code editor](http://ace.ajax.org) wrapped inside a TextArea-like [Vaadin](http://vaadin.com) component.**

Available as an [add-on in Vaadin Directory](http://vaadin.com/addon/aceeditor).
**This fork has been made by the [MRules](https://mrules.xyz) dev team in order to create a rich web editor to configure its rule engine.**

### NOTE
**Unfortunately I (@ahn) currently don't have time to maintain this project.
I can accept pull requests and put a new version to Vaadin Directory once in a while but not do much beyond that.
If you'd like to be a more active maintainer, feel free to contact me.**

From version 0.8.15 onwards this add-on requires Vaadin 8. Versions 0.7.0 - 0.8.14 are for Vaadin 7, and versions before that are for Vaadin 6. [The "quick and brutal" port to Vaadin 8 by willtemperley](https://github.com/ahn/vaadin-aceeditor/pull/56). 

* Currently using version 1.1.9 of Ace.

This add-on is still in an experimental phase, interfaces etc. are subject to change.

<!--
## Demo

[Online demo of Vaadin AceEditor](http://130.230.142.91:8080/aceeditor/). The source code of the demo available [here](https://github.com/ahn/vaadin-aceeditor/tree/master/aceeditor-demo).
-->


## Getting started

1. Start a Vaadin 8 project.
2. Get the [AceEditor addon from Vaadin directory](http://vaadin.com/addon/aceeditor). Maven is recommended.
3. Compile widgetset.
4. See [below](#how-to-use) for instructions on how to use the `AceEditor` component. 

## How to use

These instructions are for Vaadin 7 version of AceEditor.

### Basics
```java
AceEditor editor = new AceEditor();
editor.setValue("Hello world!");
layout.addComponent(editor);
// ...
String s = editor.getValue();
```

### Mode & theme

*Mode* defines the programming language used in the editor. *Theme* is the appearance of the editor.

```java
editor.setMode(AceMode.python);
editor.setTheme(AceTheme.eclipse);

// Use worker (if available for the current mode)
editor.setUseWorker(true);
```

**NOTE**: to be able to use workers, you must host the worker files on the same server (same-origin policy restriction.) See [below](#ace-file-paths).

### Ace file paths

By default, Vaadin AceEditor gets the mode, theme and worker files from the [ace-builds repository](https://github.com/ajaxorg/ace-builds) via rawgit.com. For example: [mode-javascript.js](//cdn.rawgit.com/ajaxorg/ace-builds/e3ccd2c654cf45ee41ffb09d0e7fa5b40cf91a8f/src-min-noconflict/mode-javascript.js). Currently using version 1.1.9 of Ace. The 1.2.x doesn't work (yet).

**It's probably safer to host the mode&theme files yourself so that you can be sure that they're compatible with the main Ace file used by this editor.**

To host the files on your own server, here's how:

First, get the `ace` dir from the [Vaadin Directory download package](http://vaadin.com/addon/aceeditor). It contains the [src-min-noconflict](https://github.com/ajaxorg/ace-builds/tree/master/src-min-noconflict) Ace files compatible with this addon.
Copy the `ace` dir to location `webapp/static/ace` in your Vaadin application.
The structure should look something like this:

    webapp/
      META-INF/
      static/
        ace/
          mode-abap.js
          ...
      VAADIN/
      WEB-INF/
      
And have this in your `web.xml`:
```xml
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/static/*</url-pattern>
</servlet-mapping>
```

Then, tell the editor to use the files in the location:

```java
editor.setThemePath("/static/ace");
editor.setModePath("/static/ace");
editor.setWorkerPath("/static/ace");    
// or "/myapp/static/ace" depending on server configuration.
``` 
Now, Ace should read the theme/mode/worker files from your local server.
    
### Other settings

```java
editor.setWordWrap(false);
editor.setReadOnly(false);
editor.setShowInvisibles(false);
// TODO: more
```

### Listeners

##### TextChangeListener

```java
ed.addTextChangeListener(new TextChangeListener() {
    @Override
    public void textChange(TextChangeEvent event) {
        Notification.show("Text: " + event.getText());
    }
});
```

##### SelectionChangeListener

```java
ed.addSelectionChangeListener(new SelectionChangeListener() {
    @Override
    public void selectionChanged(SelectionChangeEvent e) {
        int cursor = e.getSelection().getCursorPosition();
        Notification.show("Cursor at: " + cursor);
    }
});
```

### Markers

Ace supports custom markers within the code. The marker appearance is defined by a css class.

```java
String cssClass = "mymarker1";
TextRange range = editor.getSelection();    
AceMarker.Type type = AceMarker.Type.text; // text or line
boolean inFront = false; // whether in front or behind the text
AceMarker.OnTextChange onChange = AceMarker.OnTextChange.ADJUST;
String markerId = editor.addMarker(range, cssClass, type, inFront, onChange);
// ...
editor.removeMarker(markerId);
```

The cssClass must be defined in some css file, for example `mymarkers.css`:

```css
.ace_marker-layer .mymarker1 {
    background: red;
	border-bottom: 2px solid black;
	position: absolute;
}
```

...and then use the file:

```java
@StyleSheet("mymarkers.css")
public class MyUI extends UI {
```
    
The `OnTextChange` defines how the marker behaves when the editor text changes.

* DEFAULT: stay in the original position. That's what Ace does by default.
* ADJUST: adjust the marker position when text changes. For example, if a line is added before the marker, the marker is moved one line down, thus keeping its "logical" position within the text.
* REMOVE: remove the marker on text change.
 

### Annotations

Ace supports annotations, i.e little notes on the side of the editor.

Vaadin AceEditor has two types of Annotations: *row annotations* and *marker annotations*. Only one type of annotations is possible to be used on an AceEditor at a time.

*Row annotations* are standard Ace annotations that are added to a certain row and Ace handles their position from there on. (Their possibly changed positions can't be retrieved later from Ace, which is the cause for this two kinds of annotations mess in Vaadin AceEditor.)

*Marker annotations* are attached to a marker. If the marker changes position the annotation follows.

```java
String msg = "Warning!!!";
AceAnnotation.Type type = AceAnnotation.Type.warning;
AceAnnotation ann = new AceAnnotation(msg, type);
if (rowAnnotations) {
    editor.addRowAnnotation(ann, 2);
}
else {
    String markerId = editor.addMarker(/*...*/);
    editor.addMarkerAnnotation(ann, markerId);
}
// ...
editor.clearRowAnnotations();
editor.clearMarkerAnnotations();
```

### Suggestions

This addon also includes a `SuggestionExtension` for implementing a "suggester" that gives user a list of text suggestions after she presses Ctrl+Space in AceEditor. An example `MySuggester` implementation [here](https://github.com/ahn/vaadin-aceeditor/blob/master/aceeditor-demo/src/main/java/org/vaadin/aceeditor/MySuggester.java). See the "suggestion demo" [here](http://antti.virtuallypreinstalled.com/aceeditor/).

```java
new SuggestionExtension(new MySuggester()).extend(editor);
```

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

[![Build Status](https://travis-ci.org/ahn/vaadin-aceeditor.png)](https://travis-ci.org/ahn/vaadin-aceeditor)


## Notes on implementation

### Server-client communication

This addon uses diffs to communicate between the server-side and the client-side of the AceEditor component. That is, when a user types something, the whole text is not sent to the server, just some kind of diff from the old value to the new one. Similarly, diffs are sent if the value changes on the server. The addon utitlizes the [diff-match-patch library](https://code.google.com/p/google-diff-match-patch/ ) along with the [differential synchronization algorithm](http://neil.fraser.name/writing/sync/) for communication.

Pros of this diff approach:

* Less data to be sent between client and server.
* The content of the editor can be changed concurrently on the server and on the client. This makes it possible to implement things like the "auto-correction demo" in the [aceeditor demo](http://antti.virtuallypreinstalled.com/aceeditor/) (code of the server-side "auto-corrector" [here](https://github.com/ahn/vaadin-aceeditor/blob/master/aceeditor-demo/src/main/java/org/vaadin/aceeditor/LeetSpeakerizer.java)). In the demo the value can be modified at the same time on the client and on the server without losing either modifications. Also, Google Docs style collaborative editor can be implemented on top of this.

Cons:

* Requires more cpu, for computing the diffs etc. (There's a room for optimization in the current implementation.)
* Complicates things...

## Links

* [Ace Website](http://ace.c9.io/)
* [Ace Kitchen Sink Demo](http://ace.c9.io/build/kitchen-sink.html)
* [Ace API](http://ace.c9.io/#nav=api), [Wiki](https://github.com/ajaxorg/ace/wiki)

## Related Projects
* [Ace wrapper for GWT](https://github.com/daveho/AceGWT)
* [Ace GWT Editor](https://github.com/ahome-it/ahome-ace)
