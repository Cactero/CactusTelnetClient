/* 
 * Copyright (C) 2012-2017 Steven Lawson
 *
 * This file is part of FreedomTelnetClient.
 *
 * FreedomTelnetClient is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.StevenLawson.BukkitTelnetClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.*;

public class BTC_PlayerListDecoder
{
    private static final Pattern PLAYER_LIST_MESSAGE = Pattern.compile(":\\[.+@BukkitTelnet\\]\\$ playerList~(.+)");

    private BTC_PlayerListDecoder()
    {
        throw new AssertionError();
    }

    public static final boolean checkForPlayerListMessage(final String message, final List<PlayerInfo> playerList)
    {
        final Matcher matcher = PLAYER_LIST_MESSAGE.matcher(message);
        if (matcher.find())
        {
            final String data = matcher.group(1);
            try
            {
                playerList.clear();

                final JSONObject json = new JSONObject(data);
                final JSONArrayIterable players = new JSONArrayIterable(json.getJSONArray("players"));
                for (JSONObject player : players)
                {
                    final String name = getStringSafe(player, "name");
                    playerList.add(new PlayerInfo(
                            name,
                            getStringSafe(player, "ip"),
                            getStringSafe(player, "displayName"),
                            getStringSafe(player, "uuid"),
                            Boolean.valueOf(getStringSafe(player, "tfm.admin.isAdmin")),
                            Boolean.valueOf(getStringSafe(player, "tfm.admin.isTelnetAdmin")),
                            Boolean.valueOf(getStringSafe(player, "tfm.admin.isSeniorAdmin")),
                            getStringSafe(player, "tfm.playerdata.getTag"),
                            getStringSafe(player, "tfm.essentialsBridge.getNickname")
                    ));
                }

                Collections.sort(playerList, PlayerInfo.getComparator());

                return true;
            }
            catch (JSONException ex)
            {
            }
        }

        return false;
    }

    private static String getStringSafe(JSONObject player, String key)
    {
        try
        {
            return player.getString(key);
        }
        catch (JSONException ex)
        {
            return "null";
        }
    }

    public static final class JSONArrayIterable implements Iterable<JSONObject>
    {
        private final JSONArray array;

        public JSONArrayIterable(JSONArray array)
        {
            this.array = array;
        }

        @Override
        public Iterator<JSONObject> iterator()
        {
            return new Iterator<JSONObject>()
            {
                private int index = 0;
                private final int length = array.length();

                @Override
                public boolean hasNext()
                {
                    return index < length;
                }

                @Override
                public JSONObject next()
                {
                    return array.getJSONObject(index++);
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
