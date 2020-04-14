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
import io.github.dsheirer.rrapi.type.System;
import io.github.dsheirer.rrapi.type.Talkgroup;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TalkgroupEditor extends GridPane
{
    private static final Logger mLog = LoggerFactory.getLogger(TalkgroupEditor.class);
    private UserPreferences mUserPreferences;
    private PlaylistManager mPlaylistManager;
    private TextField mAlphaTagTextField;
    private TextField mDescriptionTextField;
    private TextField mTalkgroupTextField;
    private TextField mModeTextField;
    private TextField mEncryptionTextField;

    public TalkgroupEditor(UserPreferences userPreferences, PlaylistManager playlistManager)
    {
        mUserPreferences = userPreferences;
        mPlaylistManager = playlistManager;

        setHgap(5);
        setVgap(5);

        int row = 0;

        Label radioReferenceLabel = new Label("Radio Reference Talkgroup Details");
        GridPane.setConstraints(radioReferenceLabel, 1, row);
        GridPane.setHalignment(radioReferenceLabel, HPos.LEFT);
        getChildren().add(radioReferenceLabel);

        Label talkgroupLabel = new Label("Talkgroup");
        GridPane.setConstraints(talkgroupLabel, 0, ++row);
        GridPane.setHalignment(talkgroupLabel, HPos.RIGHT);
        getChildren().add(talkgroupLabel);

        GridPane.setHgrow(getTalkgroupTextField(), Priority.ALWAYS);
        GridPane.setConstraints(getTalkgroupTextField(), 1, row);
        getChildren().add(getTalkgroupTextField());

        Label alphaLabel = new Label("Alpha Tag");
        GridPane.setConstraints(alphaLabel, 0, ++row);
        GridPane.setHalignment(alphaLabel, HPos.RIGHT);
        getChildren().add(alphaLabel);

        GridPane.setHgrow(getAlphaTagTextField(), Priority.ALWAYS);
        GridPane.setConstraints(getAlphaTagTextField(), 1, row);
        getChildren().add(getAlphaTagTextField());

        Label descriptionLabel = new Label("Description");
        GridPane.setConstraints(descriptionLabel, 0, ++row);
        GridPane.setHalignment(descriptionLabel, HPos.RIGHT);
        getChildren().add(descriptionLabel);

        GridPane.setHgrow(getDescriptionTextField(), Priority.ALWAYS);
        GridPane.setConstraints(getDescriptionTextField(), 1, row);
        getChildren().add(getDescriptionTextField());

        Label modeLabel = new Label("Mode");
        GridPane.setConstraints(modeLabel, 0, ++row);
        GridPane.setHalignment(modeLabel, HPos.RIGHT);
        getChildren().add(modeLabel);

        GridPane.setHgrow(getModeTextField(), Priority.ALWAYS);
        GridPane.setConstraints(getModeTextField(), 1, row);
        getChildren().add(getModeTextField());

        Label encryptionLabel = new Label("Encryption");
        GridPane.setConstraints(encryptionLabel, 0, ++row);
        GridPane.setHalignment(encryptionLabel, HPos.RIGHT);
        getChildren().add(encryptionLabel);

        GridPane.setHgrow(getEncryptionTextField(), Priority.ALWAYS);
        GridPane.setConstraints(getEncryptionTextField(), 1, row);
        getChildren().add(getEncryptionTextField());
    }

    public void setTalkgroup(Talkgroup talkgroup, System system, RadioReferenceDecoder decoder)
    {
        if(talkgroup != null)
        {
            getTalkgroupTextField().setText(decoder.format(talkgroup, system));
            getAlphaTagTextField().setText(talkgroup.getAlphaTag());
            getDescriptionTextField().setText(talkgroup.getDescription());

            TalkgroupMode talkgroupMode = TalkgroupMode.lookup(talkgroup.getMode());
            getModeTextField().setText(talkgroupMode.toString());

            TalkgroupEncryption talkgroupEncryption = TalkgroupEncryption.lookup(talkgroup.getEncryptionState());
            getEncryptionTextField().setText(talkgroupEncryption.toString());
        }
        else
        {
            getTalkgroupTextField().setText(null);
            getAlphaTagTextField().setText(null);
            getDescriptionTextField().setText(null);
            getModeTextField().setText(null);
            getEncryptionTextField().setText(null);
        }
    }

    public TextField getAlphaTagTextField()
    {
        if(mAlphaTagTextField == null)
        {
            mAlphaTagTextField = new TextField();
            mAlphaTagTextField.setMaxWidth(Double.MAX_VALUE);
            mAlphaTagTextField.setDisable(true);
        }

        return mAlphaTagTextField;
    }

    public TextField getDescriptionTextField()
    {
        if(mDescriptionTextField == null)
        {
            mDescriptionTextField = new TextField();
            mDescriptionTextField.setMaxWidth(Double.MAX_VALUE);
            mDescriptionTextField.setDisable(true);
        }

        return mDescriptionTextField;
    }

    public TextField getTalkgroupTextField()
    {
        if(mTalkgroupTextField == null)
        {
            mTalkgroupTextField = new TextField();
            mTalkgroupTextField.setMaxWidth(Double.MAX_VALUE);
            mTalkgroupTextField.setDisable(true);
        }

        return mTalkgroupTextField;
    }

    public TextField getModeTextField()
    {
        if(mModeTextField == null)
        {
            mModeTextField = new TextField();
            mModeTextField.setMaxWidth(Double.MAX_VALUE);
            mModeTextField.setDisable(true);
        }

        return mModeTextField;
    }

    public TextField getEncryptionTextField()
    {
        if(mEncryptionTextField == null)
        {
            mEncryptionTextField = new TextField();
            mEncryptionTextField.setMaxWidth(Double.MAX_VALUE);
            mEncryptionTextField.setDisable(true);
        }

        return mEncryptionTextField;
    }
}
