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

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class BTC_ConfigLoader
{
    private static final String SETTINGS_FILE = "ztc_settings.xml";

    private final ServerEntry.ServerEntryList servers = new ServerEntry.ServerEntryList();
    private final PlayerCommandEntry.PlayerCommandEntryList playerCommands = new PlayerCommandEntry.PlayerCommandEntryList();
    private final FavoriteButtonEntry.FavoriteButtonEntryList favoriteButtons = new FavoriteButtonEntry.FavoriteButtonEntryList();
    public final Themes themes = BukkitTelnetClient.themes;
    public boolean filterIgnorePreprocessCommands = false;
    public boolean filterIgnoreServerCommands = false;
    public boolean filterShowChatOnly = false;
    public boolean filterIgnoreWarnings = false;
    public boolean filterIgnoreErrors = false;
    public boolean filterShowAdminChatOnly = false;
    public boolean filterIgnoreAsyncWorldEdit = false;
    public boolean filterIgnoreGuildChat = false;

    public BTC_ConfigLoader()
    {
    }

    public boolean load(boolean verbose)
    {
        File settings = new File(SETTINGS_FILE);

        if (!settings.exists())
        {
            if (extractFileFromJar("/" + SETTINGS_FILE, SETTINGS_FILE))
            {
                if (verbose)
                {
                    System.out.println("Copied default " + SETTINGS_FILE + ".");
                }
            }
        }

        if (settings.exists())
        {
            boolean loadError = loadXML(settings);

            if (verbose)
            {
                if (loadError)
                {
                    System.out.println("Settings loaded with errors.");
                }
                else
                {
                    System.out.println("Settings loaded.");
                }
            }

            return true;
        }
        else
        {
            if (verbose)
            {
                System.out.println("Can't load " + SETTINGS_FILE + ".");
            }
        }

        return false;
    }

    public boolean save()
    {
        return generateXML(new File(SETTINGS_FILE));
    }

    public Collection<PlayerCommandEntry> getCommands()
    {
        return this.playerCommands.getList();
    }

    public Collection<ServerEntry> getServers()
    {
        return this.servers.getList();
    }

    public Collection<FavoriteButtonEntry> getFavoriteButtons()
    {
        return favoriteButtons.getList();
    }

    private boolean generateXML(final File file)
    {
        try
        {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            final Element rootElement = doc.createElement("configuration");
            doc.appendChild(rootElement);

            rootElement.appendChild(this.servers.listToXML(doc));
            rootElement.appendChild(this.playerCommands.listToXML(doc));
            rootElement.appendChild(this.favoriteButtons.listToXML(doc));
            rootElement.appendChild(themes.toXML(doc));
            rootElement.appendChild(filtersToXML(doc));

            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.transform(new DOMSource(doc), new StreamResult(file));

            return true;
        }
        catch (IllegalArgumentException | ParserConfigurationException | TransformerException | DOMException ex)
        {
            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public Element filtersToXML(Document doc)
    {
        Element filters = doc.createElement("filters");

        Element ignorePreprocessCommands = doc.createElement("ignorePreprocessCommands");
        ignorePreprocessCommands.appendChild(doc.createTextNode(String.valueOf(filterIgnorePreprocessCommands)));
        filters.appendChild(ignorePreprocessCommands);

        Element ignoreServerCommands = doc.createElement("ignoreServerCommands");
        ignoreServerCommands.appendChild(doc.createTextNode(String.valueOf(filterIgnoreServerCommands)));
        filters.appendChild(ignoreServerCommands);

        Element showChatOnly = doc.createElement("showChatOnly");
        showChatOnly.appendChild(doc.createTextNode(String.valueOf(filterShowChatOnly)));
        filters.appendChild(showChatOnly);

        Element ignoreWarnings = doc.createElement("ignoreWarnings");
        ignoreWarnings.appendChild(doc.createTextNode(String.valueOf(filterIgnoreWarnings)));
        filters.appendChild(ignoreWarnings);

        Element ignoreErrors = doc.createElement("ignoreErrors");
        ignoreErrors.appendChild(doc.createTextNode(String.valueOf(filterIgnoreErrors)));
        filters.appendChild(ignoreErrors);

        Element showAdminChatOnly = doc.createElement("showAdminChatOnly");
        showAdminChatOnly.appendChild(doc.createTextNode(String.valueOf(filterShowAdminChatOnly)));
        filters.appendChild(showAdminChatOnly);

        Element ignoreAsyncWorldEdit = doc.createElement("ignoreAsyncWorldEdit");
        ignoreAsyncWorldEdit.appendChild(doc.createTextNode(String.valueOf(filterIgnoreAsyncWorldEdit)));
        filters.appendChild(ignoreAsyncWorldEdit);

        Element ignoreGuildChat = doc.createElement("ignoreGuildChat");
        ignoreGuildChat.appendChild(doc.createTextNode(String.valueOf(filterIgnoreGuildChat)));
        filters.appendChild(ignoreGuildChat);

        return filters;
    }

    public void filtersFromXML(Document doc)
    {
        Element filters = (Element)doc.getElementsByTagName("filters").item(0);
        filterIgnorePreprocessCommands = Boolean.valueOf(filters.getElementsByTagName("ignorePreprocessCommands").item(0).getTextContent());
        filterIgnoreServerCommands = Boolean.valueOf(filters.getElementsByTagName("ignoreServerCommands").item(0).getTextContent());
        filterShowChatOnly = Boolean.valueOf(filters.getElementsByTagName("showChatOnly").item(0).getTextContent());
        filterIgnoreWarnings = Boolean.valueOf(filters.getElementsByTagName("ignoreWarnings").item(0).getTextContent());
        filterIgnoreErrors = Boolean.valueOf(filters.getElementsByTagName("ignoreErrors").item(0).getTextContent());
        filterShowAdminChatOnly = Boolean.valueOf(filters.getElementsByTagName("showAdminChatOnly").item(0).getTextContent());
        filterIgnoreAsyncWorldEdit = Boolean.valueOf(filters.getElementsByTagName("ignoreAsyncWorldEdit").item(0).getTextContent());
        filterIgnoreGuildChat = Boolean.valueOf(filters.getElementsByTagName("ignoreGuildChat").item(0).getTextContent());
    }

    private boolean loadXML(final File file)
    {
        boolean hadErrors = false;

        try
        {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();

            if (!this.servers.listFromXML(doc))
            {
                System.out.println("Error loading servers.");
                hadErrors = true;
            }

            if (!this.playerCommands.listFromXML(doc))
            {
                System.out.println("Error loading playerCommands.");
                hadErrors = true;
            }

            if (!this.favoriteButtons.listFromXML(doc))
            {
                System.out.println("Error favorite buttons.");
                hadErrors = true;
            }

            loadThemeSettings(doc);
            filtersFromXML(doc);
        }
        catch (IOException | ParserConfigurationException | SAXException ex)
        {
            hadErrors = true;

            BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
        }

        return hadErrors;
    }

    private void loadThemeSettings(Document doc)
    {
        Element theme = (Element)doc.getElementsByTagName("theme").item(0);
        themes.lastSelectedTheme = theme.getElementsByTagName("lastSelectedTheme").item(0).getTextContent();
        themes.customThemePath = theme.getElementsByTagName("customThemePath").item(0).getTextContent();
        themes.useCustomTheme = Boolean.valueOf(theme.getElementsByTagName("useCustomTheme").item(0).getTextContent());
        themes.darkTheme = Boolean.valueOf(theme.getElementsByTagName("darkTheme").item(0).getTextContent());
        themes.customThemeDarkTheme = Boolean.valueOf(theme.getElementsByTagName("customThemeDarkTheme").item(0).getTextContent());
        themes.lastSelectedFontSize = Integer.parseInt(theme.getElementsByTagName("lastSelectedFontSize").item(0).getTextContent());
        themes.lastSelectedFont = theme.getElementsByTagName("lastSelectedFont").item(0).getTextContent();
    }

    private static boolean extractFileFromJar(final String resourceName, final String fileName)
    {
        final InputStream resource = BTC_ConfigLoader.class.getResourceAsStream(resourceName);
        if (resource != null)
        {
            final File destination = new File(fileName);
            try
            {
                FileUtils.copyInputStreamToFile(resource, destination);
                return true;
            }
            catch (IOException ex)
            {
                BukkitTelnetClient.LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }
}