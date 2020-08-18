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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PlayerInfo
{
    public static int numColumns;
    public static String[] columnNames;

    private final String name;
    private final String ip;
    private final String displayName;
    private final String uuid;

    // TFM tags:
    private final boolean staff;
    private final boolean mod;
    private final boolean admin;
    private final String tag;
    private final String nickName;

    static
    {
        final Map<Integer, String> columnNamesMap = new HashMap<>();

        int _numColumns = 0;
        final Method[] declaredMethods = PlayerInfo.class.getDeclaredMethods();
        for (final Method method : declaredMethods)
        {
            final Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
            for (final Annotation annotation : declaredAnnotations)
            {
                if (annotation instanceof PlayerTableColumn)
                {
                    PlayerTableColumn playerInfoTag = (PlayerTableColumn) annotation;
                    columnNamesMap.put(playerInfoTag.column(), playerInfoTag.name());
                    _numColumns++;
                }
            }
        }

        final String[] _columnNames = new String[_numColumns];
        for (int i = 0; i < _numColumns; i++)
        {
            _columnNames[i] = columnNamesMap.get(i);
        }

        columnNames = _columnNames;
        numColumns = _numColumns;
    }

    public PlayerInfo(String name, String ip, String displayName, String uuid, boolean staff, boolean mod, boolean admin, String tag, String nickName)
    {
        this.name = name;
        this.ip = ip;
        this.displayName = displayName;
        this.uuid = uuid;
        this.staff = staff;
        this.mod = mod;
        this.admin = admin;
        this.tag = tag;
        this.nickName = nickName;
    }

    @PlayerTableColumn(name = "Name", column = 0)
    public String getName()
    {
        return name;
    }

    @PlayerTableColumn(name = "IP", column = 1)
    public String getIp()
    {
        return ip;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getUuid()
    {
        return uuid;
    }

    public boolean isStaff()
    {
        return staff;
    }

    public boolean isMod()
    {
        return mod;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    @PlayerTableColumn(name = "Tag", column = 2)
    public String getTag()
    {
        return tag == null || tag.isEmpty() || tag.equalsIgnoreCase("null") ? "" : tag;
    }

    @PlayerTableColumn(name = "Nickname", column = 3)
    public String getNickName()
    {
        return nickName == null || nickName.isEmpty() || nickName.equalsIgnoreCase("null") ? "" : nickName;
    }

    @PlayerTableColumn(name = "Staff Level", column = 4)
    public String getStaffLevel()
    {
        if (isStaff())
        {
            if (isAdmin())
            {
                return "Admin";
            }
            else if (isMod())
            {
                return "Mod";
            }
            else
            {
                return "Trial Mod";
            }
        }
        return "";
    }

    public String getColumnValue(int columnIndex)
    {
        final Method[] declaredMethods = this.getClass().getDeclaredMethods();
        for (final Method method : declaredMethods)
        {
            final Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
            for (final Annotation annotation : declaredAnnotations)
            {
                if (annotation instanceof PlayerTableColumn)
                {
                    PlayerTableColumn playerInfoTag = (PlayerTableColumn) annotation;

                    if (playerInfoTag.column() == columnIndex)
                    {
                        try
                        {
                            final Object value = method.invoke(this);
                            if (value != null)
                            {
                                return value.toString();
                            }
                        }
                        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
                        {
                            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
                        }

                        return "null";
                    }
                }
            }
        }

        return "null";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface PlayerTableColumn
    {
        public String name();

        public int column();
    }

    public static Comparator<PlayerInfo> getComparator()
    {
        return (PlayerInfo a, PlayerInfo b) -> a.getName().compareTo(b.getName());
    }
}