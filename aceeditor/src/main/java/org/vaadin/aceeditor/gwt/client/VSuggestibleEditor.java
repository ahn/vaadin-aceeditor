package org.vaadin.aceeditor.gwt.client;

import org.vaadin.aceeditor.gwt.client.EditorFacade.Key;
import org.vaadin.aceeditor.gwt.client.EditorFacade.KeyPressHandler;
import org.vaadin.aceeditor.gwt.client.SuggestionHandler.SuggestionRequestedListener;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;

public class VSuggestibleEditor extends VAceMarkerEditor implements
		KeyPressHandler, SuggestionRequestedListener {

	private boolean suggestionEnabled = false;

	SuggestionHandler suha;

	public VSuggestibleEditor() {
		super();

	}

	@Override
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		super.updateFromUIDL(uidl, client);
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		if (!suggestionEnabled && uidl.hasAttribute("suggestion-enabled")) {
			editor.addHandler(this);
			suggestionEnabled = true;
			suha = new SuggestionHandler(markerEditor);
			suha.addListener(this);
		}

		if (suggestionEnabled) {
			suha.updateFromUIDL(uidl);
		}
	}

	@Override
	protected boolean textUpdateAllowed() {
		return (!suggestionEnabled || suha.getPhase() == SuggestionHandler.Phase.NOT_SUGGESTING)
				&& super.textUpdateAllowed();
	}

	@Override
	public void textChanged() {
		super.textChanged();
		suha.textChanged();
	}

	@Override
	public void cursorChanged() {
		super.cursorChanged();
		suha.cursorChanged();
	}

//	@Override
	public boolean keyPressed(Key key) {
		return suha.keyPressed(key);
	}

//	@Override
	public void suggestionRequested(int cursor) {
		client.updateVariable(paintableId, "suggestion-requested", cursor, true);
	}
}
