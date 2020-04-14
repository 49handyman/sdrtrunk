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

import io.github.dsheirer.module.decode.DecoderType;
import io.github.dsheirer.protocol.Protocol;
import io.github.dsheirer.rrapi.type.Flavor;
import io.github.dsheirer.rrapi.type.System;
import io.github.dsheirer.rrapi.type.Type;
import io.github.dsheirer.rrapi.type.Voice;

import java.util.Map;

public class SystemDecoder
{
    private Map<Integer,Type> mTypeMap;
    private Map<Integer,Flavor> mFlavorMap;
    private Map<Integer,Voice> mVoiceMap;

    public SystemDecoder(Map<Integer,Type> typeMap, Map<Integer,Flavor> flavorMap, Map<Integer,Voice> voiceMap)
    {
        mTypeMap = typeMap;
        mFlavorMap = flavorMap;
        mVoiceMap = voiceMap;
    }

    public Type getType(System system)
    {
        return mTypeMap.get(system.getTypeId());
    }

    public Flavor getFlavor(System system)
    {
        return mFlavorMap.get(system.getFlavorId());
    }

    public Voice getVoice(System system)
    {
        return mVoiceMap.get(system.getVoiceId());
    }

    public Protocol getProtocol(System system)
    {
        Type type = getType(system);
        Flavor flavor = getFlavor(system);
        Voice voice = getVoice(system);

        switch(type.getName())
        {
            case "LTR":
                return Protocol.LTR;
            case "MPT-1327":
                return Protocol.MPT1327;
            case "Project 25":
                return Protocol.APCO25;
            case "Motorola":
                if(voice.getName().contentEquals("Analog and APCO-25 Common Air Interface") ||
                    voice.getName().contentEquals("APCO-25 Common Air Interface Exclusive"))
                {
                    return Protocol.APCO25;
                }
                break;
            case "DMR":
            case "NXDN":
            case "EDACS":
            case "TETRA":
            case "Midland CMS":
            case "OpenSky":
            case "iDEN":
            case "SmarTrunk":
            case "Other":
            default:
        }

        return Protocol.UNKNOWN;
    }

    /**
     * Decoder type for the specified system, if supported.
     * @param system requiring a decoder type
     * @return
     */
    public DecoderType getDecoderType(System system)
    {
        Type type = getType(system);
        Flavor flavor = getFlavor(system);
        Voice voice = getVoice(system);

        switch(type.getName())
        {
            case "LTR":
                if(flavor.getName().contentEquals("Net"))
                {
                    return DecoderType.LTR_NET;
                }
                else
                {
                    return DecoderType.LTR_STANDARD;
                }
            case "MPT-1327":
                return DecoderType.MPT1327;
            case "Project 25":
                if(flavor.getName().contentEquals("Phase II"))
                {
                    return DecoderType.P25_PHASE2;
                }
                else
                {
                    return DecoderType.P25_PHASE1;
                }
            case "Motorola":
                if(voice.getName().contentEquals("Analog and APCO-25 Common Air Interface") ||
                   voice.getName().contentEquals("APCO-25 Common Air Interface Exclusive"))
                {
                    return DecoderType.P25_PHASE1;
                }
                break;
            case "DMR":
            case "NXDN":

            case "EDACS":
            case "TETRA":
            case "Midland CMS":
            case "OpenSky":
            case "iDEN":
            case "SmarTrunk":
            case "Other":
            default:
        }

        return null;
    }
}
