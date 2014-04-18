package edu.ucla.cens.mobilize.client.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.TextAlign;
import com.google.gwt.canvas.dom.client.Context2d.TextBaseline;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SourcesTreeEvents;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.maps.client.MapOptions;
import com.google.gwt.maps.client.MapTypeId;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.InfoWindow;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.LatLngBounds;
import com.google.gwt.maps.client.event.Event;
import com.google.gwt.maps.client.event.EventCallback;
import com.google.gwt.maps.client.event.HasMapsEventListener;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerImage;
import com.ibm.icu.util.OverlayBundle;

import edu.ucla.cens.mobilize.client.common.EventLabel;
import edu.ucla.cens.mobilize.client.common.EventType;
import edu.ucla.cens.mobilize.client.common.LocationStatus;
import edu.ucla.cens.mobilize.client.common.MobilityMode;
import edu.ucla.cens.mobilize.client.common.PlotType;
import edu.ucla.cens.mobilize.client.common.Privacy;
import edu.ucla.cens.mobilize.client.model.AppConfig;
import edu.ucla.cens.mobilize.client.model.EventInfo;
import edu.ucla.cens.mobilize.client.model.MobilityChunkedInfo;
import edu.ucla.cens.mobilize.client.model.MobilityInfo;
import edu.ucla.cens.mobilize.client.model.SurveyResponse;
import edu.ucla.cens.mobilize.client.model.SurveyResponseData;
import edu.ucla.cens.mobilize.client.model.UserParticipationInfo;
import edu.ucla.cens.mobilize.client.ui.ErrorDialog;
import edu.ucla.cens.mobilize.client.ui.MobilityVizDailySummary;
import edu.ucla.cens.mobilize.client.ui.MobilityVizHistoricalAnalysis;
import edu.ucla.cens.mobilize.client.ui.MobilityWidgetPopup;
import edu.ucla.cens.mobilize.client.ui.ResponseWidgetPopup;
import edu.ucla.cens.mobilize.client.utils.DateUtils;
import edu.ucla.cens.mobilize.client.utils.EventUtils;
import edu.ucla.cens.mobilize.client.utils.MapUtils;
import edu.ucla.cens.mobilize.client.utils.MarkerClusterer;
import edu.ucla.cens.mobilize.client.utils.MobilityUtils;

@SuppressWarnings("deprecation")
public class ExploreDataViewImpl extends Composite implements ExploreDataView {

	private static ExploreDataViewUiBinder uiBinder = GWT
			.create(ExploreDataViewUiBinder.class);

	@UiTemplate("ExploreDataView.ui.xml")
	interface ExploreDataViewUiBinder extends UiBinder<Widget, ExploreDataViewImpl> {
	}

	public interface ExploreDataStyles extends CssResource {
		String disabled();
		String leaderBoardHeaderRow();
		String leaderBoardTotalsRow();
		String requiredField();
		String requiredFieldMissing();
		String treeItemCategory();
		String treeItemPlotType();
		String treeItemMap();
		String treeItemHist();
		String treeItemTimeseries();
		String treeItemTable();
		String waiting();
		String startarrow();
	}

	@UiField ExploreDataStyles style;
	@UiField DockLayoutPanel layoutPanel;
	@UiField VerticalPanel sideBar;
	@UiField Tree plotTypeTree;
	@UiField CaptionPanel dataControls;
	@UiField Label requiredFieldMissingMsg;
	@UiField Label campaignLabel;
	@UiField Label surveyLabel;
	@UiField Label classLabel;
	@UiField Label participantLabel;
	@UiField Label promptXLabel;
	@UiField Label promptYLabel;
	@UiField Label startDateLabel;
	@UiField Label endDateLabel;
	@UiField ListBox campaignListBox;
	@UiField ListBox surveyListBox;
	@UiField ListBox classListBox;
	@UiField ListBox participantListBox;
	@UiField ListBox promptXListBox;
	@UiField ListBox promptYListBox;
	@UiField DateBox dateStartBox;
	@UiField DateBox dateEndBox;
	@UiField Button drawPlotButton;
	//@UiField Button pdfButton;
	@UiField Button exportButton;
	@UiField HTMLPanel hiddenFormContainer;
	@UiField FlowPanel plotContainer;

	private List<ListBox> requiredFields = new ArrayList<ListBox>();
	private MapWidget mapWidget;
	private List<MapWidget> locationIDs = new ArrayList<MapWidget>();
	private final InfoWindow infoWindow;
	private List<HasMapsEventListener> clickHandlers;
	private MarkerClusterer markerClusterer;
	private Map<Marker, SurveyResponse> markerToResponseMap = new HashMap<Marker, SurveyResponse>();
	private Map<Marker, MobilityChunkedInfo> markerToMobilityChunkedMap = new HashMap<Marker, MobilityChunkedInfo>();
	private Map<Marker, MobilityInfo> markerToMobilityMap = new HashMap<Marker, MobilityInfo>();
	private Map<Marker, EventInfo> markerToLocationID = new HashMap<Marker, EventInfo>();
	private FlowPanel spinner; 
	private FlowPanel startarrow;

	public ExploreDataViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		loadPlotTypeTree();

		// make data filter panel stick to the bottom of the page
		sideBar.setCellVerticalAlignment(sideBar.getWidget(1), VerticalPanel.ALIGN_BOTTOM);

		// these are required when enabled
		requiredFields = Arrays.asList(campaignListBox, surveyListBox, classListBox, participantListBox, promptXListBox, promptYListBox);

		// set up date pickers
		final DateBox.Format fmt = new DateBox.DefaultFormat(DateUtils.getDateBoxDisplayFormat());

		dateStartBox.setFormat(fmt);
		dateEndBox.setFormat(fmt);
		dateStartBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Date s_new = event.getValue();
				Date e_old = getToDate();
				if (e_old == null || s_new.after(e_old)) {
					selectToDate(s_new);
				}
			}
		});
		dateEndBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Date e_new = event.getValue();
				Date s_old = getFromDate();
				if (s_old == null || e_new.before(s_old)) {
					selectFromDate(e_new);
				}
			}
		});

		// set up image to use as wait indicator
		spinner = new FlowPanel();
		spinner.setStyleName(style.waiting());

		// set up start arrow screen
		startarrow = new FlowPanel();
		startarrow.setStyleName(style.startarrow());

		// Single info window instance used by all markers
		infoWindow = InfoWindow.newInstance();
		clickHandlers = new ArrayList<HasMapsEventListener>();
	}


	// display text, value associated with item, css class name for wrapper span
	private TreeItem getTreeItem(String text, PlotType plotType, String cssStyle) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span class=").append(cssStyle).append(">").append(text).append("</span>");
		TreeItem treeItem = new TreeItem(sb.toString());
		if (plotType != null) treeItem.setUserObject(plotType);
		return treeItem;    
	}

	// use for tree item that won't have a value (i.e., category/parent nodes)
	private TreeItem getTreeItem(String text, String cssStyle) {
		return getTreeItem(text, null, cssStyle);
	}

	private void loadPlotTypeTree() {
		// NOTE(8/4/2011): plotType enum is converted to all lowercase and becomes the plot type argument
		// in the api call. So SURVEY_RESPONSE_COUNT becomes "/app/viz/survey_response_count/read..."
		// (See AndWellnessDataService.getVisualizationUrl())
		// NOTE(8/27/2011): LEADER_BOARD is a special case that does not query the api

		// response count 
		TreeItem surveyResponseCounts = getTreeItem("Survey Response Counts", style.treeItemCategory()); // category
		TreeItem totalResponses = AppConfig.exportAndVisualizeSharedResponsesOnly() ?  
				getTreeItem("Shared Responses", PlotType.SURVEY_RESPONSE_COUNT, style.treeItemPlotType()) :
				getTreeItem("Total Responses", PlotType.SURVEY_RESPONSE_COUNT, style.treeItemPlotType());
		TreeItem responsesByPrivacy = getTreeItem("Responses By Privacy", PlotType.SURVEY_RESPONSES_PRIVACY_STATE, style.treeItemPlotType());
		TreeItem responseTimeseries = getTreeItem("Response Timeseries", PlotType.SURVEY_RESPONSES_PRIVACY_STATE_TIME, style.treeItemPlotType());
		TreeItem leaderBoard = getTreeItem("Leader Board", PlotType.LEADER_BOARD, style.treeItemPlotType());

		// univariate 
		TreeItem univariate = getTreeItem("Single Variable", style.treeItemCategory());
		TreeItem userTimeseries = getTreeItem("User Timeseries", PlotType.USER_TIMESERIES, style.treeItemTimeseries());
		TreeItem promptTimeseries = getTreeItem("Prompt Timeseries", PlotType.PROMPT_TIMESERIES, style.treeItemTimeseries());
		TreeItem promptDistribution = getTreeItem("Prompt Distribution", PlotType.PROMPT_DISTRIBUTION, style.treeItemHist());

		// multivariate 
		TreeItem multivariate = getTreeItem("Multiple Variables", style.treeItemCategory()); // category
		TreeItem scatterplot = getTreeItem("Scatterplot", PlotType.SCATTER_PLOT, style.treeItemTable());
		TreeItem density = getTreeItem("2D Density Plot", PlotType.DENSITY_PLOT, style.treeItemTable());

		// geographic 
		TreeItem geographic = getTreeItem("Geographical", style.treeItemCategory()); // category
		TreeItem googleMap = getTreeItem("Google Map", PlotType.MAP, style.treeItemMap());

		// mobility
		TreeItem mobility = getTreeItem("Mobility", style.treeItemCategory()); // category
		TreeItem mobilityDashboard = getTreeItem("Daily Summary", PlotType.MOBILITY_DASHBOARD, style.treeItemMap());
		TreeItem mobilityMap = getTreeItem("Mobility Map", PlotType.MOBILITY_MAP, style.treeItemMap());
		TreeItem mobilityGraph = getTreeItem("Temporal Summary", PlotType.MOBILITY_TEMPORAL, style.treeItemMap());
		TreeItem mobilityHistorical = getTreeItem("Historical Analysis", PlotType.MOBILITY_HISTORICAL, style.treeItemMap());
		
		// events
//		TreeItem event = getTreeItem("Event", style.treeItemCategory()); // category
		TreeItem eventDashboard = getTreeItem("Event Dashboard", PlotType.EVENT_DASHBOARD, style.treeItemMap());
		
		
		// build the tree
		plotTypeTree.addItem(surveyResponseCounts);
		surveyResponseCounts.addItem(totalResponses);
		surveyResponseCounts.addItem(responsesByPrivacy);
		surveyResponseCounts.addItem(responseTimeseries);
		surveyResponseCounts.addItem(leaderBoard);
		plotTypeTree.addItem(univariate);
		univariate.addItem(userTimeseries);
		univariate.addItem(promptTimeseries);
		univariate.addItem(promptDistribution);
		plotTypeTree.addItem(multivariate);
		multivariate.addItem(scatterplot);
		multivariate.addItem(density);
		plotTypeTree.addItem(geographic);
		geographic.addItem(googleMap);
		if (AppConfig.getMobilityEnabled()) {
			plotTypeTree.addItem(mobility);
			mobility.addItem(mobilityDashboard);
			mobility.addItem(mobilityMap);
			mobility.addItem(mobilityGraph);
			mobility.addItem(mobilityHistorical);
		}
		plotTypeTree.addItem(eventDashboard);

		// add expand/fold click handler for tree's categories
		plotTypeTree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			int comingFromSetState = 0;
			boolean prevOpenState = true;

			@Override
			public void onSelection(SelectionEvent<TreeItem> event) {
				TreeItem item = event.getSelectedItem();

				//this expands/collapses the category on click
				//NOTE: this code is a workaround due to a bug in GWT's TreeItem
				if (item.getChildCount() == 0) {
					// Do nothing
				} else {
					if (comingFromSetState == 1 && prevOpenState) {
						comingFromSetState++;
					}
					if (comingFromSetState != 2) {
						comingFromSetState++;
						item.setState(!item.getState());
						prevOpenState = !item.getState();
					} else {
						comingFromSetState = 0;
						prevOpenState = true;
					}
				}
			}
		});
	}

	@Override
	public void setCampaignList(Map<String, String> campaignIdToNameMap) {
		if (campaignIdToNameMap == null) return;
		campaignListBox.clear();
		List<String> idsSortedByName = MapUtils.getKeysSortedByValues(campaignIdToNameMap);
		for (String campaignId : idsSortedByName) {
			campaignListBox.addItem(campaignIdToNameMap.get(campaignId), campaignId);
		}
	}

	@Override
	public void setSurveyList(List<String> surveyIds) {
		surveyListBox.clear();
	    if (surveyIds == null)
	    	return;
	    for (String name : surveyIds) {
	    	surveyListBox.addItem(name);
	    }
	    surveyListBox.setSelectedIndex(0);
	}

	@Override
	public void setSelectedCampaign(String campaignId) {
		campaignListBox.setSelectedIndex(-1);
		for (int i = 0; i < campaignListBox.getItemCount(); i++) {
			if (campaignListBox.getValue(i).equals(campaignId)) {
				campaignListBox.setSelectedIndex(i);
				break;
			}
		}
	}

	@Override
	public String getSelectedCampaign() {
		if (!campaignListBox.isEnabled()) return null;
		int index = campaignListBox.getSelectedIndex();
		return (index > -1) ? campaignListBox.getValue(index) : null;
	}

	@Override
	public void setSelectedSurvey(String surveyId) {
		surveyListBox.setSelectedIndex(-1);
		for (int i = 0; i < surveyListBox.getItemCount(); i++) {
			if (surveyListBox.getValue(i).equals(surveyId)) {
				surveyListBox.setSelectedIndex(i);
				break;
			}
		}
	}
	
	@Override
	public String getSelectedSurvey() {
		if (!surveyListBox.isEnabled()) return null;
		int index = surveyListBox.getSelectedIndex();
		return (index > -1) ? surveyListBox.getValue(index) : null;
	}

	@Override
	public void setClassList(List<String> classIds) {
		// Save old selection
		String oldClassId = this.getSelectedClass();
		
		classListBox.clear();
		classListBox.addItem("No class (just you)","");
		
		if (classIds == null)
			return;
		
		for (String classId : classIds)
			classListBox.addItem(classId, classId);
		
		// Try to set original class id
		this.setSelectedClass(oldClassId);
	}
	
	@Override
	public void setSelectedClass(String classId) {
		for (int i = 0; i < classListBox.getItemCount(); i++) {
			if (classListBox.getValue(i).equals(classId)) {
				classListBox.setSelectedIndex(i);
				return;
			}
		}

		// If not found, just select first item
		classListBox.setSelectedIndex(0);
	}

	@Override
	public String getSelectedClass() {
		if (!classListBox.isEnabled())
			return null;
		int index = classListBox.getSelectedIndex();
		return (index > -1) ? classListBox.getValue(index) : null;
	}
	
	@Override
	public void setParticipantList(List<String> participants) {
		// Save old selection
		String oldSelection = this.getSelectedParticipant();
		
		participantListBox.clear();

		if (participants == null) {
			return;
		}

		// add a multi-user option
		if (this.getSelectedPlotType() == PlotType.MAP)
			participantListBox.addItem("All Users", "");

		for (String username : participants)
			participantListBox.addItem(username, username);
		
		// Try to set original participant
		this.setSelectedParticipant(oldSelection);
	}


	@Override
	public void setSelectedParticipant(String participantUsername) {
		for (int i = 0; i < participantListBox.getItemCount(); i++) {
			if (participantListBox.getValue(i).equals(participantUsername)) {
				participantListBox.setSelectedIndex(i);
				return;
			}
		}

		// if not found, select first item
		participantListBox.setSelectedIndex(0);
	}


	@Override
	public String getSelectedParticipant() {
		if (!participantListBox.isEnabled()) return null;
		int index = participantListBox.getSelectedIndex();
		return (index > -1) ? participantListBox.getValue(index) : null;
	}

	// Returns true if a promptId is disabled in a listBox, false otherwise or invalid
	public boolean isPromptIdDisabled(ListBox listBox, String promptId) {
		//Determine index of prompt containing "value"
		boolean found = false;
		int itemIndex = 0;
		for ( ; itemIndex < listBox.getItemCount(); itemIndex++) {
			if (listBox.getValue(itemIndex) == promptId) {
				found = true;
				break;
			}
		}
		if (!found)
			return false;

		//See if "disabled" option is true or false
		NodeList<Element> items = listBox.getElement().getElementsByTagName("option");
		return (items.getItem(itemIndex).getAttribute("disabled") == "disabled");
	}

	@Override
	public void setSelectedPromptX(String promptId) {
		promptXListBox.setSelectedIndex(-1);
		for (int i = 0; i < promptXListBox.getItemCount(); i++) {
			if (promptXListBox.getValue(i).equals(promptId)) {
				if (isPromptIdDisabled(promptXListBox, promptId) == false)
					promptXListBox.setSelectedIndex(i);
				break;
			}
		}    
	}

	@Override
	public String getSelectedPromptX() {
		if (!promptXListBox.isEnabled()) return null;
		int index = promptXListBox.getSelectedIndex();
		return (index > -1) ? promptXListBox.getValue(index) : null;
	}

	@Override
	public void setSelectedPromptY(String promptId) {
		promptYListBox.setSelectedIndex(-1);
		for (int i = 0; i < promptYListBox.getItemCount(); i++) {
			if (promptYListBox.getValue(i).equals(promptId)) {
				if (isPromptIdDisabled(promptYListBox, promptId) == false)
					promptYListBox.setSelectedIndex(i);
				break;
			}
		}       
	}


	@Override
	public String getSelectedPromptY() {
		if (!promptYListBox.isEnabled()) return null;
		int index = promptYListBox.getSelectedIndex();
		return (index > -1) ? promptYListBox.getValue(index) : null;
	}


	@Override
	public PlotType getSelectedPlotType() {
		TreeItem selected = plotTypeTree.getSelectedItem();
		return (selected != null) ? (PlotType)selected.getUserObject() : null;
	}


	@Override
	public void setSelectedPlotType(PlotType plotType) {
		plotTypeTree.setSelectedItem(null);
		Iterator<TreeItem> iter = plotTypeTree.treeItemIterator();
		while (iter.hasNext()) {
			TreeItem curr = iter.next();
			PlotType treeItemPlotType = (PlotType)curr.getUserObject();
			if (treeItemPlotType != null && treeItemPlotType.equals(plotType)) {
				plotTypeTree.setSelectedItem(curr);
				break;
			}
		}
		plotTypeTree.ensureSelectedItemVisible();
	}

	@Override
	public void setPlotUrl(String url) {
		setPlotUrl(url, null); // no custom error handler
	}

	@Override
	public void setPlotUrl(String url, final ErrorHandler errorHandler) {
		clearPlot();
		//final Image loading = new Image();
		//loading.setStyleName(style.waiting());
		//plotContainer.add(loading);
		showWaitIndicator();
		Image plot = new Image(url);
		plot.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				hideWaitIndicator();
			}
		});
		plot.addErrorHandler(new ErrorHandler() {
			@Override
			public void onError(ErrorEvent event) {
				// get rid of the loading indicator and broken image
				clearPlot();
				// also call custom error handler, if given
				if (errorHandler != null) errorHandler.onError(event);
			}
		});

		plotContainer.add(plot);

	}

	@Override
	public void showWaitIndicator() {
		clearPlot();
		plotContainer.add(spinner);
	}

	@Override
	public void hideWaitIndicator() {
		plotContainer.remove(spinner);
	}

	@Override
	public void showStartArrow() {
		clearPlot();
		plotContainer.add(startarrow);
	}

	@Override
	public void hideStartArrow() {
		plotContainer.remove(startarrow);
	}

	@Override
	public void clearPlot() {
		plotContainer.clear();
	}

	@Override
	public void setCampaignDropDownEnabled(boolean isEnabled) {
		campaignLabel.setVisible(isEnabled);
		campaignListBox.setVisible(isEnabled);
		campaignListBox.setEnabled(isEnabled);
		setRequiredFlag(campaignListBox, isEnabled);
	}

	@Override
	public void setSurveyDropDownEnabled(boolean isEnabled) {
		surveyLabel.setVisible(isEnabled);
		surveyListBox.setVisible(isEnabled);
		surveyListBox.setEnabled(isEnabled);
		setRequiredFlag(surveyListBox, isEnabled);
	}
	
	@Override
	public void setClassDropDownEnabled(boolean isEnabled) {
		classLabel.setVisible(isEnabled);
		classListBox.setVisible(isEnabled);
		classListBox.setEnabled(isEnabled);
		setRequiredFlag(classListBox, isEnabled);
	}

	@Override
	public void setParticipantDropDownEnabled(boolean isEnabled) {
		participantLabel.setVisible(isEnabled);
		participantListBox.setVisible(isEnabled);
		participantListBox.setEnabled(isEnabled);
		setRequiredFlag(participantListBox, isEnabled);
	}

	@Override
	public void setPromptXDropDownEnabled(boolean isEnabled) {
		promptXLabel.setVisible(isEnabled);
		promptXListBox.setVisible(isEnabled);
		promptXListBox.setEnabled(isEnabled);
		setRequiredFlag(promptXListBox, isEnabled);
	}

	@Override
	public void setPromptYDropDownEnabled(boolean isEnabled) {
		promptYLabel.setVisible(isEnabled);
		promptYListBox.setVisible(isEnabled);
		promptYListBox.setEnabled(isEnabled);
		setRequiredFlag(promptYListBox, isEnabled);
	}

	@Override
	public void setDateRangeEnabled(boolean isEnabled) {
		setStartDateRangeEnabled(isEnabled);
		setEndDateRangeEnabled(isEnabled);
	}

	@Override
	public void setStartDateRangeEnabled(boolean isEnabled) {
		startDateLabel.setVisible(isEnabled);
		dateStartBox.setVisible(isEnabled);
		dateStartBox.setEnabled(isEnabled);
		setRequiredFlag(dateStartBox, isEnabled);
		if (isEnabled == false) {
			dateStartBox.setValue(null);
		}
	}

	@Override
	public void setEndDateRangeEnabled(boolean isEnabled) {
		endDateLabel.setVisible(isEnabled);
		dateEndBox.setVisible(isEnabled);
		dateEndBox.setEnabled(isEnabled);
		setRequiredFlag(dateEndBox, isEnabled);
		if (isEnabled == false) {
			startDateLabel.setText("Date:");			//01/06/2012: temporary fix. explore data controls will be overhauled in next release
		} else {
			startDateLabel.setText("Start Date:");	//01/06/2012: see note above
		}
	}

	@Override
	public void setExportButtonEnabled(boolean isEnabled) {
		exportButton.setVisible(isEnabled);
		exportButton.setEnabled(isEnabled);
	}

	@Override
	public void disableAllDataControls() {
		campaignListBox.setSelectedIndex(-1); // campaigns never change, just deselect
		surveyListBox.clear();
		classListBox.setSelectedIndex(0);	// classes never change, just deselect
		participantListBox.clear();
		promptXListBox.clear();
		promptYListBox.clear();

		// disable control
		campaignListBox.setEnabled(false);
		surveyListBox.setEnabled(false);
		classListBox.setEnabled(false);
		participantListBox.setEnabled(false);
		promptXListBox.setEnabled(false);
		promptYListBox.setEnabled(false);
		dateStartBox.setEnabled(false);
		dateEndBox.setEnabled(false);
		drawPlotButton.setEnabled(false);
		//pdfButton.setEnabled(false);
		exportButton.setEnabled(false);

		// remove style name that marks control as required
		clearMissingFieldMarkers();
		for (ListBox listBox : requiredFields) {
			setRequiredFlag(listBox, false);
		}

		dataControls.addStyleName(style.disabled());
	}

	@Override
	public void setDataButtonsEnabled(boolean isEnabled) {
		dataControls.removeStyleName(style.disabled());
		drawPlotButton.setEnabled(isEnabled);
		//pdfButton.setEnabled(isEnabled);
		exportButton.setEnabled(isEnabled);
	}

	@Override 
	public HasChangeHandlers getCampaignDropDown() {
		return campaignListBox;
	}

	@Override
	public HasChangeHandlers getClassDropDown() {
		return classListBox;
	}

	@Override
	public HasClickHandlers getDrawPlotButton() {
		return drawPlotButton;
	}

	/*
  @Override
  public HasClickHandlers getPdfButton() {
    return pdfButton;
  }
	 */

	@Override
	public HasClickHandlers getExportDataButton() {
		return exportButton;
	}

	@Override
	public SourcesTreeEvents getPlotTypeTree() {
		return plotTypeTree;
	}


	@Override
	public int getPlotPanelWidth() {
		return plotContainer.getElement().getClientWidth();
	}


	@Override
	public int getPlotPanelHeight() {
		return plotContainer.getElement().getClientHeight();
	}


	@Override
	public boolean isMissingRequiredField() { 
		clearMissingFieldMarkers(); // clear any left over from last validation
		boolean atLeastOneFieldIsMissing = false;
		for (ListBox listBox : requiredFields) {
			if (listBox.getSelectedIndex() == -1 && isRequired(listBox)) {
				markMissing(listBox);
				atLeastOneFieldIsMissing = true;
			}
		}

		if (atLeastOneFieldIsMissing) {
			requiredFieldMissingMsg.setVisible(true);      
		}

		return atLeastOneFieldIsMissing;
	}

	@Override
	public void clearMissingFieldMarkers() {
		for (ListBox listBox : requiredFields) {
			clearMissingMarker(listBox);
		}
		requiredFieldMissingMsg.setVisible(false);      
	}

	private boolean isRequired(UIObject field) {
		return field.getStyleName().contains(style.requiredField());
	}

	private void markMissing(UIObject field) {
		field.addStyleName(style.requiredFieldMissing());
	}

	private void clearMissingMarker(UIObject field) {
		field.removeStyleName(style.requiredFieldMissing());
	}

	// used for both styling and validation
	private void setRequiredFlag(UIObject field, boolean isRequired) {
		if (isRequired) {
			field.addStyleName(style.requiredField());
		} else {
			field.removeStyleName(style.requiredField());
		}
	}


	@Override
	public void showResponsesOnMap(final List<SurveyResponse> responses) {
		// hide previous plot, if any
		clearPlot(); 

		// add responses to map, attach it to the document to make it visible
		if (mapWidget == null) { // lazy init map, add responses when done
			initMap(new Runnable() {
				@Override
				public void run() {
					setResponsesOnMap(responses);
					hideWaitIndicator();
				}
			});
		} else { // map already initialized
			setResponsesOnMap(responses); 
		}
	}

	private void setResponsesOnMap(List<SurveyResponse> responses) {

		// Clear any previous data points    
		clearOverlays();

		// Show error message if campaign has no user response data for map plotting
		if (responses == null || responses.isEmpty()) {
			String user = this.getSelectedParticipant();
			if (user == null || user.isEmpty())
				ErrorDialog.show("This campaign has no user responses for the selected parameters.");
			else
				ErrorDialog.show("The user \'" + user + "\' does not have any geo location data.");
			return;
		}

		List<Marker> markers = new ArrayList<Marker>();	// markers to add to MarkerClusterer
		LatLngBounds bounds = LatLngBounds.newInstance();

		boolean hasPlottableData = false;

		// Add new data points 
		for (SurveyResponse response : responses) {
			if (response.hasLocation()) {
				final LatLng location = LatLng.newInstance(response.getLatitude(), response.getLongitude());
				bounds.extend(location);
				final Marker marker = Marker.newInstance();
				marker.setPosition(location);
				//marker.setMap(mapWidget.getMap());	//*** old ***
				markers.add(marker);	// instead of rendering the marker directly, add it to our list
				markerToResponseMap.put(marker, response);

				Event.addListener(marker, "click", new EventCallback() {
					@Override
					public void callback() {
						showResponseDetail(marker);
					}
				});

				hasPlottableData = true;
			}
		}

		if (hasPlottableData == false) {
			String user = this.getSelectedParticipant();
			ErrorDialog.show("The user \'" + user + "\' does not have any plottable responses.");
		}

		// pass markers list to MarkerClusterer for clustered rendering
		markerClusterer = MarkerClusterer.newInstance(mapWidget.getMap(), markers.toArray(new Marker[markers.size()]), true);

		// Attach map before calculating zoom level or it might be incorrectly set to 0 (?)
		if (!mapWidget.isAttached()) plotContainer.add(mapWidget);

		// Zoom and center the map to the new bounds
		mapWidget.getMap().fitBounds(bounds); 
	}


	@Override
	public void showMobilityDataOnMap(final List<MobilityInfo> mdata) {
		// hide previous plot, if any
		clearPlot(); 

		// add responses to map, attach it to the document to make it visible
		if (mapWidget == null) { // lazy init map, add responses when done
			initMap(new Runnable() {
				@Override
				public void run() {
					drawMobilityDataOnMap(mdata);
					hideWaitIndicator();
				}
			});
		} else { // map already initialized
			drawMobilityDataOnMap(mdata);
		}
	}
	
	public void showLocationIDOnMap(final List<EventInfo> mdata, final VerticalPanel panel, final Map<String, String> locColors) {
		// hide previous plot, if any
//		clearPlot(); 

		// add responses to map, attach it to the document to make it visible
//		if (mapWidget == null) { // lazy init map, add responses when done
		List<EventInfo> distinct = new ArrayList<EventInfo>();
		List<String> seenLabels = new ArrayList<String>();
		for (EventInfo m : mdata)
//			if (!seenLabels.contains(m.getEventLabel()))
			{
				seenLabels.add(m.getEventLabel());
				distinct.add(m);
			}
//		int index = 0;
//		for (final EventInfo m : distinct)
		for (int i = 0; i < distinct.size(); i++)
		{
			final EventInfo m = distinct.get(i);
			final int index = i;
			initLocationIDMap(new Runnable() {
		
				@Override
				public void run() {
					drawLocationIDOnMap(m, panel, locColors, "location_event_" + index);
//					hideWaitIndicator();
				}
			});
		}
//		} else { // map already initialized
//			drawLocationIDOnMap(mdata, panel);
//		}
	}

	@Override
	public void showMobilityDashboard(final List<MobilityInfo> mdata) {
		MobilityVizDailySummary widget = new MobilityVizDailySummary(mdata);
		plotContainer.add(widget);
	}

	private void drawMobilityDataOnMap(final List<MobilityInfo> mdata) {
		// Clear any previous data points    
		clearOverlays();

		// Show error message if user has no mobility data
		if (mdata == null || mdata.isEmpty()) {
			ErrorDialog.show("Sorry, we couldn't find any mobility data for the selected date(s).");
			return;
		}
		boolean hasPlottableData = false;
		for (MobilityInfo m : mdata) {
			if (m.getLocationStatus() != LocationStatus.UNAVAILABLE) {
				hasPlottableData = true;
				break;
			}
		}
		if (hasPlottableData == false) {
			ErrorDialog.show("Sorry, we couldn't find any mobility data with geolocations for the selected date(s).");
			return;
		}

		List<Marker> markers = new ArrayList<Marker>();	// markers to add to MarkerClusterer
		LatLngBounds bounds = LatLngBounds.newInstance();

		// Add new data points 
		for (int i = 0; i < mdata.size(); i++) {
			MobilityInfo m = mdata.get(i);

			if (m.getLocationStatus() != LocationStatus.UNAVAILABLE) {
				final LatLng location = LatLng.newInstance(m.getLocationLat(), m.getLocationLong());
				bounds.extend(location);

				final Marker marker = Marker.newInstance();
				marker.setPosition(location);
				//marker.setMap(mapWidget.getMap());	//*** old ***
				markers.add(marker);	// instead of rendering the marker directly, add it to our list

				// Select mobility mode for icon
				MobilityMode mode = m.getMode();

				// Pick marker corresponding to mode 
				// NOTE: To support MarkerClusterer hover info's, we store data into the title
				try {
					//NOTE: THIS DURATION ESTIMATION IS EXTREMELY HACKY/DANGEROUS
					int duration = 5;
					if (i+1 < mdata.size()) {
						duration = MobilityUtils.getTimeInMinutes(mdata.get(i+1).getDate()) - MobilityUtils.getTimeInMinutes(mdata.get(i).getDate());
						if (duration <= 0 || duration > 5)
							duration = 5;
					}
					marker.set("mobility_duration", duration);

					// Pick mobility icon to display
					MarkerImage.Builder imgBuilder;
					if (mode == MobilityMode.STILL) {
						marker.setTitle("still");
						imgBuilder = new MarkerImage.Builder("images/mobility/m_still.png");
					} else if (mode == MobilityMode.WALK) {
						marker.setTitle("walk");
						imgBuilder = new MarkerImage.Builder("images/mobility/m_walk.png");
					} else if (mode == MobilityMode.RUN) {
						marker.setTitle("run");
						imgBuilder = new MarkerImage.Builder("images/mobility/m_run.png");
					} else if (mode == MobilityMode.BIKE) {
						marker.setTitle("bike");
						imgBuilder = new MarkerImage.Builder("images/mobility/m_bike.png");
					} else if (mode == MobilityMode.DRIVE) {
						marker.setTitle("drive");
						imgBuilder = new MarkerImage.Builder("images/mobility/m_drive.png");
					} else { // "ERROR" or unknown
						marker.setTitle("error");
						imgBuilder = new MarkerImage.Builder("images/mobility/m_error.png");
					}
					marker.setIcon(imgBuilder.build());
				} catch (Exception e) {
					//do nothing
				}

				markerToMobilityMap.put(marker, m);
				

				Event.addListener(marker, "click", new EventCallback() {
					@Override
					public void callback() {
						showMobilityDetail(marker);
					}
				});
			}
		}

		// pass markers list to MarkerClusterer for clustered rendering
		markerClusterer = MarkerClusterer.newInstance(mapWidget.getMap(), markers.toArray(new Marker[markers.size()]), false);

		// Attach map before calculating zoom level or it might be incorrectly set to 0 (?)
		if (!mapWidget.isAttached()) plotContainer.add(mapWidget);

		// Zoom and center the map to the new bounds
		mapWidget.getMap().fitBounds(bounds); 
	}
	
	private void drawLocationIDOnMap(final EventInfo ei, VerticalPanel panel, Map<String, String> locColors, String name) {
		// Clear any previous data points    
//		clearOverlays();

		// Show error message if user has no mobility data
		if (ei == null) {
			ErrorDialog.show("Sorry, we couldn't find any mobility data for the selected date(s).");
			return;
		}
		boolean hasPlottableData = false;

		List<Marker> markers = new ArrayList<Marker>();	// markers to add to MarkerClusterer
		LatLngBounds bounds = LatLngBounds.newInstance();

		// Add new data points 
//		for (int i = 0; i < 1; i++){//list.size(); i++) {
//			EventInfo m = list.get(0);

//			if (m.getLocationStatus() != LocationStatus.UNAVAILABLE) 
			
			{
				final LatLng location = LatLng.newInstance(ei.getLatitude(), ei.getLongitude());
//				final LatLng location = LatLng.newInstance(34.164163, -118.767289);
				
				bounds.extend(location);
				
				

//				final LatLng left = LatLng.newInstance(34.164163 + , lng)
//				bounds.extend(left);
				
				
				
				final Marker marker = Marker.newInstance();
				marker.setPosition(location);
//				marker.set();
				//marker.setMap(mapWidget.getMap());	//*** old ***
				markers.add(marker);	// instead of rendering the marker directly, add it to our list
				
				marker.setMap(locationIDs.get(locationIDs.size() - 1).getMap());
				
				markerToLocationID.put(marker, ei);

			}
//		}
//			String name = 
			Label locLabel = new Label(ei.getEventLabel());
			String htmlStr = "<span style='color: "+ locColors.get(ei.getEventLabel())  +"; font-size: 20pt'>"+SafeHtmlUtils.htmlEscape(ei.getEventLabel())+"</span>";
			HTML html = new HTML(htmlStr);
			locLabel = html;
			HorizontalPanel eventPanel = new HorizontalPanel();
			eventPanel.add(locLabel);
			eventPanel.add(locationIDs.get(locationIDs.size() - 1));
			// form response
			Label routineQuestion = new Label("Is this a typical event in your routine?");
			RadioButton radioButtonYes = new RadioButton("routineGroup"+ ei.getEventLabel(), "Yes");
		    RadioButton radioButtonNo = new RadioButton("routineGroup"+ ei.getEventLabel(), "No");
		    VerticalPanel routinePanel = new VerticalPanel();
		    routinePanel.add(routineQuestion);
		    routinePanel.add(radioButtonYes);
		    routinePanel.add(radioButtonNo);
		    eventPanel.add(routinePanel);
		    
		    Label boundaryQuestion = new Label("If it's a routine event, are the beginning and end times typical?");
		    CheckBox checkBoxBegin = new CheckBox("Beginning");
		    CheckBox checkBoxEnd = new CheckBox("End");
		    VerticalPanel boundaryPanel = new VerticalPanel();
		    boundaryPanel.add(boundaryQuestion);
		    boundaryPanel.add(checkBoxBegin);
		    boundaryPanel.add(checkBoxEnd);
		    eventPanel.add(boundaryPanel);
			
			Label otherQuestions = new Label("Are there any problems with this event as detected?");
			
			
			
			panel.add(eventPanel);
//		if (!locationIDs.get(locationIDs.size() - 1).isAttached()) gpanel.add(locationIDs.get(locationIDs.size() - 1));
//		plotContainer.add(gpanel);
		// Zoom and center the map to the new bounds
//		locationIDs.get(locationIDs.size() - 1).getMap().fitBounds(bounds);
			locationIDs.get(locationIDs.size() - 1).getMap().setCenter(LatLng.newInstance(ei.getLatitude(), ei.getLongitude()));;
		locationIDs.get(locationIDs.size() - 1).getMap().setZoom(zoomlevel);
	}
	final int zoomlevel = 15;
	@Override
	public void showMobilityTemporalSummary(final List<List<MobilityInfo>> mdataList) {
		// hide previous plot, if any
		clearPlot(); 
		hideWaitIndicator();

		final VerticalPanel panels = new VerticalPanel();
		final int interval = 5;	// minutes

		for (int i = 0; i < mdataList.size(); i++) {
			List<MobilityInfo> mdata = mdataList.get(i);
			List<MobilityMode> buckets = MobilityUtils.bucketByInterval(mdata, interval);
			List<MobilityMode> buckets1 = MobilityUtils.bucketByInterval(mdata, 1);
			
			DateTimeFormat format = DateTimeFormat.getFormat("EEEE, MMMM dd, yyyy");
			String day_str = format.format(DateUtils.addDays(getFromDate(), i));

			Label date_label = new Label(day_str);
			panels.add(date_label);

			Widget testViz = MobilityUtils.createMobilityBarChartCanvasWidget(buckets, interval, 750, 120, true, true);
//			Widget viz2 = MobilityUtils.createMobilityBarChartCanvasWidget(buckets1, 1, 750, 120, true, true);
			
			panels.add(testViz);
//			panels.add(viz2);
		}

		plotContainer.add(panels);
	}
	
	@Override
	public void showEventsWithTimeline(final List<EventInfo> mdataList) {
		// hide previous plot, if any
		clearPlot();
		hideWaitIndicator();

		final VerticalPanel panels = new VerticalPanel();
		
		final int interval = 5;	// minutes
		HashMap<EventType, List<EventLabel>> buckets = new HashMap<EventType, List<EventLabel>>();//  EventUtils.bucketByInterval(mdata, interval);
		
		EventType[] allTypes = new EventType [] { EventType.ACTIVITY, EventType.APP, EventType.CALL, EventType.SMS, EventType.LOCATION };
		HashMap<EventType, HashMap<String, EventInfo>> labelMaps = new HashMap<EventType, HashMap<String, EventInfo>>();
		for (EventType type : allTypes)
		{
			buckets.put(type, new ArrayList<EventLabel>());
			labelMaps.put(type, new HashMap<String, EventInfo>());
		}
//		buckets.put(EventType.LOCATION, new ArrayList<EventLabel>());
//		buckets.put(EventType.SMS, new ArrayList<EventLabel>());
//		buckets.put(EventType.CALL, new ArrayList<EventLabel>());
//		buckets.put(EventType.APP, new ArrayList<EventLabel>());
		
		
		
		HashMap<EventType, List<EventInfo>> mdataAsLists = new HashMap<EventType, List<EventInfo>>();
		for (int i = 0; i < mdataList.size(); i++) {
			EventInfo mdata = mdataList.get(i);
			if (mdata.getEventLabel() == null)
				mdata = null;
			// make all legend info available by name
			if (!labelMaps.containsKey(mdata.getType()))
				labelMaps.put(mdata.getType(), new HashMap<String, EventInfo>());
			if (!labelMaps.get(mdata.getType()).containsKey(mdata.getEventLabel().toString()))
				labelMaps.get(mdata.getType()).put(mdata.getEventLabel().toString(), mdata);
			if (!mdataAsLists.containsKey(mdata.getType()))
				mdataAsLists.put(mdata.getType(), new ArrayList<EventInfo>());
			// make sure bucket list of this type of event is available
			if (!buckets.containsKey(mdata.getType()))
				buckets.put(mdata.getType(), new ArrayList<EventLabel>());
			
			// use list of one thing so I can change to multiple before changing the current way of doing it
			
			mdataAsLists.get(mdata.getType()).add(mdata);
		}
//		if (mdata.getType().equals(EventType.LOCATION)) // TODO REMOVE
			buckets.get(EventType.LOCATION).addAll(EventUtils.bucketByInterval(mdataAsLists.get(EventType.LOCATION), interval));
			buckets.get(EventType.ACTIVITY).addAll(EventUtils.bucketByInterval(mdataAsLists.get(EventType.ACTIVITY), interval));
			buckets.get(EventType.APP).addAll(EventUtils.bucketByInterval(mdataAsLists.get(EventType.APP), interval));
			buckets.get(EventType.SMS).addAll(EventUtils.bucketByInterval(mdataAsLists.get(EventType.SMS), interval));
			buckets.get(EventType.CALL).addAll(EventUtils.bucketByInterval(mdataAsLists.get(EventType.CALL), interval));
		
		DateTimeFormat format = DateTimeFormat.getFormat("EEEE, MMMM dd, yyyy");
		String day_str = format.format(DateUtils.addDays(getFromDate(), 1));
		Label date_label = new Label(day_str);
		panels.add(date_label);
		Map<String, String> locColors = EventUtils.getEventColorMap(labelMaps.get(EventType.LOCATION));
		Widget locationViz = EventUtils.createLocationEventsBarChartCanvasWidget(buckets.get(EventType.LOCATION), interval, 750, 120, true, true, labelMaps.get(EventType.LOCATION), locColors);
		
//			Widget viz2 = MobilityUtils.createMobilityBarChartCanvasWidget(buckets1, 1, 750, 120, true, true);
		Widget activityViz = EventUtils.createActivityEventsBarChartCanvasWidget(buckets.get(EventType.ACTIVITY), interval, 750, 120, true, true, labelMaps.get(EventType.ACTIVITY));
		Widget appViz = EventUtils.createActivityEventsBarChartCanvasWidget(buckets.get(EventType.APP), interval, 750, 120, true, true, labelMaps.get(EventType.APP));
		Widget smsViz = EventUtils.createActivityEventsBarChartCanvasWidget(buckets.get(EventType.SMS), interval, 750, 120, true, true, labelMaps.get(EventType.SMS));
		Widget callViz = EventUtils.createActivityEventsBarChartCanvasWidget(buckets.get(EventType.CALL), interval, 750, 120, true, true, labelMaps.get(EventType.CALL));
		
		// Event identification thingies
		// location
		final HashMap<EventType, List<EventInfo>> mdataFinal = mdataAsLists;
		
		// TODO foreach
		
		
		
//		if (mapWidget == null) { // lazy init map, add responses when done
//			initMap(new Runnable() {
//				@Override
//				public void run() {
//					drawLocationIDOnMap(mdataFinal.get(EventType.LOCATION));
//					hideWaitIndicator();
//				}
//			});
//		} else { // map already initialized
//			drawLocationIDOnMap(mdataAsLists.get(EventType.LOCATION));
//		}
		
		VerticalPanel locPanel = new VerticalPanel();

		if (locationViz != null)
		{
			panels.add(locationViz);
			showLocationIDOnMap(mdataFinal.get(EventType.LOCATION), locPanel, locColors);
			if (!locPanel.isAttached())
			{
//				clearOverlays(); // TODO do we need this?
				panels.add(locPanel);
			}
			
		}
		if (activityViz != null)
			panels.add(activityViz);
		if (appViz != null)
			panels.add(appViz);
		if (smsViz != null)
			panels.add(smsViz);
		if (callViz != null)
			panels.add(callViz);
//			panels.add(viz2);
//		}
		plotContainer.add(panels);
//		if (gpanel == null)
//			gpanel = new HorizontalPanel();
		
			
		
		
	}
//	final HorizontalPanel gpanel = new HorizontalPanel();

	@Override
	public void showMobilityHistoricalAnalysis(List<List<MobilityInfo>> multiDayMobilityDataList) {
		MobilityVizHistoricalAnalysis widget = new MobilityVizHistoricalAnalysis(multiDayMobilityDataList);
		plotContainer.add(widget);
	}
	
	@Override
	public void renderLeaderBoard(List<UserParticipationInfo> participationInfo) {
		clearPlot();
		Grid leaderBoard = new Grid();
		int numRows = participationInfo.size() + 2; // + 2 for header row at top + totals row at bottom
		int numCols = 4; // username, total, private, shared // FIXME: invisible?
		leaderBoard.resize(numRows, numCols);
		leaderBoard.setText(0, 0, "Username");
		leaderBoard.setText(0, 1, "Total Responses");
		leaderBoard.setText(0, 2, "Private Responses");
		leaderBoard.setText(0, 3, "Shared Responses");
		leaderBoard.getRowFormatter().addStyleName(0, style.leaderBoardHeaderRow());
		int row = 1; // first row is header
		int totalResponsesFromAllUsers = 0;
		int totalPrivateResponsesFromAllUsers = 0;
		int totalSharedResponsesFromAllUsers = 0;
		for (UserParticipationInfo info : participationInfo) {
			// get response counts for this user
			int totalResponseCount = info.getTotalResponseCount();
			int privateResponseCount = info.getResponseCount(Privacy.PRIVATE);
			int sharedResponseCount = info.getResponseCount(Privacy.SHARED);
			// fill in user info row in leader board
			leaderBoard.setText(row, 0, info.getUsername());
			leaderBoard.setText(row, 1, Integer.toString(totalResponseCount));
			leaderBoard.setText(row, 2, Integer.toString(privateResponseCount));
			leaderBoard.setText(row, 3, Integer.toString(sharedResponseCount));
			// add this user's counts to running totals
			totalResponsesFromAllUsers += totalResponseCount;
			totalPrivateResponsesFromAllUsers += privateResponseCount;
			totalSharedResponsesFromAllUsers += sharedResponseCount;
			// increment row
			row++;
		}
		// insert row of totals at the end
		leaderBoard.setText(row, 0, "Total (All Users)");
		leaderBoard.setText(row, 1, Integer.toString(totalResponsesFromAllUsers));
		leaderBoard.setText(row, 2, Integer.toString(totalPrivateResponsesFromAllUsers));
		leaderBoard.setText(row, 3, Integer.toString(totalSharedResponsesFromAllUsers));
		leaderBoard.getRowFormatter().addStyleName(row, style.leaderBoardTotalsRow());

		// add widget to the display
		plotContainer.add(leaderBoard);
	}

	@Override
	public void setInfoText(String text) {
		// TODO: display info about current plot
	}

	/**
	 * Clears the markers one by one from the map.
	 */
	private void clearOverlays() {
		if (markerClusterer != null) {
			markerClusterer.clearMarkers();
		}

		// Clear response map markers
		for (final Marker marker: markerToResponseMap.keySet()) {
			marker.setMap(null); // Remove from map
			Event.clearInstanceListeners(marker); // Remove the event listener
		}
		markerToResponseMap.clear();

		// Clear mobility chunked map markers
		for (final Marker marker: markerToMobilityChunkedMap.keySet()) {
			marker.setMap(null); // Remove from map
			Event.clearInstanceListeners(marker); // Remove the event listener
		}
		markerToMobilityChunkedMap.clear();

		// Clear mobility map markers
		for (final Marker marker: markerToMobilityMap.keySet()) {
			marker.setMap(null); // Remove from map
			Event.clearInstanceListeners(marker); // Remove the event listener
		}
		markerToMobilityMap.clear();
		
		for (final Marker marker: markerToLocationID.keySet()) {
			marker.setMap(null); // Remove from map
			Event.clearInstanceListeners(marker); // Remove the event listener
		}
		markerToLocationID.clear();
	}

	private void initMap(final Runnable actionToTakeWhenDone) {
		final MapOptions options = new MapOptions();
		options.setMapTypeControl(true);
		options.setZoom(8);
		options.setCenter(LatLng.newInstance(39.509, -98.434));
		options.setMapTypeId(new MapTypeId().getRoadmap());
		options.setDraggable(true);
		options.setScaleControl(true);
		options.setNavigationControl(true);
		options.setScrollwheel(true);
		mapWidget = new MapWidget(options);
		mapWidget.setSize("100%", "100%");

		// Close the info window when clicking anywhere
		Event.addListener(mapWidget.getMap(), "click", new EventCallback() {
			@Override
			public void callback() {
				closeInfoWindow();
			}
		});

		// Close the info window when clicking close
		Event.addListener(infoWindow, "closeclick", new EventCallback() {
			@Override
			public void callback() {
				closeInfoWindow();
			}
		});

		if (actionToTakeWhenDone != null) actionToTakeWhenDone.run();
	}
	
	private void initLocationIDMap(final Runnable actionToTakeWhenDone) {
		final MapOptions options = new MapOptions();
		options.setMapTypeControl(true);
		options.setZoom(zoomlevel);
		options.setCenter(LatLng.newInstance(39.509, -98.434));
		options.setMapTypeId(new MapTypeId().getRoadmap());
		options.setDraggable(true);
		options.setScaleControl(true);
		options.setNavigationControl(true);
		options.setScrollwheel(true);
		MapWidget mw = new MapWidget(options);
		mw.setSize("200px", "200px");

		// Close the info window when clicking anywhere
//		Event.addListener(mw.getMap(), "click", new EventCallback() {
//			@Override
//			public void callback() {
//				closeInfoWindow();
//			}
//		});

		// Close the info window when clicking close
//		Event.addListener(infoWindow, "closeclick", new EventCallback() {
//			@Override
//			public void callback() {
//				closeInfoWindow();
//			}
//		});
		
		locationIDs.add(mw);

		if (actionToTakeWhenDone != null) actionToTakeWhenDone.run();
	}
	
	@Override
	public void showResponseDetail(Marker location) {
		if (markerToResponseMap.containsKey(location)) {
			SurveyResponse response = markerToResponseMap.get(location);
			final ResponseWidgetPopup displayWidget = new ResponseWidgetPopup();
			displayWidget.setResponse(response, new ResponseWidgetPopup.ElementHandlerCallback() {
				@Override
				public void addingElement(com.google.gwt.user.client.Element element,
						final String url) {
					// Save the event listener for later removal
					clickHandlers.add(
							Event.addDomListener(element, "click", new EventCallback() {
								// Pop open a new window when an element is clicked
								@Override
								public void callback() {
									Window.open(url, "_blank", "");
								}
							})
							);
				}
			});

			infoWindow.setContent(displayWidget.getElement());
			infoWindow.open(mapWidget.getMap(), location);
		}
	}

	@Override
	public void showMobilityDetail(Marker location) {
		if (markerToMobilityMap.containsKey(location)) {
			MobilityInfo mobInfo = markerToMobilityMap.get(location);
			final MobilityWidgetPopup displayWidget = new MobilityWidgetPopup();
			displayWidget.setResponse(mobInfo);

			infoWindow.setContent(displayWidget.getElement());
			infoWindow.open(mapWidget.getMap(), location);
		}
	}

	/**
	 * Cleans up all the event listeners and closes the info window.
	 */
	private void closeInfoWindow() {
		for (final HasMapsEventListener event : clickHandlers) {
			Event.removeListener(event);
		}
		clickHandlers.clear();
		infoWindow.close();
	} 

	@Override
	public void doExportCsvFormPost(String url, Map<String, String> params) {
		FormPanel exportForm = new FormPanel("_blank"); // must be _blank for firefox
		exportForm.setAction(url);
		exportForm.setMethod(FormPanel.METHOD_POST);
		FlowPanel innerContainer = new FlowPanel();    
		for (String paramName : params.keySet()) {
			Hidden field = new Hidden();
			field.setName(paramName);
			field.setValue(params.get(paramName));
			innerContainer.add(field);
		}
		exportForm.add(innerContainer);
		hiddenFormContainer.add(exportForm, "innerHiddenFormContainer");
		exportForm.submit();
		exportForm.removeFromParent();
	}


	@Override
	public void clearPromptXList() {
		this.promptXListBox.clear();
	}


	@Override
	public void addPromptX(String promptId, String displayString, boolean isSupported) {
		this.promptXListBox.addItem(displayString, promptId);
		if (!isSupported) { // unsupported prompts are disabled. // FIXME: IE?
			int itemIndex = this.promptXListBox.getItemCount() - 1;
			NodeList<Element> items = this.promptXListBox.getElement().getElementsByTagName("option");
			items.getItem(itemIndex).setAttribute("disabled", "disabled");
		}
	}


	@Override
	public void clearPromptYList() {
		this.promptYListBox.clear();
	}


	@Override
	public void addPromptY(String promptId, String displayString,
			boolean isSupported) {
		this.promptYListBox.addItem(displayString, promptId);
		if (!isSupported) { // unsupported prompts are disabled. // FIXME: IE?
			int itemIndex = this.promptYListBox.getItemCount() - 1;
			NodeList<Element> items = this.promptYListBox.getElement().getElementsByTagName("option");
			items.getItem(itemIndex).setAttribute("disabled", "disabled");
		}    
	}

	@Override
	public void selectFromDate(Date fromDate) {
		dateStartBox.setValue(fromDate);
	}

	@Override
	public Date getFromDate() {
		return this.dateStartBox.getValue();
	}

	@Override
	public void selectToDate(Date toDate) {
		dateEndBox.setValue(toDate);
	}

	@Override
	public Date getToDate() {
		return this.dateEndBox.getValue();
	}
}
