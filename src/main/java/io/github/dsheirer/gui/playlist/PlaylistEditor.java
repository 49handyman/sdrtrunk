/*
 *
 *  * ******************************************************************************
 *  * Copyright (C) 2014-2020 Dennis Sheirer
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *  * *****************************************************************************
 *
 *
 */

package io.github.dsheirer.gui.playlist;

import io.github.dsheirer.eventbus.MyEventBus;
import io.github.dsheirer.gui.playlist.alias.AliasEditor;
import io.github.dsheirer.gui.playlist.alias.AliasTabRequest;
import io.github.dsheirer.gui.playlist.channel.ChannelEditor;
import io.github.dsheirer.gui.playlist.manager.PlaylistManagerEditor;
import io.github.dsheirer.gui.playlist.radioreference.RadioReferenceEditor;
import io.github.dsheirer.gui.playlist.streaming.StreamingEditor;
import io.github.dsheirer.gui.preference.PreferenceEditorType;
import io.github.dsheirer.gui.preference.PreferenceEditorViewRequest;
import io.github.dsheirer.playlist.PlaylistManager;
import io.github.dsheirer.preference.UserPreferences;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX playlist, channels, aliases, streaming and radioreference.com import editor
 */
public class PlaylistEditor extends BorderPane
{
    private static final Logger mLog = LoggerFactory.getLogger(PlaylistEditor.class);

    private PlaylistManager mPlaylistManager;
    private UserPreferences mUserPreferences;
    private MenuBar mMenuBar;
    private TabPane mTabPane;
    private Tab mPlaylistsTab;
    private Tab mChannelsTab;
    private Tab mAliasesTab;
    private Tab mRadioReferenceTab;
    private Tab mStreamingTab;
    private AliasEditor mAliasEditor;

    /**
     * Constructs an instance
     * @param playlistManager for alias and channel models
     * @param userPreferences for settings
     */
    public PlaylistEditor(PlaylistManager playlistManager, UserPreferences userPreferences)
    {
        mPlaylistManager = playlistManager;
        mUserPreferences = userPreferences;

        setTop(getMenuBar());
        setCenter(getTabPane());
    }

    /**
     * Process requests for sub-editor actions like view an alias or view a channel.
     *
     * Note: this method must be invoked on the JavaFX platform thread
     * @param request to process
     */
    public void process(PlaylistEditorRequest request)
    {
        switch(request.getTabName())
        {
            case ALIAS:
                if(request instanceof AliasTabRequest)
                {
                    getTabPane().getSelectionModel().select(getAliasesTab());
                    getAliasEditor().process((AliasTabRequest)request);
                }
                break;
            case PLAYLIST:
                //Ignore - this is a request to simply show te playlist editor
                break;
            default:
                mLog.warn("Unrecognized playlist editor request: " + request.getClass());
                break;
        }
    }

    private MenuBar getMenuBar()
    {
        if(mMenuBar == null)
        {
            mMenuBar = new MenuBar();

            //File Menu
            Menu fileMenu = new Menu("File");

            MenuItem closeItem = new MenuItem("Close");
            closeItem.setOnAction(event -> getMenuBar().getParent().getScene().getWindow().hide());
            fileMenu.getItems().add(closeItem);

            mMenuBar.getMenus().add(fileMenu);

            Menu editMenu = new Menu("Edit");
            MenuItem userPreferenceItem = new MenuItem("User Preferences");
            userPreferenceItem.setOnAction(event -> MyEventBus.getEventBus()
                .post(new PreferenceEditorViewRequest(PreferenceEditorType.TALKGROUP_FORMAT)));
            editMenu.getItems().add(userPreferenceItem);
            mMenuBar.getMenus().add(editMenu);
        }

        return mMenuBar;
    }

    private TabPane getTabPane()
    {
        if(mTabPane == null)
        {
            mTabPane = new TabPane();
            mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            mTabPane.getTabs().addAll(getPlaylistsTab(), getChannelsTab(), getAliasesTab(), getStreamingTab(),
                getRadioReferenceTab());
        }

        return mTabPane;
    }

    private Tab getAliasesTab()
    {
        if(mAliasesTab == null)
        {
            mAliasesTab = new Tab("Aliases");
            mAliasesTab.setContent(getAliasEditor());
        }

        return mAliasesTab;
    }

    private AliasEditor getAliasEditor()
    {
        if(mAliasEditor == null)
        {
            mAliasEditor = new AliasEditor(mPlaylistManager, mUserPreferences);
        }

        return mAliasEditor;
    }

    private Tab getChannelsTab()
    {
        if(mChannelsTab == null)
        {
            mChannelsTab = new Tab("Channels");
            mChannelsTab.setContent(new ChannelEditor(mPlaylistManager));
        }

        return mChannelsTab;
    }

    private Tab getPlaylistsTab()
    {
        if(mPlaylistsTab == null)
        {
            mPlaylistsTab = new Tab("Playlists");
            mPlaylistsTab.setContent(new PlaylistManagerEditor(mPlaylistManager, mUserPreferences));
        }

        return mPlaylistsTab;
    }

    private Tab getRadioReferenceTab()
    {
        if(mRadioReferenceTab == null)
        {
            mRadioReferenceTab = new Tab("Radio Reference");
            mRadioReferenceTab.setContent(new RadioReferenceEditor(mUserPreferences, mPlaylistManager.getRadioReference()));
        }

        return mRadioReferenceTab;
    }

    private Tab getStreamingTab()
    {
        if(mStreamingTab == null)
        {
            mStreamingTab = new Tab("Streaming");
            mStreamingTab.setContent(new StreamingEditor(mPlaylistManager));
        }

        return mStreamingTab;
    }
}
