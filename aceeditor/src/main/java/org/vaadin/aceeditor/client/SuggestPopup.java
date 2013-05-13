package org.vaadin.aceeditor.client;

import java.util.LinkedList;
import java.util.List;

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
import com.vaadin.client.ui.VOverlay;

public class SuggestPopup extends VOverlay implements KeyDownHandler,
		DoubleClickHandler, ChangeHandler {
	protected ListBox choiceList = new ListBox();

	private String startOfValue = "";

	public interface SuggestionSelectedListener {
		void suggestionSelected(Suggestion s);
		void noSuggestionSelected();
	}

	private SuggestionSelectedListener listener;

	private PopupPanel descriptionPopup;

	private List<Suggestion> suggs;
	private List<Suggestion> visibleSuggs = new LinkedList<Suggestion>();

	public static final int WIDTH = 100;
	public static final int HEIGHT = 200;

	public static final int DESCRIPTION_WIDTH = 150;

	// TODO addSuggestionSelectedListener?
	public void setSuggestionSelectedListener(SuggestionSelectedListener ssl) {
		listener = ssl;
	}

	public SuggestPopup() {
		super(true);

		this.startOfValue = startOfValue.toLowerCase();

		setWidth(WIDTH + "px");
		setHeight(HEIGHT + "px");
		add(choiceList);
		choiceList.setStyleName("list");
		choiceList.setVisibleItemCount(2);

		choiceList.addKeyDownHandler(this);
		choiceList.addDoubleClickHandler(this);

		choiceList.addChangeHandler(this);

		choiceList.setStylePrimaryName("aceeditor-suggestpopup-list");
	}
	
	public void setSuggestions(List<Suggestion> suggs) {
		this.suggs = suggs;
		populateList();
		if (choiceList.getItemCount() == 0) {
			close();
		}
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
		if (choiceList.getItemCount() > 0) {
			choiceList.setSelectedIndex(0);
			this.onChange(null);
		}
	}

	public void close() {
		hide();
		if (listener != null)
			listener.noSuggestionSelected();
	}

	@Override
	public void hide() {
		super.hide();
		if (descriptionPopup != null)
			descriptionPopup.hide();
		descriptionPopup = null;
	}

	@Override
	public void hide(boolean ac) {
		super.hide(ac);
		if (ac) {
			// This happens when user clicks outside this popup (or something
			// similar) while autohide is on. We must cancel the suggestion.
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
		if (suggs==null) {
			return;
		}
		int current = this.choiceList.getSelectedIndex();
		int next = (current - 1 >= 0) ? current - 1 : 0;
		this.choiceList.setSelectedIndex(next);
		// Note that setting the selection programmatically does not cause the
		// ChangeHandler.onChange(ChangeEvent) event to be fired.
		// Doing it manually.
		this.onChange(null);
	}

	public void down() {
		if (suggs==null) {
			return;
		}
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
		if (suggs==null) {
			return;
		}
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

	@Override
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
	
	
	@Override
	public void setPopupPosition(int left, int top) {
		super.setPopupPosition(left, top);
		if (descriptionPopup!=null) {
			updateDescriptionPopupPosition();
		}
	}
	
	private void updateDescriptionPopupPosition() {
		int x = getAbsoluteLeft() + WIDTH;
		int y = getAbsoluteTop();
		descriptionPopup.setPopupPosition(x, y);
		if (descriptionPopup!=null) {
			descriptionPopup.setPopupPosition(x, y);
		}
	}

	private void createDescriptionPopup() {
		descriptionPopup = new PopupPanel();
		descriptionPopup.setStylePrimaryName("aceeditor-suggestpopup-description");
		HTML lbl = new HTML();
		lbl.setWordWrap(true);
		descriptionPopup.setWidget(lbl);
		updateDescriptionPopupPosition();
		descriptionPopup.setSize(DESCRIPTION_WIDTH+"px", HEIGHT+"px");
	}

	public void setStartOfValue(String startOfValue) {
		this.startOfValue = startOfValue.toLowerCase();
		if (suggs==null) {
			return;
		}
		populateList();
		if (choiceList.getItemCount() == 0) {
			close();
		}
	}

}
