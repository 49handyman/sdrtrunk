/*
 * *****************************************************************************
 *  Copyright (C) 2014-2020 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * ****************************************************************************
 */

package io.github.dsheirer.gui.playlist.radioreference;

import io.github.dsheirer.playlist.PlaylistManager;
import io.github.dsheirer.preference.UserPreferences;
import io.github.dsheirer.rrapi.RadioReferenceException;
import io.github.dsheirer.rrapi.type.CountyInfo;
import io.github.dsheirer.rrapi.type.Flavor;
import io.github.dsheirer.rrapi.type.Site;
import io.github.dsheirer.rrapi.type.System;
import io.github.dsheirer.rrapi.type.SystemInformation;
import io.github.dsheirer.rrapi.type.Tag;
import io.github.dsheirer.rrapi.type.Talkgroup;
import io.github.dsheirer.rrapi.type.TalkgroupCategory;
import io.github.dsheirer.rrapi.type.Type;
import io.github.dsheirer.rrapi.type.Voice;
import io.github.dsheirer.service.radioreference.RadioReference;
import io.github.dsheirer.util.ThreadPool;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Radio Reference editor for trunked radio systems
 */
public class SystemEditor extends VBox
{
    private static final Logger mLog = LoggerFactory.getLogger(SystemEditor.class);

    private UserPreferences mUserPreferences;
    private RadioReference mRadioReference;
    private PlaylistManager mPlaylistManager;
    private Level mLevel;
    private ListView<System> mSystemListView;
    private SystemFrequencyEditor mSystemFrequencyEditor;
    private IntegerProperty mSystemCountProperty = new SimpleIntegerProperty();
    private TabPane mTabPane;
    private Tab mSystemTab;
    private Tab mTalkgroupTab;
    private SystemSiteSelectionEditor mSystemSiteSelectionEditor;
    private SystemTalkgroupSelectionEditor mSystemTalkgroupSelectionEditor;
    private Map<Integer,Tag> mTagMap;

    /**
     * Constructs an instance
     * @param userPreferences for preferences
     * @param radioReference to access radio reference
     * @param playlistManager
     * @param level STATE or COUNTY
     */
    public SystemEditor(UserPreferences userPreferences, RadioReference radioReference,
                        PlaylistManager playlistManager, Level level)
    {
        mUserPreferences = userPreferences;
        mRadioReference = radioReference;
        mPlaylistManager = playlistManager;
        mLevel = level;
        mSystemCountProperty.bind(Bindings.size(getSystemListView().getItems()));

        setPadding(new Insets(10,10,10,10));
        setSpacing(10);
        VBox.setVgrow(getSystemFrequencyEditor(), Priority.ALWAYS);
        getChildren().addAll(getSystemListView(), getTabPane());
    }

    /**
     * Observable count of systems in the editor
     */
    public IntegerProperty systemCountProperty()
    {
        return mSystemCountProperty;
    }

    /**
     * Sets the list of displayed systems and clears out any existing systems.  Auto-selects a system if the user
     * has selected that system before.
     *
     * Note: this should only be invoked on the FX application thread
     *
     * @param systems to display
     */
    public void setSystems(List<System> systems)
    {
        Collections.sort(systems, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        getSystemListView().getItems().clear();
        getSystemListView().getItems().addAll(systems);

        int preferredSystemId = mUserPreferences.getRadioReferencePreference().getPreferredSystemId(mLevel);

        for(System system: getSystemListView().getItems())
        {
            if(system.getSystemId() == preferredSystemId)
            {
                getSystemListView().getSelectionModel().select(system);
                getSystemListView().scrollTo(system);
                return;
            }
        }
    }

    private TabPane getTabPane()
    {
        if(mTabPane == null)
        {
            mTabPane = new TabPane();
            mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            mTabPane.getTabs().addAll(getSystemTab(), getTalkgroupTab());
        }

        return mTabPane;
    }

    private SystemSiteSelectionEditor getSystemSiteSelectionEditor()
    {
        if(mSystemSiteSelectionEditor == null)
        {
            mSystemSiteSelectionEditor = new SystemSiteSelectionEditor(mUserPreferences, mPlaylistManager);
        }

        return mSystemSiteSelectionEditor;
    }

    private Tab getSystemTab()
    {
        if(mSystemTab == null)
        {
            mSystemTab = new Tab("System View");
            mSystemTab.setContent(getSystemSiteSelectionEditor());
        }

        return mSystemTab;
    }

    private SystemTalkgroupSelectionEditor getSystemTalkgroupSelectionEditor()
    {
        if(mSystemTalkgroupSelectionEditor == null)
        {
            mSystemTalkgroupSelectionEditor = new SystemTalkgroupSelectionEditor(mUserPreferences, mPlaylistManager);
        }

        return mSystemTalkgroupSelectionEditor;
    }

    private Tab getTalkgroupTab()
    {
        if(mTalkgroupTab == null)
        {
            mTalkgroupTab = new Tab("Talkgroup View");
            mTalkgroupTab.setContent(getSystemTalkgroupSelectionEditor());
        }

        return mTalkgroupTab;
    }

    private ListView<System> getSystemListView()
    {
        if(mSystemListView == null)
        {
            mSystemListView = new ListView<>();
            mSystemListView.setPrefHeight(100);
            mSystemListView.setCellFactory(param -> new SystemListCell());
            mSystemListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> setSystem(newValue));
        }

        return mSystemListView;
    }

    /**
     * Trunked radio system view for displaying a trunked system
     * @return node
     */
    private SystemFrequencyEditor getSystemFrequencyEditor()
    {
        if(mSystemFrequencyEditor == null)
        {
            mSystemFrequencyEditor = new SystemFrequencyEditor(mRadioReference);
        }

        return mSystemFrequencyEditor;
    }

    /**
     * Sets the system to be displayed in the editor
     */
    private void setSystem(System system)
    {
        getSystemSiteSelectionEditor().clear();
        getSystemTalkgroupSelectionEditor().clear();

        if(system != null)
        {
            mLog.debug("System: " + (system != null ? system.getName() : ""));
            mLog.debug("Type:" + system.getTypeId() + " Flavor:" + system.getFlavorId() + " Voice:" + system.getVoiceId());
            mUserPreferences.getRadioReferencePreference().setPreferredSystemId(system.getSystemId(), mLevel);

            ThreadPool.SCHEDULED.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(mTagMap == null)
                        {
                            mTagMap = mRadioReference.getService().getTagsMap();
                        }

                        SystemInformation systemInformation = mRadioReference.getService().getSystemInformation(system.getSystemId());
                        Type type = mRadioReference.getService().getType(systemInformation.getTypeId());
                        Flavor flavor = mRadioReference.getService().getFlavor(systemInformation.getFlavorId());
                        Voice voice = mRadioReference.getService().getVoice(systemInformation.getVoiceId());
                        List<Site> sites = mRadioReference.getService().getSites(system.getSystemId());

                        //The service api doesn't provide the county name, so we run a separate query and update the value
                        for(Site site: sites)
                        {
                            CountyInfo countyInfo = mRadioReference.getService().getCountyInfo(site.getCountyId());
                            site.setCountyName(countyInfo.getName());
                        }

                        Platform.runLater(() -> getSystemSiteSelectionEditor().setSystem(system, systemInformation, type, flavor, voice, sites));

                        List<Talkgroup> talkgroups = mRadioReference.getService().getTalkgroups(system.getSystemId());
                        List<TalkgroupCategory> categories = mRadioReference.getService().getTalkgroupCategories(system.getSystemId());
                        Platform.runLater(() -> getSystemTalkgroupSelectionEditor()
                            .setSystem(system, talkgroups, categories, getTagMap()));
                    }
                    catch(RadioReferenceException rre)
                    {
                        mLog.error("Error retrieving system information - " + rre.getMessage());
                    }
                }
            });
        }
    }

    private Map<Integer,Tag> getTagMap()
    {
        return mTagMap;
    }

//    private void setCountry(final Country country)
//    {
//        getStateListView().getItems().clear();
//        getCountryAgencyListView().getItems().clear();
//        getFrequencyTableView().update(null, null, Collections.emptyList());
//        getTrunkedSystemView().setSystem(null);
//
//        final int preferredStateId = mUserPreferences.getRadioReferencePreference().getPreferredStateId();
//        final int preferredAgencyId = mUserPreferences.getRadioReferencePreference().getPreferredAgencyId();
//
//        if(country != null && mRadioReference.availableProperty().get())
//        {
//            ThreadPool.SCHEDULED.submit(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    try
//                    {
//                        CountryInfo countryInfo = mRadioReference.getService().getCountryInfo(country);
//                        List<State> states = countryInfo.getStates();
//                        Collections.sort(states, new Comparator<State>()
//                        {
//                            @Override
//                            public int compare(State o1, State o2)
//                            {
//                                return o1.getName().compareTo(o2.getName());
//                            }
//                        });
//                        List<Agency> agencies = countryInfo.getAgencies();
//                        Collections.sort(agencies, new Comparator<Agency>()
//                        {
//                            @Override
//                            public int compare(Agency o1, Agency o2)
//                            {
//                                return o1.getName().compareTo(o2.getName());
//                            }
//                        });
//
//                        if(states != null || agencies != null)
//                        {
//                            Platform.runLater(new Runnable()
//                            {
//                                @Override
//                                public void run()
//                                {
//                                    if(states != null && !states.isEmpty())
//                                    {
//                                        getStateListView().getItems().addAll(states);
//
//                                        if(preferredStateId >= 0)
//                                        {
//                                            for(State state: states)
//                                            {
//                                                if(state.getStateId() == preferredStateId)
//                                                {
//                                                    getStateListView().getSelectionModel().select(state);
//                                                    getStateListView().scrollTo(state);
//                                                    continue;
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    if(agencies != null && !agencies.isEmpty())
//                                    {
//                                        getCountryAgencyListView().getItems().addAll(agencies);
//
//                                        if(preferredAgencyId >= 0)
//                                        {
//                                            for(Agency agency: agencies)
//                                            {
//                                                if(agency.getAgencyId() == preferredAgencyId)
//                                                {
//                                                    getCountryAgencyListView().getSelectionModel().select(agency);
//                                                    getCountryAgencyListView().scrollTo(agency);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//
//                        }
//                    }
//                    catch(RadioReferenceException rre)
//                    {
//                        mLog.error("Error retrieving country info for " + country.getName());
//                    }
//                }
//            });
//        }
//    }
//
//    private void setState(State state)
//    {
//        getCountyListView().getItems().clear();
//        getStateAgencyListView().getItems().clear();
//
//        final int preferredCountyId = mUserPreferences.getRadioReferencePreference().getPreferredCountyId();
//        final int preferredAgencyId = mUserPreferences.getRadioReferencePreference().getPreferredAgencyId();
//
//        if(state != null && mRadioReference.availableProperty().get())
//        {
//            ThreadPool.SCHEDULED.submit(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    try
//                    {
//                        StateInfo stateInfo = mRadioReference.getService().getStateInfo(state.getStateId());
//
//                        List<County> counties = stateInfo.getCounties();
//
//                        //Pre-cache county information instances
//                        ThreadPool.SCHEDULED.submit(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                try
//                                {
//                                    for(County county: counties)
//                                    {
//                                        mRadioReference.getService().getCountyInfo(county.getCountyId());
//                                    }
//                                }
//                                catch(RadioReferenceException rre)
//                                {
//                                    //Do nothing, this just an attempt to pre-cache the counties
//                                }
//                            }
//                        });
//
//                        Collections.sort(counties, new Comparator<County>()
//                        {
//                            @Override
//                            public int compare(County o1, County o2)
//                            {
//                                return o1.getName().compareTo(o2.getName());
//                            }
//                        });
//
//                        List<Agency> agencies = stateInfo.getAgencies();
//                        Collections.sort(agencies, new Comparator<Agency>()
//                        {
//                            @Override
//                            public int compare(Agency o1, Agency o2)
//                            {
//                                return o1.getName().compareTo(o2.getName());
//                            }
//                        });
//
//
//
//                        if(counties != null || agencies != null)
//                        {
//                            Platform.runLater(new Runnable()
//                            {
//                                @Override
//                                public void run()
//                                {
//                                    if(counties != null && !counties.isEmpty())
//                                    {
//                                        getCountyListView().getItems().addAll(counties);
//
//                                        if(preferredCountyId >= 0)
//                                        {
//                                            for(County county: counties)
//                                            {
//                                                if(county.getCountyId() == preferredCountyId)
//                                                {
//                                                    getCountyListView().getSelectionModel().select(county);
//                                                    getCountyListView().scrollTo(county);
//                                                    continue;
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    if(agencies != null && !agencies.isEmpty())
//                                    {
//                                        getStateAgencyListView().getItems().addAll(agencies);
//
//                                        if(preferredAgencyId >= 0)
//                                        {
//                                            for(Agency agency: agencies)
//                                            {
//                                                if(agency.getAgencyId() == preferredAgencyId)
//                                                {
//                                                    getStateAgencyListView().getSelectionModel().select(agency);
//                                                    getStateAgencyListView().scrollTo(agency);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//
//                        }
//                    }
//                    catch(RadioReferenceException rre)
//                    {
//                        mLog.error("Error retrieving country info for " + state.getName(), rre);
//                    }
//                }
//            });
//        }
//    }
//
//    private void setCounty(County county)
//    {
//        getSystemComboBox().getItems().clear();
//        getCountyAgencyListView().getItems().clear();
//        final int preferredSystemId = mUserPreferences.getRadioReferencePreference().getPreferredSystemId();
//        final int preferredAgencyId = mUserPreferences.getRadioReferencePreference().getPreferredAgencyId();
//
//        if(county != null && mRadioReference.availableProperty().get())
//        {
//            ThreadPool.SCHEDULED.submit(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    try
//                    {
//                        CountyInfo countyInfo = mRadioReference.getService().getCountyInfo(county.getCountyId());
//                        setFrequencyViewCategories(COUNTY_LABEL, countyInfo.getName(), countyInfo.getCategories());
//                        List<System> systems = countyInfo.getSystems();
//                        Collections.sort(systems, new Comparator<System>()
//                        {
//                            @Override
//                            public int compare(System o1, System o2)
//                            {
//                                return o1.getName().compareTo(o2.getName());
//                            }
//                        });
//
//                        List<Agency> agencies = countyInfo.getAgencies();
//                        Collections.sort(agencies, new Comparator<Agency>()
//                        {
//                            @Override
//                            public int compare(Agency o1, Agency o2)
//                            {
//                                return o1.getName().compareTo(o2.getName());
//                            }
//                        });
//
//                        if(systems != null || agencies != null)
//                        {
//                            Platform.runLater(new Runnable()
//                            {
//                                @Override
//                                public void run()
//                                {
//                                    if(systems != null && !systems.isEmpty())
//                                    {
//                                        getSystemComboBox().getItems().addAll(systems);
//
//                                        if(preferredSystemId >= 0)
//                                        {
//                                            for(System system: systems)
//                                            {
//                                                if(system.getSystemId() == preferredSystemId)
//                                                {
//                                                    getSystemComboBox().getSelectionModel().select(system);
//                                                    getSystemComboBox().scrollTo(system);
//                                                    continue;
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    if(agencies != null && !agencies.isEmpty())
//                                    {
//                                        getCountyAgencyListView().getItems().addAll(agencies);
//
//                                        if(preferredAgencyId >= 0)
//                                        {
//                                            for(Agency agency: agencies)
//                                            {
//                                                if(agency.getAgencyId() == preferredAgencyId)
//                                                {
//                                                    getCountyAgencyListView().getSelectionModel().select(agency);
//                                                    getCountyAgencyListView().scrollTo(agency);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            });
//                        }
//                    }
//                    catch(RadioReferenceException rre)
//                    {
//                        mLog.error("Error retrieving country info for " + county.getName(), rre);
//                    }
//                }
//            });
//        }
//    }
//
//    /**
//     * Sets the argument as the results view node in the lower half of the split pane
//     * @param node to view
//     */
//    private void setResultsView(Node node)
//    {
//        if(getSplitPane().getItems().size() == 2)
//        {
//            getSplitPane().getItems().remove(1);
//            getSplitPane().getItems().add(node);
//        }
//        else
//        {
//            mLog.error("Error - expected 2 nodes in split pane but encountered: " + getSplitPane().getItems().size());
//        }
//    }
//
//    private void setSystem(System system)
//    {
//        if(system != null)
//        {
//            mUserPreferences.getRadioReferencePreference().setPreferredAgencyId(RadioReferencePreference.INVALID_ID);
//            mUserPreferences.getRadioReferencePreference().setPreferredSystemId(system.getSystemId());
//            setResultsView(getTrunkedSystemView());
//            getTrunkedSystemView().setSystem(system);
//        }
//    }
//
//    public class StateListCell extends ListCell<State>
//    {
//        @Override
//        protected void updateItem(State item, boolean empty)
//        {
//            super.updateItem(item, empty);
//            setText((empty || item == null) ? null : item.getName());
//        }
//    }
//
//    public class CountyListCell extends ListCell<County>
//    {
//        @Override
//        protected void updateItem(County item, boolean empty)
//        {
//            super.updateItem(item, empty);
//            setText((empty || item == null) ? null : item.getName());
//        }
//    }

    public class SystemListCell extends ListCell<System>
    {
        private HBox mHBox;
        private Label mName;
        private Label mProtocol;

        public SystemListCell()
        {
            mHBox = new HBox();
            mHBox.setMaxWidth(Double.MAX_VALUE);
            mName = new Label();
            mName.setMaxWidth(Double.MAX_VALUE);
            mName.setAlignment(Pos.CENTER_LEFT);
            mProtocol = new Label();
            mProtocol.setMaxWidth(Double.MAX_VALUE);
            mProtocol.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(mName, Priority.ALWAYS);
            HBox.setHgrow(mProtocol, Priority.ALWAYS);
            mHBox.getChildren().addAll(mName, mProtocol);
        }

        @Override
        protected void updateItem(System item, boolean empty)
        {
            super.updateItem(item, empty);

            setText(null);

            if(empty || item == null)
            {
                setGraphic(null);
            }
            else
            {
                mName.setText(item.getName());

                Type type = null;

                try
                {
                    type = mRadioReference.getService().getType(item.getTypeId());
                }
                catch(RadioReferenceException rre)
                {
                    mLog.error("Error getting type", rre);
                }

                if(type != null)
                {
                    mProtocol.setText(type.getName());
                }
                else
                {
                    mProtocol.setText("Unknown");
                }

                setGraphic(mHBox);
            }
        }
    }
}
