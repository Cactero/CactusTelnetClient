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

import java.awt.Color;
import java.util.regex.Pattern;

public class BTC_TelnetMessage extends BTC_ConsoleMessage
{
    private static final String PATTERN_PREFIX = "^:\\[.+? INFO\\]: ";
    public static final Color PURPLE = new Color(128, 0, 128);
    public static final Color DARK_GREEN = new Color(86, 130, 3);

    private static final Pattern INFO_MESSAGE = Pattern.compile(PATTERN_PREFIX);

    private final BTC_LogMessageType messageType;

    public BTC_TelnetMessage(String message)
    {
        super(message);
        this.messageType = BTC_LogMessageType.getMessageType(message);
    }

    public BTC_LogMessageType getMessageType()
    {
        return this.messageType;
    }

    public boolean isInfoMessage()
    {
        return INFO_MESSAGE.matcher(this.getMessage()).find();
    }

    private boolean isType(final BTC_LogMessageType checkType)
    {
        return this.messageType != null ? this.messageType == checkType : false;
    }

    public boolean skip()
    {
        final BTC_MainPanel mainPanel = BukkitTelnetClient.mainPanel;

        if (mainPanel == null)
        {
            return false;
        }

        if (mainPanel.getChkShowChatOnly().isSelected())
        {
            return !isType(BTC_LogMessageType.CHAT_MESSAGE)
                    && !isType(BTC_LogMessageType.CSAY_MESSAGE)
                    && !isType(BTC_LogMessageType.SAY_MESSAGE)
                    && !isType(BTC_LogMessageType.LEGACY_CHAT_MESSAGE)
                    && !isType(BTC_LogMessageType.DISCORD_CHAT_MESSAGE)
                    && !isType(BTC_LogMessageType.STAFFSAY_MESSAGE);
        }

        if (mainPanel.getChkIgnorePreprocessCommands().isSelected() && isType(BTC_LogMessageType.PREPROCESS_COMMAND))
        {
            return true;
        }

        if (mainPanel.getChkIgnoreServerCommands().isSelected() && isType(BTC_LogMessageType.ISSUED_SERVER_COMMAND))
        {
            return true;
        }

        if (mainPanel.getChkIgnoreWarnings().isSelected() && isType(BTC_LogMessageType.WARNING_MESSAGE))
        {
            return true;
        }

        if (mainPanel.getChkIgnoreErrors().isSelected() && isType(BTC_LogMessageType.ERROR_MESSAGE))
        {
            return true;
        }

        if (mainPanel.getChkIgnoreErrors().isSelected() && isType(BTC_LogMessageType.TRACEBACK))
        {
            return true;
        }

        if (mainPanel.getChkShowStaffChatOnly().isSelected() && !isType(BTC_LogMessageType.STAFFSAY_MESSAGE))
        {
            return true;
        }

        if (mainPanel.getChkIgnoreAsyncWorldEdit().isSelected() && isType(BTC_LogMessageType.ASYNC_WORLD_EDIT))
        {
            return true;
        }

        if (mainPanel.getChkIgnoreGuildChat().isSelected() && isType(BTC_LogMessageType.GUILD_CHAT))
        {
            return true;
        }

        return false;
    }

    @Override
    public Color getColor()
    {
        if (this.messageType == null)
        {
            return super.getColor();
        }
        else
        {
            return this.messageType.getColor();
        }
    }

    public static enum BTC_LogMessageType
    {
        CHAT_MESSAGE(PATTERN_PREFIX + ".+? \u00bb", Color.BLUE),
        DISCORD_CHAT_MESSAGE(PATTERN_PREFIX + "\\[Discord\\] .+?:", Color.BLUE),
        LEGACY_CHAT_MESSAGE(PATTERN_PREFIX + "\\<.+?\\>", Color.BLUE),
        SAY_MESSAGE(PATTERN_PREFIX + "\\[Server:", Color.BLUE),
        CSAY_MESSAGE(PATTERN_PREFIX + "\\[CONSOLE\\]<", Color.BLUE),
        PRIVATE_MESSAGE(PATTERN_PREFIX + "\\[.+? -> .+?\\]", Color.BLUE),
        TRACEBACK("	at ", Color.RED),
        //
        WARNING_MESSAGE("^:\\[.+? WARN\\]: ", Color.YELLOW),
        ERROR_MESSAGE("^:\\[.+? ERROR\\]: ", Color.RED),
        //
        STAFFSAY_MESSAGE(PATTERN_PREFIX + "\\[STAFF\\] ", PURPLE),
        //
        WORLD_EDIT(PATTERN_PREFIX + "WorldEdit: ", Color.RED),
        ASYNC_WORLD_EDIT(PATTERN_PREFIX + "\\[AsyncWorldEdit\\] "),
        //
        PREPROCESS_COMMAND(PATTERN_PREFIX + "\\[PREPROCESS_COMMAND\\] ", DARK_GREEN),
        //
        ISSUED_SERVER_COMMAND(PATTERN_PREFIX + ".+? issued server command: "),
        //
        GUILD_CHAT(PATTERN_PREFIX + "\\[TFGuilds\\] \\[Guild Chat | .+?\\] .+?: ", DARK_GREEN);

        private final Pattern messagePattern;
        private final Color color;

        private BTC_LogMessageType(final String messagePatternStr)
        {
            this.messagePattern = Pattern.compile(messagePatternStr);
            this.color = BukkitTelnetClient.themes.checkColor(Color.BLACK);
        }

        private BTC_LogMessageType(final String messagePatternStr, final Color color)
        {
            this.messagePattern = Pattern.compile(messagePatternStr);
            this.color = BukkitTelnetClient.themes.checkColor(color);
        }

        public Pattern getMessagePattern()
        {
            return this.messagePattern;
        }

        public Color getColor()
        {
            return BukkitTelnetClient.themes.checkColor(this.color);
        }

        public static BTC_LogMessageType getMessageType(final String message)
        {
            for (final BTC_LogMessageType type : values())
            {
                if (type.getMessagePattern().matcher(message).find())
                {
                    return type;
                }
            }
            return null;
        }
    }
}
