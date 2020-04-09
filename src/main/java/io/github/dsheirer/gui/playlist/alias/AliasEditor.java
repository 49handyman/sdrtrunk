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

package io.github.dsheirer.gui.playlist.alias;

import io.github.dsheirer.playlist.PlaylistManager;
import io.github.dsheirer.preference.UserPreferences;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class AliasEditor extends TabPane
{
    private PlaylistManager mPlaylistManager;
    private UserPreferences mUserPreferences;
    private Tab mAliasConfigurationTab;
    private Tab mAliasRadioReferenceTab;
    private Tab mAliasIdentifierTab;
    private Tab mAliasPriorityTab;
    private Tab mAliasRecordingTab;

    public AliasEditor(PlaylistManager playlistManager, UserPreferences userPreferences)
    {
        mPlaylistManager = playlistManager;
        mUserPreferences = userPreferences;

        setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        getTabs().addAll(getAliasConfigurationTab(), getAliasRadioReferenceTab(), getAliasIdentifierTab(),
            getAliasPriorityTab(), getAliasRecordingTab());
    }

    private Tab getAliasConfigurationTab()
    {
        if(mAliasConfigurationTab == null)
        {
            mAliasConfigurationTab = new Tab("Configuration");
            mAliasConfigurationTab.setContent(new AliasConfigurationEditor(mPlaylistManager, mUserPreferences));
        }

        return mAliasConfigurationTab;
    }

    private Tab getAliasRadioReferenceTab()
    {
        if(mAliasRadioReferenceTab == null)
        {
            mAliasRadioReferenceTab = new Tab("Radio Reference Import");
        }

        return mAliasRadioReferenceTab;
    }

    private Tab getAliasIdentifierTab()
    {
        if(mAliasIdentifierTab == null)
        {
            mAliasIdentifierTab = new Tab("View By: Identifier");
        }

        return mAliasIdentifierTab;
    }

    private Tab getAliasPriorityTab()
    {
        if(mAliasPriorityTab == null)
        {
            mAliasPriorityTab = new Tab("View By: Listen Priority");
        }

        return mAliasPriorityTab;
    }

    private Tab getAliasRecordingTab()
    {
        if(mAliasRecordingTab == null)
        {
            mAliasRecordingTab = new Tab("View By: Recording");
            mAliasRecordingTab.setContent(new AliasViewByRecordingEditor(mPlaylistManager));
        }

        return mAliasRecordingTab;
    }
}
