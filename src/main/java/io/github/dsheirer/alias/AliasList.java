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
package io.github.dsheirer.alias;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.dsheirer.alias.id.AliasID;
import io.github.dsheirer.alias.id.broadcast.BroadcastChannel;
import io.github.dsheirer.alias.id.esn.Esn;
import io.github.dsheirer.alias.id.priority.Priority;
import io.github.dsheirer.alias.id.radio.Radio;
import io.github.dsheirer.alias.id.radio.RadioRange;
import io.github.dsheirer.alias.id.status.UnitStatusID;
import io.github.dsheirer.alias.id.status.UserStatusID;
import io.github.dsheirer.alias.id.talkgroup.Talkgroup;
import io.github.dsheirer.alias.id.talkgroup.TalkgroupRange;
import io.github.dsheirer.alias.id.tone.TonesID;
import io.github.dsheirer.identifier.Identifier;
import io.github.dsheirer.identifier.IdentifierCollection;
import io.github.dsheirer.identifier.esn.ESNIdentifier;
import io.github.dsheirer.identifier.patch.PatchGroup;
import io.github.dsheirer.identifier.patch.PatchGroupIdentifier;
import io.github.dsheirer.identifier.radio.RadioIdentifier;
import io.github.dsheirer.identifier.status.UnitStatusIdentifier;
import io.github.dsheirer.identifier.status.UserStatusIdentifier;
import io.github.dsheirer.identifier.talkgroup.TalkgroupIdentifier;
import io.github.dsheirer.identifier.tone.ToneIdentifier;
import io.github.dsheirer.identifier.tone.ToneSequence;
import io.github.dsheirer.protocol.Protocol;
import io.github.dsheirer.sample.Listener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * List of aliases that share the same alias list name and provides convenient methods for looking up alias
 * objects that match an identifier.
 */
public class AliasList implements Listener<AliasEvent>
{
    private final static Logger mLog = LoggerFactory.getLogger(AliasList.class);
    private Map<Protocol,TalkgroupAliasList> mTalkgroupProtocolMap = new HashMap<>();
    private Map<Protocol,RadioAliasList> mRadioProtocolMap = new HashMap<>();
    private Map<String,Alias> mESNMap = new HashMap<>();
    private Map<Integer,Alias> mUnitStatusMap = new HashMap<>();
    private Map<Integer,Alias> mUserStatusMap = new HashMap<>();
    private Map<ToneSequence,Alias> mToneSequenceMap = new HashMap<>();
    private boolean mHasAliasActions = false;
    private String mName;
    private ObservableList<Alias> mAliases = FXCollections.observableArrayList(Alias.extractor());

    /**
     * List of aliases where all aliases share the same list name.  Contains
     * several methods for alias lookup from identifier values, like talkgroups.
     *
     * Responds to alias change events to keep the internal alias list updated.
     */
    public AliasList(String name)
    {
        mName = name;
    }

    /**
     * Observable list of aliases contained in this alias list
     */
    public ObservableList<Alias> aliases()
    {
        return mAliases;
    }

    /**
     * Adds the alias to this list
     */
    public void addAlias(Alias alias)
    {
        if(alias != null)
        {
            for(AliasID aliasID : alias.getAliasIdentifiers())
            {
                addAliasID(aliasID, alias);
            }
        }

        if(alias.hasActions())
        {
            mHasAliasActions = true;
        }

        mAliases.add(alias);
    }

    /**
     * Adds the alias and alias identifier to the internal type mapping.
     */
    private void addAliasID(AliasID id, Alias alias)
    {
        if(id.isValid())
        {
            try
            {
                switch(id.getType())
                {
                    case TALKGROUP:
                        Talkgroup talkgroup = (Talkgroup)id;

                        TalkgroupAliasList talkgroupAliasList = mTalkgroupProtocolMap.get(talkgroup.getProtocol());

                        if(talkgroupAliasList == null)
                        {
                            talkgroupAliasList = new TalkgroupAliasList();
                            mTalkgroupProtocolMap.put(talkgroup.getProtocol(), talkgroupAliasList);
                        }

                        talkgroupAliasList.add(talkgroup, alias);
                        break;
                    case TALKGROUP_RANGE:
                        TalkgroupRange talkgroupRange = (TalkgroupRange)id;

                        TalkgroupAliasList talkgroupRangeAliasList = mTalkgroupProtocolMap.get(talkgroupRange.getProtocol());

                        if(talkgroupRangeAliasList == null)
                        {
                            talkgroupRangeAliasList = new TalkgroupAliasList();
                            mTalkgroupProtocolMap.put(talkgroupRange.getProtocol(), talkgroupRangeAliasList);
                        }

                        talkgroupRangeAliasList.add(talkgroupRange, alias);
                        break;
                    case RADIO_ID:
                        Radio radio = (Radio)id;

                        RadioAliasList radioAliasList = mRadioProtocolMap.get(radio.getProtocol());

                        if(radioAliasList == null)
                        {
                            radioAliasList = new RadioAliasList();
                            mRadioProtocolMap.put(radio.getProtocol(), radioAliasList);
                        }

                        radioAliasList.add(radio, alias);
                        break;
                    case RADIO_ID_RANGE:
                        RadioRange radioRange = (RadioRange)id;

                        RadioAliasList radioRangeAliasList = mRadioProtocolMap.get(radioRange.getProtocol());

                        if(radioRangeAliasList == null)
                        {
                            radioRangeAliasList = new RadioAliasList();
                            mRadioProtocolMap.put(radioRange.getProtocol(), radioRangeAliasList);
                        }

                        radioRangeAliasList.add(radioRange, alias);
                        break;
                    case ESN:
                        String esn = ((Esn) id).getEsn();

                        if(esn != null && !esn.isEmpty())
                        {
                            mESNMap.put(esn.toLowerCase(), alias);
                        }
                        break;
                    case STATUS:
                        mUserStatusMap.put(((UserStatusID)id).getStatus(), alias);
                        break;
                    case UNIT_STATUS:
                        mUnitStatusMap.put(((UnitStatusID)id).getStatus(), alias);
                        break;
                    case TONES:
                        ToneSequence toneSequence = ((TonesID)id).getToneSequence();

                        if(toneSequence != null)
                        {
                            if(mToneSequenceMap.containsKey(toneSequence))
                            {
                                Alias existing = mToneSequenceMap.get(toneSequence);

                                mLog.warn("Alias List Error. Can't map alias [" + alias.getName() +
                                    "] to tone Sequence [" + toneSequence +
                                    "] - that sequence is already mapped to alias [" +
                                    (existing.getName() != null ? existing.getName() : "alias without a name") + "]");
                            }
                            else
                            {
                                mToneSequenceMap.put(toneSequence, alias);
                            }
                        }
                        break;
                }
            }
            catch(Exception e)
            {
                mLog.error("Couldn't add alias ID " + id + " for alias " + alias);
            }
        }
    }

    /**
     * Removes the alias and alias identifier to the internal type mapping.
     */
    private void removeAliasID(AliasID id, Alias alias)
    {
        try
        {
            switch(id.getType())
            {
                case TALKGROUP:
                    Talkgroup talkgroup = (Talkgroup)id;

                    TalkgroupAliasList talkgroupAliasList = mTalkgroupProtocolMap.get(talkgroup.getProtocol());

                    if(talkgroupAliasList != null)
                    {
                        talkgroupAliasList.remove(talkgroup, alias);
                    }
                    break;
                case TALKGROUP_RANGE:
                    TalkgroupRange talkgroupRange = (TalkgroupRange)id;

                    TalkgroupAliasList talkgroupRangeAliasList = mTalkgroupProtocolMap.get(talkgroupRange.getProtocol());

                    if(talkgroupRangeAliasList != null)
                    {
                        talkgroupRangeAliasList.remove(talkgroupRange, alias);
                    }
                    break;
                case RADIO_ID:
                    Radio radio = (Radio)id;

                    RadioAliasList radioAliasList = mRadioProtocolMap.get(radio.getProtocol());

                    if(radioAliasList != null)
                    {
                        radioAliasList.remove(radio, alias);
                    }
                    break;
                case RADIO_ID_RANGE:
                    RadioRange radioRange = (RadioRange)id;

                    RadioAliasList radioRangeAliasList = mRadioProtocolMap.get(radioRange.getProtocol());

                    if(radioRangeAliasList != null)
                    {
                        radioRangeAliasList.remove(radioRange, alias);
                    }
                    break;
                case ESN:
                    String esn = ((Esn) id).getEsn();

                    if(esn != null && !esn.isEmpty())
                    {
                        String key = esn.toLowerCase();

                        if(mESNMap.containsKey(key) && mESNMap.get(key) == alias)
                        {
                            mESNMap.remove(key);
                        }
                    }
                    break;
                case STATUS:
                    int status = ((UserStatusID)id).getStatus();

                    if(mUserStatusMap.containsKey(status) && mUserStatusMap.get(status) == alias)
                    {
                        mUserStatusMap.remove(status);
                    }
                    break;
                case UNIT_STATUS:
                    int unitStatus = ((UnitStatusID)id).getStatus();

                    if(mUnitStatusMap.containsKey(unitStatus) && mUnitStatusMap.get(unitStatus) == alias)
                    {
                        mUnitStatusMap.remove(unitStatus);
                    }
                    break;
                case TONES:
                    ToneSequence toneSequence = ((TonesID)id).getToneSequence();

                    if(toneSequence != null && mToneSequenceMap.containsKey(toneSequence) &&
                        mToneSequenceMap.get(toneSequence) == alias)
                    {
                        mToneSequenceMap.remove(toneSequence);
                    }
                    break;
            }
        }
        catch(Exception e)
        {
            mLog.error("Couldn't remove alias ID " + id + " for alias " + alias, e);
        }
    }

    /**
     * Removes the alias from this list
     */
    public void removeAlias(Alias alias)
    {
        for(AliasID aliasID: alias.getAliasIdentifiers())
        {
            removeAliasID(aliasID, alias);
        }

        mAliases.remove(alias);
    }

    /**
     * Lookup alias by ESN
     */
    public Alias getESNAlias(String esn)
    {
        Alias alias = null;

        if(esn != null)
        {
            alias = mESNMap.get(esn);
        }

        return alias;
    }

    /**
     * Alias list name
     */
    public String toString()
    {
        return mName;
    }

    /**
     * Alias list name
     */
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    public String getName()
    {
        return mName;
    }

    /**
     * Indicates if this alias list has a non-null, non-empty name
     */
    private boolean hasName()
    {
        return mName != null && !mName.isEmpty();
    }

    /**
     * Receive alias change event notifications and modify this list accordingly
     */
    @Override
    public void receive(AliasEvent event)
    {
        if(hasName())
        {
            Alias alias = event.getAlias();

            switch(event.getEvent())
            {
                case ADD:
                    if(alias.getAliasListName() != null && getName().equalsIgnoreCase(alias.getAliasListName()))
                    {
                        addAlias(alias);
                    }
                    break;
                case CHANGE:
                    if(alias.getAliasListName() != null && getName().equalsIgnoreCase(alias.getAliasListName()))
                    {
                        removeAlias(alias);
                        addAlias(alias);
                    }
                    break;
                case DELETE:
                    if(alias.getAliasListName() != null && getName().equalsIgnoreCase(alias.getAliasListName()))
                    {
                        removeAlias(alias);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Returns an optional alias that is associated with the identifier
      * @param identifier to alias
     * @return list of alias or empty list
     */
    public List<Alias> getAliases(Identifier identifier)
    {
        if(identifier != null)
        {
            switch(identifier.getForm())
            {
                case TALKGROUP:
                    TalkgroupIdentifier talkgroup = (TalkgroupIdentifier)identifier;

                    TalkgroupAliasList talkgroupAliasList = mTalkgroupProtocolMap.get(identifier.getProtocol());

                    if(talkgroupAliasList != null)
                    {
                        return toList(talkgroupAliasList.getAlias(talkgroup));
                    }
                    break;
                case PATCH_GROUP:
                    PatchGroupIdentifier patchGroupIdentifier = (PatchGroupIdentifier)identifier;
                    PatchGroup patchGroup = patchGroupIdentifier.getValue();

                    TalkgroupAliasList patchGroupAliasList = mTalkgroupProtocolMap.get(patchGroupIdentifier.getProtocol());

                    if(patchGroupAliasList != null)
                    {
                        List<Alias> aliases = new ArrayList<>();

                        Alias alias = patchGroupAliasList.getAlias(patchGroup.getPatchGroup());

                        if(alias != null)
                        {
                            aliases.add(alias);
                        }

                        for(TalkgroupIdentifier patchedGroup: patchGroup.getPatchedGroupIdentifiers())
                        {
                            Alias patchedAlias = patchGroupAliasList.getAlias(patchedGroup);

                            if(patchedAlias != null && !aliases.contains(patchedAlias))
                            {
                                aliases.add(patchedAlias);
                            }
                        }

                        return aliases;
                    }
                    break;
                case RADIO:
                    RadioIdentifier radio = (RadioIdentifier)identifier;

                    RadioAliasList radioAliasList = mRadioProtocolMap.get(identifier.getProtocol());

                    if(radioAliasList != null)
                    {
                        return toList(radioAliasList.getAlias(radio));
                    }
                    break;
                case ESN:
                    if(identifier instanceof ESNIdentifier)
                    {
                        return toList(getESNAlias(((ESNIdentifier)identifier).getValue()));
                    }
                    break;
                case UNIT_STATUS:
                    if(identifier instanceof UnitStatusIdentifier)
                    {
                        int status = ((UnitStatusIdentifier)identifier).getValue();
                        return toList(mUserStatusMap.get(status));
                    }
                    break;
                case USER_STATUS:
                    if(identifier instanceof UserStatusIdentifier)
                    {
                        int status = ((UserStatusIdentifier)identifier).getValue();
                        return toList(mUserStatusMap.get(status));
                    }
                    break;
                case TONE:
                    if(identifier instanceof ToneIdentifier)
                    {
                        ToneSequence toneSequence = ((ToneIdentifier)identifier).getValue();

                        if(toneSequence != null && toneSequence.hasTones())
                        {
                            for(Map.Entry<ToneSequence,Alias> entry: mToneSequenceMap.entrySet())
                            {
                                if(entry.getKey().isContainedIn(toneSequence))
                                {
                                    return toList(entry.getValue());
                                }
                            }
                        }
                    }
                    break;
            }
        }

        return Collections.emptyList();
    }

    private static List<Alias> toList(Alias alias)
    {
        if(alias != null)
        {
            List<Alias> aliases = new ArrayList<>();
            aliases.add(alias);
            return aliases;
        }

        return Collections.emptyList();
    }

    /**
     * Indicates if any of the identifiers contain a broadcast channel for streaming of audio.
     * @param identifierCollection to inspect
     * @return true if the identifier collection is designated for streaming to one or more channels.
     */
    public boolean isStreamable(IdentifierCollection identifierCollection)
    {
        for(Identifier identifier: identifierCollection.getIdentifiers())
        {
            List<Alias> aliases = getAliases(identifier);

            for(Alias alias: aliases)
            {
                if(alias != null && alias.isStreamable())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Indicates if any of the identifiers have been identified for recording.
     * @param identifierCollection to inspect
     * @return true if recordable.
     */
    public boolean isRecordable(IdentifierCollection identifierCollection)
    {
        for(Identifier identifier: identifierCollection.getIdentifiers())
        {
            List<Alias> aliases = getAliases(identifier);

            for(Alias alias: aliases)
            {
                if(alias != null && alias.isRecordable())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Indicates if any of the aliases in this list have an associated alias action
     */
    public boolean hasAliasActions()
    {
        return mHasAliasActions;
    }

    /**
     * Returns the lowest audio playback priority specified by aliases for identifiers in the
     * identifier collection.
     *
     * @param identifierCollection to inspect for audio priority
     * @return audio playback priority
     */
    public int getAudioPlaybackPriority(IdentifierCollection identifierCollection)
    {
        int priority = Priority.DEFAULT_PRIORITY;

        for(Identifier identifier: identifierCollection.getIdentifiers())
        {
            List<Alias> aliases = getAliases(identifier);

            for(Alias alias: aliases)
            {
                if(alias != null && alias.getPlaybackPriority() < priority)
                {
                    priority = alias.getPlaybackPriority();
                }
            }
        }

        return priority;
    }

    /**
     * Returns a list of streaming broadcast channels specified for any of the identifiers in the collection.
     *
     * @return list of broadcast channels or an empty list
     */
    public List<BroadcastChannel> getBroadcastChannels(IdentifierCollection identifierCollection)
    {
        List<BroadcastChannel> channels = new ArrayList<>();

        for(Identifier identifier: identifierCollection.getIdentifiers())
        {
            List<Alias> aliases = getAliases(identifier);

            for(Alias alias: aliases)
            {
                if(alias != null && alias.isStreamable())
                {
                    for(BroadcastChannel broadcastChannel: alias.getBroadcastChannels())
                    {
                        if(!channels.contains(broadcastChannel))
                        {
                            channels.add(broadcastChannel);
                        }
                    }
                }
            }
        }

        return channels;
    }

    /**
     * Listing of talkgroups and ranges for a specific protocol
     */
    public class TalkgroupAliasList
    {
        private Map<Integer,Alias> mTalkgroupAliasMap = new TreeMap<>();
        private Map<TalkgroupRange, Alias> mTalkgroupRangeAliasMap = new HashMap<>();

        public TalkgroupAliasList()
        {
        }

        public Alias getAlias(TalkgroupIdentifier identifier)
        {
            int value = identifier.getValue();

            if(mTalkgroupAliasMap.containsKey(value))
            {
                return mTalkgroupAliasMap.get(value);
            }

            for(TalkgroupRange talkgroupRange: mTalkgroupRangeAliasMap.keySet())
            {
                if(talkgroupRange.contains(value))
                {
                    return mTalkgroupRangeAliasMap.get(talkgroupRange);
                }
            }

            return null;
        }

        public void add(Talkgroup talkgroup, Alias alias)
        {
            //Detect talkgroup collisions
            if(mTalkgroupAliasMap.containsKey(talkgroup.getValue()))
            {
                Alias existing = mTalkgroupAliasMap.get(talkgroup.getValue());

                if(!existing.equals(alias))
                {
                    mLog.warn("Alias [" + alias.getName() + "] talkgroup [" + talkgroup.getValue() +
                        "] has the same talkgroup value as alias [" + existing.getName() +
                        "] - alias [" + alias.getName() + "] will be used for alias list [" + getName() + "]");
                }
            }

            mTalkgroupAliasMap.put(talkgroup.getValue(), alias);
        }

        public void add(TalkgroupRange talkgroupRange, Alias alias)
        {
            //Log warning if the new talkgroup range overlaps with any existing ranges
            for(Map.Entry<TalkgroupRange,Alias> entry: mTalkgroupRangeAliasMap.entrySet())
            {
                if(talkgroupRange.overlaps(entry.getKey()) && !entry.getValue().equals(alias))
                {
                    mLog.warn("Alias [" + alias.getName() + "] with talkgroup range [" + talkgroupRange.toString() +
                        "] overlaps with alias [" + entry.getValue().getName() +
                        "] with talkgroup range [" + entry.getKey().toString() + "] for alias list [" + getName() + "]");
                }
            }

            mTalkgroupRangeAliasMap.put(talkgroupRange, alias);
        }

        public void remove(Talkgroup talkgroup, Alias alias)
        {
            if(mTalkgroupAliasMap.containsKey(talkgroup.getValue()) && mTalkgroupAliasMap.get(talkgroup.getValue()) == alias)
            {
                mTalkgroupAliasMap.remove(talkgroup.getValue());
            }
        }

        public void remove(TalkgroupRange talkgroupRange, Alias alias)
        {
            //Only remove the entry if both the key and the value match
            if(mTalkgroupRangeAliasMap.containsKey(talkgroupRange) && mTalkgroupRangeAliasMap.get(talkgroupRange) == alias)
            {
                mTalkgroupRangeAliasMap.remove(talkgroupRange);
            }
        }
    }

    /**
     * Listing of radio IDs and ranges for a specific protocol
     */
    public class RadioAliasList
    {
        private Map<Integer,Alias> mRadioAliasMap = new TreeMap<>();
        private Map<RadioRange, Alias> mRadioRangeAliasMap = new HashMap<>();

        public RadioAliasList()
        {
        }

        public Alias getAlias(RadioIdentifier identifier)
        {
            int value = identifier.getValue();

            if(mRadioAliasMap.containsKey(value))
            {
                return mRadioAliasMap.get(value);
            }

            for(RadioRange radioRange: mRadioRangeAliasMap.keySet())
            {
                if(radioRange.contains(value))
                {
                    return mRadioRangeAliasMap.get(radioRange);
                }
            }

            return null;
        }

        public void add(Radio radio, Alias alias)
        {
            //Detect collisions
            if(mRadioAliasMap.containsKey(radio.getValue()))
            {
                Alias existing = mRadioAliasMap.get(radio.getValue());

                if(!existing.equals(alias))
                {
                    mLog.warn("Alias [" + alias.getName() + "] radio ID [" + radio.getValue() +
                        "] has the same value as alias [" + existing.getName() +
                        "] - alias [" + alias.getName() + "] will be used for alias list [" + getName() + "]");
                }
            }

            mRadioAliasMap.put(radio.getValue(), alias);
        }

        public void add(RadioRange radioRange, Alias alias)
        {
            //Log warning if the new range overlaps with any existing ranges
            for(Map.Entry<RadioRange,Alias> entry: mRadioRangeAliasMap.entrySet())
            {
                if(radioRange.overlaps(entry.getKey()) && !entry.getValue().equals(alias))
                {
                    mLog.warn("Alias [" + alias.getName() + "] with radio ID range [" + radioRange.toString() +
                        "] overlaps with alias [" + entry.getValue().getName() +
                        "] with range [" + entry.getKey().toString() + "] for alias list [" + getName() + "]");
                }
            }

            mRadioRangeAliasMap.put(radioRange, alias);
        }

        public void remove(Radio radio, Alias alias)
        {
            //Only remove the entry if both the key and the value match
            if(mRadioAliasMap.containsKey(radio.getValue()) && mRadioAliasMap.get(radio.getValue()) == alias)
            {
                mRadioAliasMap.remove(radio.getValue());
            }
        }

        public void remove(RadioRange radioRange, Alias alias)
        {
            //Only remove the entry if both the key and the value match
            if(mRadioRangeAliasMap.containsKey(radioRange) && mRadioRangeAliasMap.get(radioRange) == alias)
            {
                mRadioRangeAliasMap.remove(radioRange);
            }
        }
    }
}
