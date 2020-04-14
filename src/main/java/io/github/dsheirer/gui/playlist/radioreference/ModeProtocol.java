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

import io.github.dsheirer.protocol.Protocol;
import io.github.dsheirer.rrapi.type.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of radio reference modes to corresponding sdrtrunk decoder type
 */
public enum ModeProtocol
{
    APCO25("p25", Protocol.APCO25),
    DMR("dmr", null),
    DSTAR("d-star", null),
    LTR("ltr", Protocol.LTR),
    LTR_NET("ltr-net", Protocol.LTR),
    MPT1327("mpt", Protocol.MPT1327),
    NXDN("nxdn", null),
    PASSPORT("passport", Protocol.PASSPORT),
    YAESU_SYSTEM_FUSION("ysf", null),
    UNKNOWN("UNKNOWN", null);

    private String mValue;
    private Protocol mProtocol;

    ModeProtocol(String value, Protocol protocol)
    {
        mValue = value;
        mProtocol = protocol;
    }

    private static final Logger mLog = LoggerFactory.getLogger(ModeProtocol.class);

    public String getValue()
    {
        return mValue;
    }

    public Protocol getProtocol()
    {
        return mProtocol;
    }

    public boolean hasProtocol()
    {
        return mProtocol != null;
    }

    /**
     * Lookup the entry that matches the mode
     * @param mode to match
     * @return matching entry or UNKNOWN
     */
    public static ModeProtocol get(Mode mode)
    {
        if(mode != null)
        {
            for(ModeProtocol modeDecoder: ModeProtocol.values())
            {
                if(modeDecoder.getValue().contentEquals(mode.getName().toLowerCase()))
                {
                    return modeDecoder;
                }
            }

            mLog.warn("Unrecognized Radio Reference Mode [" + mode.getName() + " ID:" + mode.getModeId() + "]");
        }

        return UNKNOWN;
    }
}
