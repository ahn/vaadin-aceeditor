package org.vaadin.aceeditor.gwt.client;

import java.util.LinkedList;
import java.util.List;

import org.vaadin.aceeditor.gwt.shared.Suggestion;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.vaadin.terminal.gwt.client.ui.VOverlay;

public class SuggestPopup extends VOverlay implements KeyDownHandler,
		DoubleClickHandler, ChangeHandler {
	protected ListBox choiceList = new ListBox();
	// private String[] choices;
	// private String[] titles;

	private String startOfValue = "";

	public interface SuggestionSelectedListener {
		void suggestionSelected(Suggestion s);

		void noSuggestionSelected();
	}

	private SuggestionSelectedListener listener;

	private boolean open;
	private PopupPanel descriptionPopup;

	private List<Suggestion> suggs;
	private List<Suggestion> visibleSuggs = new LinkedList<Suggestion>();

	private static final int WIDTH = 200;
	private static final int HEIGHT = 200;

	// TODO addSuggestionSelectedListener?
	public void setSuggestionSelectedListener(SuggestionSelectedListener ssl) {
		listener = ssl;
	}

	public SuggestPopup(List<Suggestion> suggestions, String startOfValue) {
		super(true);

		this.suggs = suggestions;
		this.startOfValue = startOfValue.toLowerCase();

		setWidth(WIDTH + "px");
		setHeight(HEIGHT + "px");
		// choiceList.setWidth(WIDTH+"px");
		// choiceList.setHeight(HEIGHT+"px");
		add(choiceList);
		choiceList.setStyleName("list");
		choiceList.setVisibleItemCount(2);

		choiceList.addKeyDownHandler(this);
		choiceList.addDoubleClickHandler(this);

		choiceList.addChangeHandler(this);

		choiceList.setStylePrimaryName("codeeditor-suggestpopup-list");
	}

	private void populateList() {
		choiceList.clear();
		visibleSuggs.clear();
		int i = 0;
		for (Suggestion s : suggs) {
			if (s.getValueText().toLowerCase().startsWith(startOfValue)) {
				visibleSuggs.add(s);
				choiceList.addItem(s.getDisplayText(), "" + i);
			}
			i++;
		}
		if (choiceList.getItemCount() > 0 && isOpen()) {
			choiceList.setSelectedIndex(0);
			this.onChange(null);
		}
	}

	public void open() {
		populateList();
		if (choiceList.getItemCount() == 0) {
			close();
			return;
		} else {
			show();
			// Note that setting the selection programmatically does not cause
			// the
			// ChangeHandler.onChange(ChangeEvent) event to be fired.
			// Doing it manually.
			choiceList.setSelectedIndex(0);
			this.onChange(null);
			open = true;
		}
	}

	public void close() {
		this.hide();
		if (listener != null)
			listener.noSuggestionSelected();
	}

	public boolean isOpen() {
		return open;
	}

	@Override
	public void hide() {
		super.hide();
		open = false;
		if (descriptionPopup != null)
			descriptionPopup.hide();
		descriptionPopup = null;
	}

	@Override
	public void hide(boolean ac) {
		super.hide(ac);
		open = false;
		if (ac) {
			// This happens when user clicks outside this popup (or something
			// similar)
			// while autohide is on. We must cancel the suggestion.
			if (listener != null)
				listener.noSuggestionSelected();
		}
		if (descriptionPopup != null)
			descriptionPopup.hide();
		descriptionPopup = null;
	}

	/* @Override */
	public void onKeyDown(KeyDownEvent event) {
		int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER
				&& choiceList.getSelectedIndex() != -1) {
			event.preventDefault();
			event.stopPropagation();
			select();
		} else if (keyCode == KeyCodes.KEY_ESCAPE) {
			event.preventDefault();
			close();
		}
	}

	/* @Override */
	public void onDoubleClick(DoubleClickEvent event) {
		event.preventDefault();
		event.stopPropagation();
		select();
	}

	@Override
	public void onBrowserEvent(Event event) {
		if (event.getTypeInt() == Event.ONCONTEXTMENU) {
			event.stopPropagation();
			event.preventDefault();
			return;
		}
		super.onBrowserEvent(event);
	}

	public void up() {
		int current = this.choiceList.getSelectedIndex();
		int next = (current - 1 >= 0) ? current - 1 : 0;
		this.choiceList.setSelectedIndex(next);
		// Note that setting the selection programmatically does not cause the
		// ChangeHandler.onChange(ChangeEvent) event to be fired.
		// Doing it manually.
		this.onChange(null);
	}

	public void down() {
		int current = this.choiceList.getSelectedIndex();
		int next = (current + 1 < choiceList.getItemCount()) ? current + 1
				: current;
		this.choiceList.setSelectedIndex(next);
		// Note that setting the selection programmatically does not cause the
		// ChangeHandler.onChange(ChangeEvent) event to be fired.
		// Doing it manually.
		this.onChange(null);
	}

	public void select() {
		this.hide();
		int selected = choiceList.getSelectedIndex();
		if (listener != null) {
			if (selected == -1) {
				listener.noSuggestionSelected();
			} else {
				listener.suggestionSelected(visibleSuggs.get(selected));
			}
		}
	}

	/* @Override */
	public void onChange(ChangeEvent event) {

		if (descriptionPopup == null) {
			createDescriptionPopup();
		}

		int selected = choiceList.getSelectedIndex();
		String descr = visibleSuggs.get(selected).getDescriptionText();

		if (descr != null && !descr.isEmpty()) {
			((HTML) descriptionPopup.getWidget()).setHTML(descr);
			descriptionPopup.show();
		} else {
			descriptionPopup.hide();
		}
	}

	private void createDescriptionPopup() {
		descriptionPopup = new PopupPanel();
		descriptionPopup
				.setStylePrimaryName("codeeditor-suggestpopup-description");
		HTML lbl = new HTML();
		lbl.setWordWrap(true);
		descriptionPopup.setWidget(lbl);
		int x = getAbsoluteLeft() + WIDTH;
		int y = getAbsoluteTop();
		descriptionPopup.setPopupPosition(x, y);
	}

	public void setStartOfValue(String startOfValue) {
		this.startOfValue = startOfValue.toLowerCase();
		populateList();
		if (choiceList.getItemCount() == 0) {
			close();
		}
	}

}
