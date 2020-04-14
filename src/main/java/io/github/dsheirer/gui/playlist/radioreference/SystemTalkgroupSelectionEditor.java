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

import io.github.dsheirer.alias.AliasModel;
import io.github.dsheirer.playlist.PlaylistManager;
import io.github.dsheirer.preference.UserPreferences;
import io.github.dsheirer.rrapi.type.System;
import io.github.dsheirer.rrapi.type.Tag;
import io.github.dsheirer.rrapi.type.Talkgroup;
import io.github.dsheirer.rrapi.type.TalkgroupCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class SystemTalkgroupSelectionEditor extends GridPane
{
    private static final Logger mLog = LoggerFactory.getLogger(SystemTalkgroupSelectionEditor.class);

    private final TalkgroupCategory ALL_TALKGROUPS = new TalkgroupCategory();
    private UserPreferences mUserPreferences;
    private PlaylistManager mPlaylistManager;
    private TableView<Talkgroup> mTalkgroupTableView;
    private ComboBox<TalkgroupCategory> mTalkgroupCategoryComboBox;
    private TextField mSearchField;
    private TalkgroupEditor mTalkgroupEditor;
    private ComboBox<String> mAliasListNameComboBox;
    private Button mNewAliasListButton;
    private TalkgroupFilter mTalkgroupFilter = new TalkgroupFilter();
    private FilteredList<Talkgroup> mTalkgroupFilteredList;
    private ObservableList<Talkgroup> mTalkgroupList = FXCollections.observableArrayList();
    private System mCurrentSystem;
    private Map<Integer,Tag> mTagMap;

    public SystemTalkgroupSelectionEditor(UserPreferences userPreferences, PlaylistManager playlistManager)
    {
        mUserPreferences = userPreferences;
        mPlaylistManager = playlistManager;

        ALL_TALKGROUPS.setName("(All Talkgroups)");

        setPadding(new Insets(10,0,0,0));
        setVgap(10);
        setHgap(10);
        setMaxHeight(Double.MAX_VALUE);

        int row = 0;

        ColumnConstraints column1 = new ColumnConstraints();
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        getColumnConstraints().addAll(column1, column2);

        HBox searchBox = new HBox();
        searchBox.setSpacing(5);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        searchBox.getChildren().addAll(new Label("Search"), getSearchField());
        GridPane.setHgrow(searchBox, Priority.ALWAYS);
        GridPane.setConstraints(searchBox, 0, row);
        getChildren().add(searchBox);

        Label directions = new Label("Import Talkgroups To:");
        GridPane.setHalignment(directions, HPos.CENTER);
        GridPane.setConstraints(directions, 1, row);
        getChildren().add(directions);

        HBox categoryBox = new HBox();
        categoryBox.setAlignment(Pos.CENTER);
        categoryBox.setSpacing(5);
        HBox.setHgrow(getTalkgroupCategoryComboBox(), Priority.ALWAYS);
        categoryBox.getChildren().addAll(new Label("Category"), getTalkgroupCategoryComboBox());
        GridPane.setHgrow(categoryBox, Priority.ALWAYS);
        GridPane.setConstraints(categoryBox, 0, ++row);
        getChildren().add(categoryBox);

        HBox aliasListBox = new HBox();
        aliasListBox.setSpacing(5);
        aliasListBox.setAlignment(Pos.CENTER);
        aliasListBox.getChildren().addAll(new Label("Alias List"), getAliasListNameComboBox(), getNewAliasListButton());
        GridPane.setHgrow(aliasListBox, Priority.ALWAYS);
        GridPane.setConstraints(aliasListBox, 1, row);
        getChildren().add(aliasListBox);

        GridPane.setHgrow(getTalkgroupTableView(), Priority.ALWAYS);
        GridPane.setVgrow(getTalkgroupTableView(), Priority.ALWAYS);
        GridPane.setConstraints(getTalkgroupTableView(), 0, ++row);
        getChildren().add(getTalkgroupTableView());

        GridPane.setHgrow(getTalkgroupEditor(), Priority.ALWAYS);
        GridPane.setVgrow(getTalkgroupEditor(), Priority.ALWAYS);
        GridPane.setConstraints(getTalkgroupEditor(), 1, row);
        getChildren().add(getTalkgroupEditor());
    }

    public void clear()
    {
        mTalkgroupList.clear();
        getTalkgroupCategoryComboBox().getItems().clear();
    }

    private void updateFilter()
    {
        mTalkgroupFilter.setFilterText(getSearchField().getText());
        TalkgroupCategory category = getTalkgroupCategoryComboBox().getSelectionModel().getSelectedItem();

        if(category == ALL_TALKGROUPS)
        {
            mTalkgroupFilter.setCategory(null);
        }
        else
        {
            mTalkgroupFilter.setCategory(category != null ? category.getTalkgroupCategoryId() : null);
        }

        mTalkgroupFilteredList.setPredicate(null);
        mTalkgroupFilteredList.setPredicate(mTalkgroupFilter);
    }

    public void setSystem(System system, List<Talkgroup> talkgroups, List<TalkgroupCategory> categories, Map<Integer,Tag> tagMap)
    {
        mCurrentSystem = system;
        mTagMap = tagMap;

        clear();

        if(talkgroups != null && !talkgroups.isEmpty())
        {
            Collections.sort(talkgroups, Comparator.comparingInt(Talkgroup::getDecimalValue));
            mTalkgroupList.addAll(talkgroups);

            if(categories.size() > 0)
            {
                Collections.sort(categories, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                categories.add(0, ALL_TALKGROUPS);
                getTalkgroupCategoryComboBox().getItems().addAll(categories);
                getTalkgroupCategoryComboBox().getSelectionModel().select(ALL_TALKGROUPS);
            }
        }
    }

    private System getCurrentSystem()
    {
        return mCurrentSystem;
    }

    private Map<Integer,Tag> getTagMap()
    {
        return mTagMap;
    }

    private TalkgroupEditor getTalkgroupEditor()
    {
        if(mTalkgroupEditor == null)
        {
            mTalkgroupEditor = new TalkgroupEditor(mUserPreferences, mPlaylistManager);
        }

        return mTalkgroupEditor;
    }

    private TextField getSearchField()
    {
        if(mSearchField == null)
        {
            mSearchField = TextFields.createClearableTextField();
            mSearchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        }

        return mSearchField;
    }

    private ComboBox<String> getAliasListNameComboBox()
    {
        if(mAliasListNameComboBox == null)
        {
            mAliasListNameComboBox = new ComboBox<>(mPlaylistManager.getAliasModel().aliasListNames());

            if(mAliasListNameComboBox.getItems().size() > 1)
            {
                if(!mAliasListNameComboBox.getItems().get(0).contentEquals(AliasModel.NO_ALIAS_LIST))
                {
                    mAliasListNameComboBox.getSelectionModel().select(0);
                }
                else
                {
                    mAliasListNameComboBox.getSelectionModel().select(1);
                }
            }
            else if(mAliasListNameComboBox.getItems().size() == 1)
            {
                mAliasListNameComboBox.getSelectionModel().select(0);
            }
        }

        return mAliasListNameComboBox;
    }

    private Button getNewAliasListButton()
    {
        if(mNewAliasListButton == null)
        {
            mNewAliasListButton = new Button("New Alias List");
            mNewAliasListButton.setOnAction(event -> {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Create New Alias List");
                dialog.setHeaderText("New Alias List");
                dialog.setContentText("Please enter a name?");
                Optional<String> result = dialog.showAndWait();

                String name = result.get();

                if(name != null && !name.isEmpty())
                {
                    name = name.trim();
                    mPlaylistManager.getAliasModel().addAliasList(name);
                    getAliasListNameComboBox().getSelectionModel().select(name);
                }
            });
        }

        return mNewAliasListButton;
    }

    private ComboBox<TalkgroupCategory> getTalkgroupCategoryComboBox()
    {
        if(mTalkgroupCategoryComboBox == null)
        {
            mTalkgroupCategoryComboBox = new ComboBox<>();
            mTalkgroupCategoryComboBox.setMaxWidth(Double.MAX_VALUE);
            mTalkgroupCategoryComboBox.setConverter(new TalkgroupCategoryStringConverter());
            mTalkgroupCategoryComboBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> updateFilter());
        }

        return mTalkgroupCategoryComboBox;
    }

    private TableView<Talkgroup> getTalkgroupTableView()
    {
        if(mTalkgroupTableView == null)
        {
            mTalkgroupTableView = new TableView<>();

            TableColumn talkgroupColumn = new TableColumn("Talkgroup");
            talkgroupColumn.setCellValueFactory(new PropertyValueFactory<>("decimalValue"));

            TableColumn descriptionColumn = new TableColumn("Description");
            descriptionColumn.setPrefWidth(300);
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

            TableColumn aliasColumn = new TableColumn("Alias");
            descriptionColumn.setPrefWidth(200);

            mTalkgroupTableView.getColumns().addAll(talkgroupColumn, descriptionColumn, aliasColumn);
            mTalkgroupTableView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, selected) -> getTalkgroupEditor().setTalkgroup(selected));

            mTalkgroupFilteredList = new FilteredList<>(mTalkgroupList);
            SortedList<Talkgroup> sortedList = new SortedList<>(mTalkgroupFilteredList);
            sortedList.comparatorProperty().bind(mTalkgroupTableView.comparatorProperty());
            mTalkgroupTableView.setItems(sortedList);
        }

        return mTalkgroupTableView;
    }

    public class TalkgroupCategoryListCell extends ListCell<TalkgroupCategory>
    {
        @Override
        protected void updateItem(TalkgroupCategory item, boolean empty)
        {
            super.updateItem(item, empty);
            setText((empty || item == null) ? null : item.getName());
        }
    }

    public class TalkgroupCategoryStringConverter extends StringConverter<TalkgroupCategory>
    {
        @Override
        public String toString(TalkgroupCategory cat)
        {
            return cat != null ? cat.getName() : null;
        }

        @Override
        public TalkgroupCategory fromString(String string)
        {
            for(TalkgroupCategory cat: getTalkgroupCategoryComboBox().getItems())
            {
                if(cat.getName().contentEquals(string))
                {
                    return cat;
                }
            }

            return null;
        }
    }

    public class TalkgroupFilter implements Predicate<Talkgroup>
    {
        private String mFilterText;
        private Integer mCategory;

        public TalkgroupFilter()
        {
        }

        public void setFilterText(String filterText)
        {
            mFilterText = filterText != null ? filterText.toLowerCase() : null;
        }

        public void setCategory(Integer category)
        {
            mCategory = category;
        }

        @Override
        public boolean test(Talkgroup talkgroup)
        {
            if(mCategory == null && (mFilterText == null  || mFilterText.isEmpty()))
            {
                return true;
            }

            if(mCategory != null && mFilterText != null)
            {
                return talkgroup.getTalkgroupCategoryId() == mCategory &&
                    (talkgroup.getDescription().toLowerCase().contains(mFilterText) ||
                      String.valueOf(talkgroup.getDecimalValue()).contains(mFilterText));
            }
            else if(mCategory != null)
            {
                return talkgroup.getTalkgroupCategoryId() == mCategory;
            }
            else
            {
                return (talkgroup.getDescription().toLowerCase().contains(mFilterText) ||
                    String.valueOf(talkgroup.getDecimalValue()).contains(mFilterText));
            }
        }
    }
}
