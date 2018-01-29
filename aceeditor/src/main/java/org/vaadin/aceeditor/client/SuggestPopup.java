package org.vaadin.aceeditor.client;

import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.ui.VOverlay;

public class SuggestPopup extends VOverlay implements KeyDownHandler,
DoubleClickHandler, ChangeHandler {
	protected ListBox choiceList;

	protected String startOfValue = "";

	public interface SuggestionSelectedListener {
		void suggestionSelected(TransportSuggestion s);
		void noSuggestionSelected();
	}

	protected SuggestionSelectedListener listener;

	protected VOverlay descriptionPopup;

	protected List<TransportSuggestion> suggs;
	protected List<TransportSuggestion> visibleSuggs = new LinkedList<>();

	protected boolean showDescriptions = true;

	protected SimplePanel loadingImagePanel;
	protected Image loadingImage;

	public static int WIDTH = 150;
	public static int HEIGHT = 200;

	public static int DESCRIPTION_WIDTH = 225;

	// TODO addSuggestionSelectedListener?
	public void setSuggestionSelectedListener(final SuggestionSelectedListener ssl) {
		this.listener = ssl;
	}

	public SuggestPopup() {
		super(true);
		this.setWidth(SuggestPopup.WIDTH + "px");
		final SuggestionResources resources = GWT.create(SuggestionResources.class);
		this.loadingImage = new Image(resources.loading());
		this.loadingImage.setSize("20px", "20px");
		this.loadingImagePanel = new SimplePanel(this.loadingImage);
		this.setWidget(this.loadingImagePanel);
	}

	protected void createChoiceList() {
		this.choiceList = new ListBox();
		this.choiceList.setStyleName("list");
		this.choiceList.addKeyDownHandler(this);
		this.choiceList.addDoubleClickHandler(this);
		this.choiceList.addChangeHandler(this);
		this.choiceList.setStylePrimaryName("aceeditor-suggestpopup-list");
		this.setWidget(this.choiceList);
	}

	protected void startLoading() {
		if (this.descriptionPopup!=null) {
			this.descriptionPopup.hide();
		}
		this.setWidget(this.loadingImagePanel);
	}

	public void setSuggestions(final List<TransportSuggestion> suggs) {
		this.suggs = suggs;
		this.createChoiceList();
		this.populateList();
		if (this.choiceList.getItemCount() == 0) {
			this.close();
		}
	}

	protected void populateList() {
		this.choiceList.clear();
		this.visibleSuggs.clear();
		int i = 0;
		for (final TransportSuggestion s : this.suggs) {
			if (s.suggestionText.toLowerCase().startsWith(this.startOfValue)) {
				this.visibleSuggs.add(s);
				this.choiceList.addItem(s.displayText, "" + i);
			}
			i++;
		}
		if (this.choiceList.getItemCount() > 0) {
			final int vic = Math.max(2, Math.min(10, this.choiceList.getItemCount()));
			this.choiceList.setVisibleItemCount(vic);
			this.choiceList.setSelectedIndex(0);
			this.onChange(null);
		}
	}

	public void close() {
		this.hide();
		if (this.listener != null) {
			this.listener.noSuggestionSelected();
		}
	}

	@Override
	public void hide() {
		super.hide();
		if (this.descriptionPopup != null) {
			this.descriptionPopup.hide();
		}
		this.descriptionPopup = null;
	}

	@Override
	public void hide(final boolean ac) {
		super.hide(ac);
		if (ac) {
			// This happens when user clicks outside this popup (or something
			// similar) while autohide is on. We must cancel the suggestion.
			if (this.listener != null) {
				this.listener.noSuggestionSelected();
			}
		}
		if (this.descriptionPopup != null) {
			this.descriptionPopup.hide();
		}
		this.descriptionPopup = null;
	}

	@Override
	public void onKeyDown(final KeyDownEvent event) {
		final int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_ENTER
				&& this.choiceList.getSelectedIndex() != -1) {
			event.preventDefault();
			event.stopPropagation();
			this.select();
		} else if (keyCode == KeyCodes.KEY_ESCAPE) {
			event.preventDefault();
			this.close();
		}
	}

	@Override
	public void onDoubleClick(final DoubleClickEvent event) {
		event.preventDefault();
		event.stopPropagation();
		this.select();
	}

	@Override
	public void onBrowserEvent(final Event event) {
		if (event.getTypeInt() == Event.ONCONTEXTMENU) {
			event.stopPropagation();
			event.preventDefault();
			return;
		}
		super.onBrowserEvent(event);
	}

	public void up() {
		if (this.suggs==null) {
			return;
		}
		final int current = this.choiceList.getSelectedIndex();
		final int next = (current - 1 >= 0) ? current - 1 : 0;
		this.choiceList.setSelectedIndex(next);
		// Note that setting the selection programmatically does not cause the
		// ChangeHandler.onChange(ChangeEvent) event to be fired.
		// Doing it manually.
		this.onChange(null);
	}

	public void down() {
		if (this.suggs==null) {
			return;
		}
		final int current = this.choiceList.getSelectedIndex();
		final int next = (current + 1 < this.choiceList.getItemCount()) ? current + 1
				: current;
		this.choiceList.setSelectedIndex(next);
		// Note that setting the selection programmatically does not cause the
		// ChangeHandler.onChange(ChangeEvent) event to be fired.
		// Doing it manually.
		this.onChange(null);
	}

	public void select() {
		if (this.suggs==null) {
			return;
		}

		final int selected = this.choiceList.getSelectedIndex();
		if (this.listener != null) {
			if (selected == -1) {
				this.hide();
				this.listener.noSuggestionSelected();
			} else {
				this.startLoading();
				this.listener.suggestionSelected(this.visibleSuggs.get(selected));
			}
		}
	}

	@Override
	public void onChange(final ChangeEvent event) {
		if (this.descriptionPopup == null) {
			this.createDescriptionPopup();
		}

		final int selected = this.choiceList.getSelectedIndex();
		final String descr = this.visibleSuggs.get(selected).descriptionText;

		if (descr != null && !descr.isEmpty()) {
			((HTML) this.descriptionPopup.getWidget()).setHTML(descr);
			if (this.showDescriptions) {
				this.descriptionPopup.show();
			}
		} else {
			this.descriptionPopup.hide();
		}
	}

	@Override
	public void setPopupPosition(final int left, final int top) {
		super.setPopupPosition(left, top);
		if (this.descriptionPopup!=null) {
			this.updateDescriptionPopupPosition();
		}
	}

	protected void updateDescriptionPopupPosition() {
		final int x = this.getAbsoluteLeft() + SuggestPopup.WIDTH;
		final int y = this.getAbsoluteTop();
		this.descriptionPopup.setPopupPosition(x, y);
		if (this.descriptionPopup!=null) {
			this.descriptionPopup.setPopupPosition(x, y);
		}
	}

	protected void createDescriptionPopup() {
		this.descriptionPopup = new VOverlay();
		this.descriptionPopup.setOwner(this.getOwner());
		this.descriptionPopup.setStylePrimaryName("aceeditor-suggestpopup-description");
		final HTML lbl = new HTML();
		lbl.setWordWrap(true);
		this.descriptionPopup.setWidget(lbl);
		this.updateDescriptionPopupPosition();
		this.descriptionPopup.setWidth(SuggestPopup.DESCRIPTION_WIDTH+"px");
		//		descriptionPopup.setSize(DESCRIPTION_WIDTH+"px", HEIGHT+"px");
	}

	public void setStartOfValue(final String startOfValue) {
		this.startOfValue = startOfValue.toLowerCase();
		if (this.suggs==null) {
			return;
		}
		this.populateList();
		if (this.choiceList.getItemCount() == 0) {
			this.close();
		}
	}

	public void setWidth(final int width){
		SuggestPopup.WIDTH = width;
		this.setWidth(width + "px");
	}

	public void setHeight(final int height){
		SuggestPopup.HEIGHT = height;
		this.setHeight(height + "px");
	}

	public void setDescriptionWidth(final int width){
		SuggestPopup.DESCRIPTION_WIDTH = width;
	}
}