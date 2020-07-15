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
import java.awt.Toolkit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Timer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.net.telnet.TelnetClient;

public class BTC_ConnectionManager
{
    private static final Pattern LOGIN_MESSAGE = Pattern.compile("\\[.+?@BukkitTelnet\\]\\$ Logged in as (.+)\\.");

    private final TelnetClient telnetClient = new TelnetClient();
    private Thread connectThread;
    private String hostname;
    private int port;
    private boolean canDoDisconnect = false;
    private String loginName;

    public BTC_ConnectionManager()
    {
    }

    public void triggerConnect(final String hostname, final int port)
    {
        final BTC_MainPanel btc = BukkitTelnetClient.mainPanel;

        btc.getBtnConnect().setEnabled(false);
        btc.getTxtServer().setEnabled(false);
        btc.toggleComponents(false);
        btc.getBtnDisconnect().setEnabled(true);

        btc.writeToConsole(new BTC_ConsoleMessage("Connecting to " + hostname + ":" + port + "...", Color.RED));

        this.hostname = hostname;
        this.port = port;
        this.loginName = null;
        updateTitle(true);
        BTC_MainPanel.setIconImage(BTC_MainPanel.getWindows()[0], "Connected");

        startConnectThread();
    }
    
    public void triggerConnect(final String hostnameAndPort)
    {
        final String[] parts = StringUtils.split(hostnameAndPort, ":");

        if (parts.length <= 1)
        {
            this.triggerConnect(parts[0], 23);
        }
        else
        {
            int _port = 23;

            try
            {
                _port = Integer.parseInt(parts[1]);
            }
            catch (NumberFormatException ex)
            {
            }

            this.triggerConnect(parts[0], _port);
        }
    }

    public void triggerDisconnect()
    {
        if (this.canDoDisconnect)
        {
            this.canDoDisconnect = false;

            try
            {
                this.telnetClient.disconnect();
            }
            catch (IOException ex)
            {
                BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void finishDisconnect()
    {
        final BTC_MainPanel btc = BukkitTelnetClient.mainPanel;

        btc.getBtnConnect().setEnabled(true);
        btc.getTxtServer().setEnabled(true);
        btc.getBtnDisconnect().setEnabled(false);
        btc.getBtnSend().setEnabled(false);
        btc.getTxtCommand().setEnabled(false);
        btc.toggleComponents(false);

        loginName = null;

        updateTitle(false);
        
        BTC_MainPanel.setIconImage(BTC_MainPanel.getWindows()[0], "Disconnected");
        
        btc.writeToConsole(new BTC_ConsoleMessage("Disconnected.", Color.RED));
    }

    public void sendCommand(final String text)
    {
        sendCommand(text, true);
    }

    public void sendCommand(final String text, final boolean verbose)
    {
        try
        {
            if (verbose)
            {
                BukkitTelnetClient.mainPanel.writeToConsole(new BTC_ConsoleMessage(":" + text));
            }

            final OutputStream out = this.telnetClient.getOutputStream();
            if (out == null)
            {
                return;
            }

            this.telnetClient.getOutputStream().write((text + "\r\n").getBytes(StandardCharsets.UTF_8));
            this.telnetClient.getOutputStream().flush();
        }
        catch (IOException ex)
        {
            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public void sendDelayedCommand(final String text, final boolean verbose, final int delay)
    {
        final Timer timer = new Timer(delay, event -> sendCommand(text, verbose));
        timer.setRepeats(false);
        timer.start();
    }

    private void startConnectThread()
    {
        if (this.connectThread != null)
        {
            return;
        }

        this.connectThread = new Thread(() ->
        {
            final BTC_MainPanel btc = BukkitTelnetClient.mainPanel;

            try
            {
                BTC_ConnectionManager.this.telnetClient.connect(hostname, port);
                BTC_ConnectionManager.this.canDoDisconnect = true;

                btc.getBtnSend().setEnabled(true);
                btc.getTxtCommand().setEnabled(true);
                btc.toggleComponents(true);
                btc.getTxtCommand().requestFocusInWindow();

                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(telnetClient.getInputStream(), StandardCharsets.UTF_8)))
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        String _loginName = null;
                        if (BTC_ConnectionManager.this.loginName == null)
                        {
                            _loginName = checkForLoginMessage(line);
                        }
                        if (_loginName != null)
                        {
                            BTC_ConnectionManager.this.loginName = _loginName;
                            updateTitle(true);
                            sendDelayedCommand("telnet.enhanced", false, 100);
                            sendDelayedCommand("telnet.enhancedplus", false, 100);
                        }
                        else
                        {
                            final PlayerInfo selectedPlayer = btc.getSelectedPlayer();
                            String selectedPlayerName = null;
                            if (selectedPlayer != null)
                            {
                                selectedPlayerName = selectedPlayer.getName();
                            }

                            if (BTC_PlayerListDecoder.checkForPlayerListMessage(line, btc.getPlayerList()))
                            {
                                btc.updatePlayerList(selectedPlayerName);
                            }
                            else
                            {
                                boolean isUsageMessage = BTC_UsageDecoder.checkForUsageMessage(line);
                                if (!isUsageMessage)
                                {
                                    final BTC_TelnetMessage message = new BTC_TelnetMessage(line);
                                    if (!message.skip())
                                    {
                                        btc.writeToConsole(message);
                                    }
                                }
                            }
                        }
                    }
                }

                triggerDisconnect();
            }
            catch (IOException ex)
            {
                btc.writeToConsole(new BTC_ConsoleMessage(ex.getMessage() + System.lineSeparator() + ExceptionUtils.getStackTrace(ex)));
            }

            finishDisconnect();

            BTC_ConnectionManager.this.connectThread = null;
        });
        this.connectThread.start();
    }

    public static final String checkForLoginMessage(String message)
    {
        final Matcher matcher = LOGIN_MESSAGE.matcher(message);
        if (matcher.find())
        {
            return matcher.group(1);
        }

        return null;
    }

    public final void updateTitle(final boolean isConnected)
    {
        final BTC_MainPanel mainPanel = BukkitTelnetClient.mainPanel;
        if (mainPanel == null)
        {
            return;
        }

        String title;

        if (isConnected)
        {
            if (loginName == null)
            {
                title = String.format("ZeroTelnetClient - %s - %s:%d", BukkitTelnetClient.VERSION, hostname, port);
            }
            else
            {
                title = String.format("ZeroTelnetClient - %s - %s@%s:%d", BukkitTelnetClient.VERSION, loginName, hostname, port);
            }
        }
        else
        {
            title = String.format("ZeroTelnetClient - %s - Disconnected", BukkitTelnetClient.VERSION);
        }

        mainPanel.setTitle(title);
    }
}
