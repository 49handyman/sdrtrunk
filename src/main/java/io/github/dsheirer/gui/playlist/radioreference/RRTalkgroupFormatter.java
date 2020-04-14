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

import io.github.dsheirer.identifier.Identifier;
import io.github.dsheirer.preference.UserPreferences;
import io.github.dsheirer.rrapi.type.System;
import io.github.dsheirer.rrapi.type.Talkgroup;

/**
 * Formats a radio reference talkgroup value according to user preferences
 */
public class RRTalkgroupFormatter
{
    public static String format(Talkgroup talkgroup, System system, UserPreferences userPreferences)
    {
        if(talkgroup.isLtr())
        {
            int value = talkgroup.getDecimalValue();
            int area = (value >= 100000 ? 1 : 0);
            int home = (value / 1000);
            int group = (value % 1000);
//            return LTRTalkgroup.encode(LTRTalkgroup.encode(area, home, group));
        }

        return null;
    }

    private static Identifier getIdentifier(Talkgroup talkgroup, System system)
    {
        return null;
    }
}
