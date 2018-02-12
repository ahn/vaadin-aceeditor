package org.vaadin.aceeditor.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.ui.VOverlay;
import com.vaadin.client.widgets.Overlay;

public class SuggestPopup extends VOverlay implements KeyDownHandler, KeyPressHandler,
DoubleClickHandler, ChangeHandler {

	private static final Integer[] SPACES;
	private static final Integer[] PLUSES;

	static {
		SPACES = new Integer[10];
		PLUSES = new Integer[10];
		for (int i = 0 ; i < SuggestPopup.SPACES.length ; i++) {
			SuggestPopup.SPACES[i] = (10*i + 10);
			SuggestPopup.PLUSES[i] = 10*i;
		}
	}

	protected ListBox choiceList;

	protected String startOfValue = "";

	public interface SuggestionSelectedListener {
		void suggestionSelected(TransportSuggestion s);
		void noSuggestionSelected();
	}

	protected SuggestionSelectedListener listener;

	protected Overlay descriptionPopup;

	protected List<TransportSuggestion> suggs;
	protected List<VisibleSugg> visibleSuggs = new LinkedList<>();
	protected Set<String> visibleGroups = new HashSet<>();

	protected boolean showDescriptions = true;

	protected SimplePanel loadingImagePanel;
	protected Image loadingImage;

	public static int WIDTH = 150;
	public static int HEIGHT = 200;

	public static int DESCRIPTION_WIDTH = 225;

	public static class VisibleSugg {
		TransportSuggestion ts;
		String fullGroup;
		public VisibleSugg(final TransportSuggestion ts, final String fullGroup) {
			super();
			this.ts = ts;
			this.fullGroup = fullGroup;
		}
	}

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
		this.choiceList.addKeyPressHandler(this);
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
		this.suggs.sort(new Comparator<TransportSuggestion>() {
			@Override
			public int compare(final TransportSuggestion o1, final TransportSuggestion o2) {
				int ret = o1.group.compareTo(o2.group);
				if (ret == 0) {
					ret = o1.displayText.compareTo(o2.displayText);
				}
				return ret;
			}
		});
		this.createChoiceList();
		this.populateList();
		if (this.choiceList.getItemCount() == 0) {
			this.close();
		}
	}

	protected void populateList() {
		this.populateList(null);
	}

	/**
	 * @param group
	 * @return Number of elements in this group or -1 if null.
	 */
	protected int populateList(final String group) {
		int nb = group == null ? -1 : 0;
		this.choiceList.clear();
		this.visibleSuggs.clear();
		final List<Integer> styles = new ArrayList<>();
		int i = 0;
		String currentFullGroup = "";
		String[] currentGroupTree = {};
		for (final TransportSuggestion s : this.suggs) {
			if (s.suggestionText.toLowerCase().startsWith(this.startOfValue)) {
				if (! currentFullGroup.equals(s.group)) {
					final String[] groupTree = s.group.split("\\/");
					boolean same = true;
					String fullGroup = "";
					for (int igrp = 0 ; igrp < groupTree.length && ("".equals(fullGroup) || this.visibleGroups.contains(fullGroup)) ; ++igrp) {
						final String parentGroup = fullGroup;
						fullGroup += (igrp == 0 ? "" : "/") + groupTree[igrp];
						if (same && (currentGroupTree.length <= igrp || !currentGroupTree[igrp].equals(groupTree[igrp]))) {
							same = false;
						}
						if (!same) {
							this.visibleSuggs.add(new VisibleSugg(null, fullGroup));
							this.choiceList.addItem((this.visibleGroups.contains(fullGroup) ? '-' : '+') + groupTree[igrp], Integer.toString(i++));
							styles.add(SuggestPopup.PLUSES[igrp]);
							if (group != null && group.equals(parentGroup)) {
								++nb;
							}
						}
					}
					currentFullGroup = s.group;
					currentGroupTree = groupTree;
				}
				if ("".equals(currentFullGroup) || this.visibleGroups.contains(currentFullGroup)) {
					this.visibleSuggs.add(new VisibleSugg(s, currentFullGroup));
					this.choiceList.addItem(s.displayText, Integer.toString(i++));
					styles.add(SuggestPopup.SPACES[currentGroupTree.length]);
					if (group != null && group.equals(currentFullGroup)) {
						++nb;
					}
				}
			}
		}
		if (this.choiceList.getItemCount() > 0) {
			final int vic = Math.max(2, Math.min(10, this.choiceList.getItemCount()));
			this.choiceList.setVisibleItemCount(vic);
			this.choiceList.setSelectedIndex(0);
			this.onChange(null);
		}

		final SelectElement selectElement = SelectElement.as(this.choiceList.getElement());
		final NodeList<OptionElement> options = selectElement.getOptions();

		for (i = 0; i < options.getLength(); i++) {
			options.getItem(i).getStyle().setPaddingLeft(styles.get(i), Unit.PX);
		}
		return nb;
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
		} else if (keyCode == KeyCodes.KEY_RIGHT) {
			event.preventDefault();
			event.stopPropagation();
			this.openOrCloseGroup(true);
		} else if (keyCode == KeyCodes.KEY_LEFT) {
			event.preventDefault();
			event.stopPropagation();
			this.openOrCloseGroup(false);
		}
	}

	@Override
	public void onKeyPress(final KeyPressEvent event) {
		event.preventDefault();
		event.stopPropagation();
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

		if (this.listener != null) {
			final int selected = this.choiceList.getSelectedIndex();
			if (selected == -1) {
				this.hide();
				this.listener.noSuggestionSelected();
			} else {
				final VisibleSugg selectedSugg = this.visibleSuggs.get(selected);
				if (selectedSugg.ts == null) {
					//Group
					this.openOrCloseGroup(! this.visibleGroups.contains(selectedSugg.fullGroup));
				} else {
					//Not a group
					this.startLoading();
					this.listener.suggestionSelected(selectedSugg.ts);
				}
			}
		}
	}

	public void openOrCloseGroup(final boolean open) {
		if (this.suggs==null) {
			return;
		}

		final int selected = this.choiceList.getSelectedIndex();
		if (selected != -1) {
			final VisibleSugg selectedSugg = this.visibleSuggs.get(selected);
			if (selectedSugg.ts == null) {
				//Group
				boolean go = false;
				boolean close = false;
				if (!open && this.visibleGroups.contains(selectedSugg.fullGroup)) {
					this.visibleGroups.remove(selectedSugg.fullGroup);
					close = true;
					go = true;
				}
				if (open && !this.visibleGroups.contains(selectedSugg.fullGroup)) {
					this.visibleGroups.add(selectedSugg.fullGroup);
					go = true;
				}
				if (go) {
					final int scroll = this.choiceList.getElement().getScrollTop();
					final int nb = this.populateList(selectedSugg.fullGroup);

					//Set content of group visible at best.

					//Re scroll to last position or to 1 of no scroll: Trick so that it works in any case.
					if (scroll != 0) {
						this.choiceList.getElement().setScrollTop(scroll);
					} else {
						this.choiceList.getElement().setScrollTop(1);
					}
					if (this.choiceList.getElement().getScrollTop() != 0) {
						if (!close) {
							//Set last element of the group selected and scroll down if necessary
							this.choiceList.setSelectedIndex(selected+nb);
						}
						//Set select and scroll up if necessary
						this.choiceList.setSelectedIndex(selected);
					}
					this.onChange(null);
				}
			}
		}
	}

	@Override
	public void onChange(final ChangeEvent event) {
		if (this.descriptionPopup == null) {
			this.createDescriptionPopup();
		}

		final int selected = this.choiceList.getSelectedIndex();
		final VisibleSugg selectedSugg = this.visibleSuggs.get(selected);
		final String descr = selectedSugg.ts == null ? null : selectedSugg.ts.descriptionText;

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
		this.descriptionPopup = GWT.create(Overlay.class);
		this.descriptionPopup.setOwner(this.getOwner());
		this.descriptionPopup.setStylePrimaryName("aceeditor-suggestpopup-description");
		final HTML lbl = new HTML();
		lbl.setWordWrap(true);
		this.descriptionPopup.setWidget(lbl);
		this.updateDescriptionPopupPosition();
		this.descriptionPopup.setWidth(SuggestPopup.DESCRIPTION_WIDTH+"px");
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