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

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.*;
import org.apache.commons.lang3.StringUtils;

public class BTC_MainPanel extends javax.swing.JFrame
{
    private final BTC_ConnectionManager connectionManager = new BTC_ConnectionManager();
    private final List<PlayerInfo> playerList = new ArrayList<>();
    private final PlayerListTableModel playerListTableModel = new PlayerListTableModel(playerList);
    private final Collection<FavoriteButtonEntry> favButtonList = BukkitTelnetClient.config.getFavoriteButtons();
    public Themes themes = BukkitTelnetClient.themes;

    public BTC_MainPanel()
    {
        initComponents();
    }

    public static void setIconImage(Window window, String status)
    {
        window.setIconImage(Toolkit.getDefaultToolkit().createImage(window.getClass().getResource("/icon" + status + ".png")));
    }

    public void setLookAndFeel(String stylePath)
    {
        try
        {
            UIManager.setLookAndFeel(stylePath);
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            catch (Exception ex)
            {
                return;
            }
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        }
    }

    public void setFlatLookAndFeel(FlatLaf theme)
    {
        try
        {
            UIManager.setLookAndFeel(theme);
            FlatLaf.updateUI();
            //SwingUtilities.updateComponentTreeUI(this);
            //pack();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            catch (Exception ex)
            {
                return;
            }
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        }
    }

    public void setFlatLookAndFeel(String themeName, boolean dark)
    {
        try
        {
            String type = "light";
            if (dark)
            {
                type = "dark";
            }
            IntelliJTheme.install(Thread.currentThread().getContextClassLoader().getResourceAsStream("themes/" + type + "/" + themeName + ".json"));
            FlatLaf.updateUI();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            try
            {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            catch (Exception ex)
            {
                return;
            }
            SwingUtilities.updateComponentTreeUI(this);
            pack();
        }
    }

    public void setCustomTheme(String path, boolean save)
    {
        FileInputStream stream = null;
        try
        {
            stream = new FileInputStream(path);
        }
        catch (FileNotFoundException e)
        {
            writeError(path + " does not exist");
            return;
        }
        boolean installed = IntelliJTheme.install(stream);
        if (!installed)
        {
            writeError("Failed to load custom theme");
            return;
        }
        FlatLaf.updateUI();
        themes.useCustomTheme = true;
        themes.customThemePath = path;
        themes.darkTheme = themeCustomDarkTheme.isSelected();
        themeTable.clearSelection();
        if (save)
        {
            BukkitTelnetClient.config.save();
        }
    }

    public void toggleComponents(boolean enable)
    {
        List<JComponent> components = Arrays.asList(
                cSayText, sayText, rawsayText, adminChatText, announceText,
                cSaySend, saySend, rawsaySend, adminChatSend, announceSend,
                banNameText, banReasonText, unbanNameText, tempbanNameText, tempbanTimeText, tempbanReasonText,
                banButton, unbanButton, tempbanButton, totalBansButton, purgeBanlistButton, banRollbackToggle,
                adminListNameText, adminListAdd, adminListRemove, adminListInfo, adminListRank, adminListSetRank, adminListView, adminListClean,
                adminWorldGuestNameText, adminWorldGuestAdd, adminWorldGuestRemove, adminWorldGuestList, adminWorldGuestPurge,
                adminWorldTimeSelect, adminWorldGuestRemove, adminWorldTimeSet, adminWorldWeatherSelect, adminWorldWeatherSet

        );
        for (JComponent component : components)
        {
            component.setEnabled(enable);
        }
    }

    public void setup()
    {
        this.txtServer.getEditor().getEditorComponent().addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                if (e.getKeyChar() == KeyEvent.VK_ENTER)
                {
                    BTC_MainPanel.this.saveServersAndTriggerConnect();
                }
            }
        });

        this.loadServerList();

        setIconImage(this, "Disconnected");

        setupTablePopup();

        this.getConnectionManager().updateTitle(false);

        this.tblPlayers.setModel(playerListTableModel);

        this.tblPlayers.getRowSorter().toggleSortOrder(0);

        themes.setupThemeMaps();
        themes.setupThemeTable(themeTable);
        themes.loadSettings(themeTable, themeCustomPath);
        themeCustomDarkTheme.setSelected(themes.customThemeDarkTheme);

        chkIgnorePreprocessCommands.setSelected(BukkitTelnetClient.config.filterIgnorePreprocessCommands);
        chkIgnoreServerCommands.setSelected(BukkitTelnetClient.config.filterIgnoreServerCommands);
        chkShowChatOnly.setSelected(BukkitTelnetClient.config.filterShowChatOnly);
        chkIgnoreWarnings.setSelected(BukkitTelnetClient.config.filterIgnoreWarnings);
        chkIgnoreErrors.setSelected(BukkitTelnetClient.config.filterIgnoreErrors);
        chkShowAdminChatOnly.setSelected(BukkitTelnetClient.config.filterShowAdminChatOnly);
        chkIgnoreAsyncWorldEdit.setSelected(BukkitTelnetClient.config.filterIgnoreAsyncWorldEdit);

        toggleComponents(false);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private final Queue<BTC_TelnetMessage> telnetErrorQueue = new LinkedList<>();
    private boolean isQueueing = false;

    private void flushTelnetErrorQueue()
    {
        BTC_TelnetMessage queuedMessage;
        while ((queuedMessage = telnetErrorQueue.poll()) != null)
        {
            queuedMessage.setColor(Color.GRAY);
            writeToConsoleImmediately(queuedMessage, true);
        }
    }

    public void writeDebug(String message)
    {
        writeToConsole(new BTC_ConsoleMessage(message, Color.PINK));
    }

    public void writeError(String message)
    {
        writeToConsole(new BTC_ConsoleMessage(message, Color.RED));
    }

    public static void setTPS(String tpsValue)
    {
        tps.setText("TPS: " + tpsValue);
    }

    public void writeToConsole(final BTC_ConsoleMessage message)
    {
        if (message.getMessage().isEmpty())
        {
            return;
        }

        if (message instanceof BTC_TelnetMessage)
        {
            final BTC_TelnetMessage telnetMessage = (BTC_TelnetMessage) message;

            if (telnetMessage.isInfoMessage())
            {
                isQueueing = false;
                flushTelnetErrorQueue();
            }

            if (!isQueueing)
            {
                writeToConsoleImmediately(telnetMessage, false);
            }
        }
        else
        {
            isQueueing = false;
            flushTelnetErrorQueue();
            writeToConsoleImmediately(message, false);
        }
    }

    private void writeToConsoleImmediately(final BTC_ConsoleMessage message, final boolean isTelnetError)
    {
        SwingUtilities.invokeLater(() ->
        {
            if (isTelnetError && chkIgnoreErrors.isSelected())
            {
                // Do Nothing
            }
            else
            {
                final StyledDocument styledDocument = mainOutput.getStyledDocument();

                int startLength = styledDocument.getLength();

                try
                {
                    styledDocument.insertString(
                            styledDocument.getLength(),
                            message.getMessage() + System.lineSeparator(),
                            StyleContext.getDefaultStyleContext().addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, message.getColor())
                    );
                }
                catch (BadLocationException ex)
                {
                    throw new RuntimeException(ex);
                }

                if (BTC_MainPanel.this.chkAutoScroll.isSelected() && BTC_MainPanel.this.mainOutput.getSelectedText() == null)
                {
                    final JScrollBar vScroll = mainOutputScoll.getVerticalScrollBar();

                    if (!vScroll.getValueIsAdjusting())
                    {
                        if (vScroll.getValue() + vScroll.getModel().getExtent() >= (vScroll.getMaximum() - 50))
                        {
                            BTC_MainPanel.this.mainOutput.setCaretPosition(startLength);

                            final Timer timer = new Timer(10, event -> vScroll.setValue(vScroll.getMaximum()));
                            timer.setRepeats(false);
                            timer.start();
                        }
                    }
                }
            }
        });
    }

    public final PlayerInfo getSelectedPlayer()
    {
        final JTable table = BTC_MainPanel.this.tblPlayers;

        final int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= playerList.size())
        {
            return null;
        }

        return playerList.get(table.convertRowIndexToModel(selectedRow));
    }

    public static class PlayerListTableModel extends AbstractTableModel
    {
        private final List<PlayerInfo> _playerList;

        public PlayerListTableModel(List<PlayerInfo> playerList)
        {
            this._playerList = playerList;
        }

        @Override
        public int getRowCount()
        {
            return _playerList.size();
        }

        @Override
        public int getColumnCount()
        {
            return PlayerInfo.numColumns;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (rowIndex >= _playerList.size())
            {
                return null;
            }

            return _playerList.get(rowIndex).getColumnValue(columnIndex);
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return columnIndex < getColumnCount() ? PlayerInfo.columnNames[columnIndex] : "null";
        }

        public List<PlayerInfo> getPlayerList()
        {
            return _playerList;
        }
    }

    public final void updatePlayerList(final String selectedPlayerName)
    {
        EventQueue.invokeLater(() ->
        {
            playerListTableModel.fireTableDataChanged();

            BTC_MainPanel.this.txtNumPlayers.setText("" + playerList.size());

            if (selectedPlayerName != null)
            {
                final JTable table = BTC_MainPanel.this.tblPlayers;
                final ListSelectionModel selectionModel = table.getSelectionModel();

                for (PlayerInfo player : playerList)
                {
                    if (player.getName().equals(selectedPlayerName))
                    {
                        selectionModel.setSelectionInterval(0, table.convertRowIndexToView(playerList.indexOf(player)));
                    }
                }
            }
        });
    }

    public static class PlayerListPopupItem extends JMenuItem
    {
        private final PlayerInfo player;

        public PlayerListPopupItem(String text, PlayerInfo player)
        {
            super(text);
            this.player = player;
        }

        public PlayerInfo getPlayer()
        {
            return player;
        }
    }

    public static class PlayerListPopupItem_Command extends PlayerListPopupItem
    {
        private final PlayerCommandEntry command;

        public PlayerListPopupItem_Command(String text, PlayerInfo player, PlayerCommandEntry command)
        {
            super(text, player);
            this.command = command;
        }

        public PlayerCommandEntry getCommand()
        {
            return command;
        }
    }

    public final void setupTablePopup()
    {
        this.tblPlayers.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(final MouseEvent mouseEvent)
            {
                final JTable table = BTC_MainPanel.this.tblPlayers;

                final int r = table.rowAtPoint(mouseEvent.getPoint());
                if (r >= 0 && r < table.getRowCount())
                {
                    table.setRowSelectionInterval(r, r);
                }
                else
                {
                    table.clearSelection();
                }

                final int rowindex = table.getSelectedRow();
                if (rowindex < 0)
                {
                    return;
                }

                if ((SwingUtilities.isRightMouseButton(mouseEvent) || mouseEvent.isControlDown()) && mouseEvent.getComponent() instanceof JTable)
                {
                    final PlayerInfo player = getSelectedPlayer();
                    if (player != null)
                    {
                        final JPopupMenu popup = new JPopupMenu(player.getName());

                        final JMenuItem header = new JMenuItem("Apply action to " + player.getName() + ":");
                        header.setEnabled(false);
                        popup.add(header);

                        popup.addSeparator();

                        final ActionListener popupAction = actionEvent ->
                        {
                            Object _source = actionEvent.getSource();
                            if (_source instanceof PlayerListPopupItem_Command)
                            {
                                final PlayerListPopupItem_Command source = (PlayerListPopupItem_Command) _source;
                                final String output = source.getCommand().buildOutput(source.getPlayer(), true);
                                BTC_MainPanel.this.getConnectionManager().sendDelayedCommand(output, true, 100);
                            }
                            else if (_source instanceof PlayerListPopupItem)
                            {
                                final PlayerListPopupItem source = (PlayerListPopupItem) _source;

                                final PlayerInfo _player = source.getPlayer();

                                switch (actionEvent.getActionCommand())
                                {
                                    case "Copy IP":
                                    {
                                        copyToClipboard(_player.getIp());
                                        BTC_MainPanel.this.writeToConsole(new BTC_ConsoleMessage("Copied IP to clipboard: " + _player.getIp()));
                                        break;
                                    }
                                    case "Copy Name":
                                    {
                                        copyToClipboard(_player.getName());
                                        BTC_MainPanel.this.writeToConsole(new BTC_ConsoleMessage("Copied name to clipboard: " + _player.getName()));
                                        break;
                                    }
                                    case "Copy UUID":
                                    {
                                        copyToClipboard(_player.getUuid());
                                        BTC_MainPanel.this.writeToConsole(new BTC_ConsoleMessage("Copied UUID to clipboard: " + _player.getUuid()));
                                        break;
                                    }
                                }
                            }
                        };

                        for (final PlayerCommandEntry command : BukkitTelnetClient.config.getCommands())
                        {
                            final PlayerListPopupItem_Command item = new PlayerListPopupItem_Command(command.getName(), player, command);
                            item.addActionListener(popupAction);
                            popup.add(item);
                        }

                        popup.addSeparator();

                        JMenuItem item;

                        item = new PlayerListPopupItem("Copy Name", player);
                        item.addActionListener(popupAction);
                        popup.add(item);

                        item = new PlayerListPopupItem("Copy IP", player);
                        item.addActionListener(popupAction);
                        popup.add(item);

                        item = new PlayerListPopupItem("Copy UUID", player);
                        item.addActionListener(popupAction);
                        popup.add(item);

                        popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
                    }
                }
            }
        });
    }

    public void copyToClipboard(final String myString)
    {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(myString), null);
    }

    public final void loadServerList()
    {
        txtServer.removeAllItems();
        for (final ServerEntry serverEntry : BukkitTelnetClient.config.getServers())
        {
            txtServer.addItem(serverEntry);
            if (serverEntry.isLastUsed())
            {
                txtServer.setSelectedItem(serverEntry);
            }
        }
    }

    public final void saveServersAndTriggerConnect()
    {
        final Object selectedItem = txtServer.getSelectedItem();
        if (selectedItem == null)
        {
            return;
        }

        ServerEntry entry;
        if (selectedItem instanceof ServerEntry)
        {
            entry = (ServerEntry) selectedItem;
        }
        else
        {
            final String serverAddress = StringUtils.trimToNull(selectedItem.toString());
            if (serverAddress == null)
            {
                return;
            }

            String serverName = JOptionPane.showInputDialog(this, "Enter server name:", "Server Name", JOptionPane.PLAIN_MESSAGE);
            if (serverName == null)
            {
                return;
            }

            serverName = StringUtils.trimToEmpty(serverName);
            if (serverName.isEmpty())
            {
                serverName = "Unnamed";
            }

            entry = new ServerEntry(serverName, serverAddress);

            BukkitTelnetClient.config.getServers().add(entry);
        }

        for (final ServerEntry existingEntry : BukkitTelnetClient.config.getServers())
        {
            if (entry.equals(existingEntry))
            {
                entry = existingEntry;
            }
            existingEntry.setLastUsed(false);
        }

        entry.setLastUsed(true);

        BukkitTelnetClient.config.save();

        loadServerList();

        getConnectionManager().triggerConnect(entry.getAddress());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollBar1 = new javax.swing.JScrollBar();
        splitPane = new javax.swing.JSplitPane();
        main = new javax.swing.JPanel();
        mainOutputScoll = new javax.swing.JScrollPane();
        mainOutput = new javax.swing.JTextPane();
        btnDisconnect = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        txtServer = new javax.swing.JComboBox<>();
        chkAutoScroll = new javax.swing.JCheckBox();
        txtCommand = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        clearLogs = new javax.swing.JButton();
        tps = new javax.swing.JLabel();
        sidebarPane = new javax.swing.JTabbedPane();
        playerListPanel = new javax.swing.JPanel();
        tblPlayersScroll = new javax.swing.JScrollPane();
        tblPlayers = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        txtNumPlayers = new javax.swing.JTextField();
        filterPanel = new javax.swing.JPanel();
        chkIgnorePreprocessCommands = new javax.swing.JCheckBox();
        chkShowAdminChatOnly = new javax.swing.JCheckBox();
        chkShowChatOnly = new javax.swing.JCheckBox();
        chkIgnoreErrors = new javax.swing.JCheckBox();
        chkIgnoreServerCommands = new javax.swing.JCheckBox();
        chkIgnoreWarnings = new javax.swing.JCheckBox();
        chkIgnoreAsyncWorldEdit = new javax.swing.JCheckBox();
        commandsPanel = new javax.swing.JPanel();
        favoriteButtonsPanelHolder = new javax.swing.JPanel();
        favoriteButtonsPanelScroll = new javax.swing.JScrollPane();
        favoriteButtonsPanel = new BTC_FavoriteButtonsPanel(favButtonList);
        chatPanel = new javax.swing.JPanel();
        sayLabel = new javax.swing.JLabel();
        sayText = new javax.swing.JTextField();
        saySend = new javax.swing.JButton();
        cSayLabel = new javax.swing.JLabel();
        cSayText = new javax.swing.JTextField();
        cSaySend = new javax.swing.JButton();
        rawsayLabel = new javax.swing.JLabel();
        rawsayText = new javax.swing.JTextField();
        rawsaySend = new javax.swing.JButton();
        adminChatLabel = new javax.swing.JLabel();
        adminChatText = new javax.swing.JTextField();
        adminChatSend = new javax.swing.JButton();
        announceLabel = new javax.swing.JLabel();
        announceText = new javax.swing.JTextField();
        announceSend = new javax.swing.JButton();
        banListPanel = new javax.swing.JPanel();
        banLabel = new javax.swing.JLabel();
        banNameText = new javax.swing.JTextField();
        banReasonText = new javax.swing.JTextField();
        banButton = new javax.swing.JButton();
        banNameLabel = new javax.swing.JLabel();
        banReasonLabel = new javax.swing.JLabel();
        banRollbackToggle = new javax.swing.JCheckBox();
        banSeparator = new javax.swing.JSeparator();
        unbanLabel = new javax.swing.JLabel();
        unbanNameText = new javax.swing.JTextField();
        unbanButton = new javax.swing.JButton();
        unbanNameLabel = new javax.swing.JLabel();
        unbanSeparator = new javax.swing.JSeparator();
        tempbanLabel = new javax.swing.JLabel();
        tempbanNameText = new javax.swing.JTextField();
        tempbanTimeText = new javax.swing.JTextField();
        tempbanReasonText = new javax.swing.JTextField();
        tempbanButton = new javax.swing.JButton();
        tempbanNameLabel = new javax.swing.JLabel();
        tempbanTimeLabel = new javax.swing.JLabel();
        tempbanReasonLabel = new javax.swing.JLabel();
        tempbanSeparator = new javax.swing.JSeparator();
        totalBansButton = new javax.swing.JButton();
        purgeBanlistButton = new javax.swing.JButton();
        adminListPanel = new javax.swing.JPanel();
        adminListNameText = new javax.swing.JTextField();
        adminListNameLabel = new javax.swing.JLabel();
        adminListAdd = new javax.swing.JButton();
        adminListRemove = new javax.swing.JButton();
        adminListInfo = new javax.swing.JButton();
        adminListRank = new javax.swing.JComboBox<>();
        adminListSetRank = new javax.swing.JButton();
        adminListSeparator = new javax.swing.JSeparator();
        adminListView = new javax.swing.JButton();
        adminListClean = new javax.swing.JButton();
        adminWorldPanel = new javax.swing.JPanel();
        adminWorldGuestNameText = new javax.swing.JTextField();
        adminWorldGuestNameLabel = new javax.swing.JLabel();
        adminWorldGuestAdd = new javax.swing.JButton();
        adminWorldGuestRemove = new javax.swing.JButton();
        adminWorldGuestSeparator = new javax.swing.JSeparator();
        adminWorldGuestList = new javax.swing.JButton();
        adminWorldGuestPurge = new javax.swing.JButton();
        adminWorldTimeSelect = new javax.swing.JComboBox<>();
        adminWorldTimeSet = new javax.swing.JButton();
        adminWorldWeatherSelect = new javax.swing.JComboBox<>();
        adminWorldWeatherSet = new javax.swing.JButton();
        themePanel = new javax.swing.JPanel();
        themeScrollPane = new javax.swing.JScrollPane();
        themeTable = new javax.swing.JTable();
        themeCustomPath = new javax.swing.JTextField();
        themeFileSelect = new javax.swing.JButton();
        themeApplyCustom = new javax.swing.JButton();
        themeCustomDarkTheme = new javax.swing.JCheckBox();
        fontPanel = new javax.swing.JPanel();
        fontSizeLabel = new javax.swing.JLabel();
        fontSizeSelect = new javax.swing.JTextField();
        fontSizeSet = new javax.swing.JButton();
        fontLabel = new javax.swing.JLabel();
        fontSelect = new javax.swing.JTextField();
        fontSet = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BukkitTelnetClient");
        setPreferredSize(new java.awt.Dimension(1231, 663));

        splitPane.setResizeWeight(1.0);
        splitPane.setMinimumSize(new java.awt.Dimension(75, 62));
        splitPane.setPreferredSize(new java.awt.Dimension(1027, 452));

        mainOutput.setEditable(false);
        mainOutput.setBorder(null);
        mainOutputScoll.setViewportView(mainOutput);
        mainOutput.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N

        btnDisconnect.setText("Disconnect");
        btnDisconnect.setEnabled(false);
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });

        btnSend.setText("Send");
        btnSend.setEnabled(false);
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        txtServer.setEditable(true);

        chkAutoScroll.setSelected(true);
        chkAutoScroll.setText("AutoScroll");

        txtCommand.setEnabled(false);
        txtCommand.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCommandKeyPressed(evt);
            }
        });

        btnConnect.setText("Connect");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        jLabel1.setText("Command:");

        jLabel2.setText("Server:");

        clearLogs.setText("Clear Logs");
        clearLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogsActionPerformed(evt);
            }
        });

        tps.setText("TPS: N/A");

        javax.swing.GroupLayout mainLayout = new javax.swing.GroupLayout(main);
        main.setLayout(mainLayout);
        mainLayout.setHorizontalGroup(
                mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(mainOutputScoll)
                                        .addGroup(mainLayout.createSequentialGroup()
                                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel1))
                                                .addGap(18, 18, 18)
                                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(txtCommand)
                                                        .addComponent(txtServer, 0, 593, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(btnConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(btnDisconnect)
                                                        .addComponent(chkAutoScroll)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainLayout.createSequentialGroup()
                                                .addComponent(tps)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(clearLogs)))
                                .addContainerGap())
        );

        mainLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[]{btnConnect, btnDisconnect, btnSend, chkAutoScroll});

        mainLayout.setVerticalGroup(
                mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(clearLogs)
                                        .addComponent(tps))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mainOutputScoll, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1)
                                        .addComponent(btnSend)
                                        .addComponent(chkAutoScroll))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(btnConnect)
                                        .addComponent(btnDisconnect)
                                        .addComponent(txtServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        splitPane.setLeftComponent(main);

        sidebarPane.setMinimumSize(new java.awt.Dimension(360, 450));
        sidebarPane.setPreferredSize(new java.awt.Dimension(360, 450));

        tblPlayers.setAutoCreateRowSorter(true);
        tblPlayers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblPlayersScroll.setViewportView(tblPlayers);
        tblPlayers.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jLabel3.setText("# Players:");

        txtNumPlayers.setEditable(false);

        javax.swing.GroupLayout playerListPanelLayout = new javax.swing.GroupLayout(playerListPanel);
        playerListPanel.setLayout(playerListPanelLayout);
        playerListPanelLayout.setHorizontalGroup(
                playerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(playerListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(playerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(tblPlayersScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                        .addGroup(playerListPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(txtNumPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        playerListPanelLayout.setVerticalGroup(
                playerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(playerListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tblPlayersScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 564, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(playerListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(txtNumPlayers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        sidebarPane.addTab("Player List", playerListPanel);

        chkIgnorePreprocessCommands.setText("Ignore \"[PREPROCESS_COMMAND]\" messages");
        chkIgnorePreprocessCommands.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnorePreprocessCommandsActionPerformed(evt);
            }
        });

        chkShowAdminChatOnly.setText("Show admin chat only");
        chkShowAdminChatOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowAdminChatOnlyActionPerformed(evt);
            }
        });

        chkShowChatOnly.setText("Show chat only");
        chkShowChatOnly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowChatOnlyActionPerformed(evt);
            }
        });

        chkIgnoreErrors.setText("Ignore errors");
        chkIgnoreErrors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnoreErrorsActionPerformed(evt);
            }
        });

        chkIgnoreServerCommands.setText("Ignore \"issued server command\" messages");
        chkIgnoreServerCommands.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnoreServerCommandsActionPerformed(evt);
            }
        });

        chkIgnoreWarnings.setText("Ignore warnings");
        chkIgnoreWarnings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnoreWarningsActionPerformed(evt);
            }
        });

        chkIgnoreAsyncWorldEdit.setText("Ignore AsyncWorldEdit");
        chkIgnoreAsyncWorldEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkIgnoreAsyncWorldEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
                filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(chkIgnorePreprocessCommands)
                                        .addComponent(chkShowAdminChatOnly)
                                        .addComponent(chkShowChatOnly)
                                        .addComponent(chkIgnoreErrors)
                                        .addComponent(chkIgnoreServerCommands)
                                        .addComponent(chkIgnoreWarnings)
                                        .addComponent(chkIgnoreAsyncWorldEdit))
                                .addContainerGap(100, Short.MAX_VALUE))
        );
        filterPanelLayout.setVerticalGroup(
                filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(filterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(chkIgnorePreprocessCommands, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkIgnoreServerCommands, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkShowChatOnly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkIgnoreWarnings, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkIgnoreErrors, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkShowAdminChatOnly, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkIgnoreAsyncWorldEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(449, Short.MAX_VALUE))
        );

        sidebarPane.addTab("Filters", filterPanel);

        favoriteButtonsPanelHolder.setLayout(new java.awt.BorderLayout());

        favoriteButtonsPanelScroll.setBorder(null);

        favoriteButtonsPanel.setLayout(null);
        favoriteButtonsPanelScroll.setViewportView(favoriteButtonsPanel);

        favoriteButtonsPanelHolder.add(favoriteButtonsPanelScroll, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout commandsPanelLayout = new javax.swing.GroupLayout(commandsPanel);
        commandsPanel.setLayout(commandsPanelLayout);
        commandsPanelLayout.setHorizontalGroup(
                commandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(commandsPanelLayout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(favoriteButtonsPanelHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0))
        );
        commandsPanelLayout.setVerticalGroup(
                commandsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(commandsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(favoriteButtonsPanelHolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        sidebarPane.addTab("Commands", commandsPanel);

        chatPanel.setAlignmentY(0.05F);

        sayLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        sayLabel.setText("Say:");
        sayLabel.setAlignmentY(0.0F);

        sayText.setName(""); // NOI18N

        saySend.setText("Send");
        saySend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saySendActionPerformed(evt);
            }
        });

        cSayLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cSayLabel.setText("Csay");
        cSayLabel.setAlignmentY(0.0F);

        cSaySend.setText("Send");
        cSaySend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cSaySendActionPerformed(evt);
            }
        });

        rawsayLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        rawsayLabel.setText("Rawsay");
        rawsayLabel.setAlignmentY(0.0F);

        rawsayText.setToolTipText("");

        rawsaySend.setText("Send");
        rawsaySend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rawsaySendActionPerformed(evt);
            }
        });

        adminChatLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        adminChatLabel.setText("Admin Chat");
        adminChatLabel.setAlignmentY(0.0F);

        adminChatSend.setText("Send");
        adminChatSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminChatSendActionPerformed(evt);
            }
        });

        announceLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        announceLabel.setText("Announce");
        announceLabel.setAlignmentY(0.0F);

        announceSend.setText("Send");
        announceSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                announceSendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout chatPanelLayout = new javax.swing.GroupLayout(chatPanel);
        chatPanel.setLayout(chatPanelLayout);
        chatPanelLayout.setHorizontalGroup(
                chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(chatPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(rawsayLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cSayLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(adminChatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                                        .addComponent(announceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(sayLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addGroup(chatPanelLayout.createSequentialGroup()
                                                .addComponent(announceText)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(announceSend))
                                        .addGroup(chatPanelLayout.createSequentialGroup()
                                                .addComponent(sayText)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(saySend))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, chatPanelLayout.createSequentialGroup()
                                                .addComponent(adminChatText)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(adminChatSend))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, chatPanelLayout.createSequentialGroup()
                                                .addComponent(rawsayText)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(rawsaySend))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, chatPanelLayout.createSequentialGroup()
                                                .addComponent(cSayText)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cSaySend)))
                                .addContainerGap())
        );
        chatPanelLayout.setVerticalGroup(
                chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(chatPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(sayText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sayLabel)
                                        .addComponent(saySend))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(cSayText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(cSayLabel)
                                        .addComponent(cSaySend))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(rawsayText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(rawsayLabel)
                                        .addComponent(rawsaySend))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminChatText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(adminChatLabel)
                                        .addComponent(adminChatSend))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(chatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(announceText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(announceSend))
                                        .addComponent(announceLabel))
                                .addContainerGap(467, Short.MAX_VALUE))
        );

        sidebarPane.addTab("Chat", chatPanel);

        banLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        banLabel.setText("Ban Player");

        banButton.setText("Ban");
        banButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                banButtonActionPerformed(evt);
            }
        });

        banNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        banNameLabel.setText("Name");

        banReasonLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        banReasonLabel.setText("Reason");

        banRollbackToggle.setSelected(true);
        banRollbackToggle.setText("RB");
        banRollbackToggle.setToolTipText("Rollback player");

        unbanLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        unbanLabel.setText("Unban Player");

        unbanButton.setText("Unban");
        unbanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unbanButtonActionPerformed(evt);
            }
        });

        unbanNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        unbanNameLabel.setText("Name");

        tempbanLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tempbanLabel.setText("Temp Ban Player");

        tempbanTimeText.setToolTipText("Example: 5m, 1h, 20y");

        tempbanButton.setText("Ban");
        tempbanButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tempbanButtonActionPerformed(evt);
            }
        });

        tempbanNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tempbanNameLabel.setText("Name");

        tempbanTimeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tempbanTimeLabel.setText("Time");
        tempbanTimeLabel.setToolTipText("");

        tempbanReasonLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tempbanReasonLabel.setText("Reason");

        totalBansButton.setText("Total Bans");
        totalBansButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalBansButtonActionPerformed(evt);
            }
        });

        purgeBanlistButton.setText("Purge Ban List");
        purgeBanlistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                purgeBanlistButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout banListPanelLayout = new javax.swing.GroupLayout(banListPanel);
        banListPanel.setLayout(banListPanelLayout);
        banListPanelLayout.setHorizontalGroup(
                banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(banListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(banSeparator)
                                        .addComponent(banLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(banListPanelLayout.createSequentialGroup()
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(banNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                                                        .addComponent(banNameText))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(banListPanelLayout.createSequentialGroup()
                                                                .addComponent(banReasonText)
                                                                .addGap(2, 2, 2))
                                                        .addComponent(banReasonLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(banRollbackToggle, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(banButton, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(unbanSeparator)
                                        .addComponent(unbanLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(banListPanelLayout.createSequentialGroup()
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(unbanNameText)
                                                        .addComponent(unbanNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(unbanButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(tempbanSeparator)
                                        .addComponent(tempbanLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(banListPanelLayout.createSequentialGroup()
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(tempbanNameText)
                                                        .addComponent(tempbanNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(tempbanTimeText)
                                                        .addComponent(tempbanTimeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(tempbanReasonText)
                                                        .addComponent(tempbanReasonLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tempbanButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(banListPanelLayout.createSequentialGroup()
                                                .addComponent(totalBansButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(purgeBanlistButton)))
                                .addContainerGap())
        );
        banListPanelLayout.setVerticalGroup(
                banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(banListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(banLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(banNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(banReasonText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(banButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(banNameLabel)
                                        .addComponent(banReasonLabel)
                                        .addComponent(banRollbackToggle))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(banSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unbanLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(unbanNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(unbanButton))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(unbanNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(unbanSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tempbanLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tempbanNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tempbanButton)
                                        .addComponent(tempbanTimeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tempbanReasonText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tempbanNameLabel)
                                        .addComponent(tempbanTimeLabel)
                                        .addComponent(tempbanReasonLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(tempbanSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(banListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(totalBansButton)
                                        .addComponent(purgeBanlistButton))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sidebarPane.addTab("Ban List", banListPanel);

        adminListNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        adminListNameLabel.setText("Name");

        adminListAdd.setText("Add");
        adminListAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminListAddActionPerformed(evt);
            }
        });

        adminListRemove.setText("Remove");
        adminListRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminListRemoveActionPerformed(evt);
            }
        });

        adminListInfo.setText("Info");
        adminListInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminListInfoActionPerformed(evt);
            }
        });

        adminListRank.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Super Admin", "Telnet Admin", "Senior Admin"}));

        adminListSetRank.setText("Set Rank");
        adminListSetRank.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminListSetRankActionPerformed(evt);
            }
        });

        adminListView.setText("List");
        adminListView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminListViewActionPerformed(evt);
            }
        });

        adminListClean.setText("Clean");
        adminListClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminListCleanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout adminListPanelLayout = new javax.swing.GroupLayout(adminListPanel);
        adminListPanel.setLayout(adminListPanelLayout);
        adminListPanelLayout.setHorizontalGroup(
                adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(adminListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(adminListPanelLayout.createSequentialGroup()
                                                .addGroup(adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(adminListNameText)
                                                        .addComponent(adminListNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(adminListSeparator))
                                                .addContainerGap())
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, adminListPanelLayout.createSequentialGroup()
                                                .addGap(75, 75, 75)
                                                .addComponent(adminListView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(adminListClean, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGap(74, 74, 74))
                                        .addGroup(adminListPanelLayout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                                                .addGroup(adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(adminListPanelLayout.createSequentialGroup()
                                                                .addComponent(adminListRank, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(adminListSetRank, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(adminListPanelLayout.createSequentialGroup()
                                                                .addComponent(adminListAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(adminListRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(adminListInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addContainerGap(47, Short.MAX_VALUE))))
        );
        adminListPanelLayout.setVerticalGroup(
                adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(adminListPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(adminListNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(adminListNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(adminListAdd)
                                        .addComponent(adminListRemove)
                                        .addComponent(adminListInfo))
                                .addGap(18, 18, 18)
                                .addGroup(adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminListRank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(adminListSetRank))
                                .addGap(18, 18, 18)
                                .addComponent(adminListSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminListClean)
                                        .addComponent(adminListView))
                                .addGap(393, 393, 393))
        );

        sidebarPane.addTab("Admin List", adminListPanel);

        adminWorldGuestNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        adminWorldGuestNameLabel.setText("Guest Name");

        adminWorldGuestAdd.setText("Add");
        adminWorldGuestAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminWorldGuestAddActionPerformed(evt);
            }
        });

        adminWorldGuestRemove.setText("Remove");
        adminWorldGuestRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminWorldGuestRemoveActionPerformed(evt);
            }
        });

        adminWorldGuestList.setText("Guest List");
        adminWorldGuestList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminWorldGuestListActionPerformed(evt);
            }
        });

        adminWorldGuestPurge.setText("Guest Purge");
        adminWorldGuestPurge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminWorldGuestPurgeActionPerformed(evt);
            }
        });

        adminWorldTimeSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Morning", "Noon", "Evening", "Night"}));

        adminWorldTimeSet.setText("Set Time");
        adminWorldTimeSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminWorldTimeSetActionPerformed(evt);
            }
        });

        adminWorldWeatherSelect.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Off", "Rain", "Storm"}));

        adminWorldWeatherSet.setText("Set Weather");
        adminWorldWeatherSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminWorldWeatherSetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout adminWorldPanelLayout = new javax.swing.GroupLayout(adminWorldPanel);
        adminWorldPanel.setLayout(adminWorldPanelLayout);
        adminWorldPanelLayout.setHorizontalGroup(
                adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(adminWorldPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(adminWorldGuestSeparator)
                                        .addComponent(adminWorldGuestNameText)
                                        .addComponent(adminWorldGuestNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(adminWorldPanelLayout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(adminWorldPanelLayout.createSequentialGroup()
                                                                .addComponent(adminWorldGuestAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(adminWorldGuestRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(adminWorldPanelLayout.createSequentialGroup()
                                                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(adminWorldTimeSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(adminWorldWeatherSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(adminWorldGuestList, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(adminWorldTimeSet, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(adminWorldWeatherSet, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(adminWorldGuestPurge, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        adminWorldPanelLayout.setVerticalGroup(
                adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(adminWorldPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(adminWorldGuestNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(adminWorldGuestNameLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminWorldGuestAdd)
                                        .addComponent(adminWorldGuestRemove))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(adminWorldGuestSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminWorldGuestList)
                                        .addComponent(adminWorldGuestPurge))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminWorldTimeSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(adminWorldTimeSet))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(adminWorldPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(adminWorldWeatherSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(adminWorldWeatherSet))
                                .addContainerGap(429, Short.MAX_VALUE))
        );

        sidebarPane.addTab("Admin World", adminWorldPanel);

        themeTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null}
                },
                new String[]{
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }
        ));
        themeTable.setRowSelectionAllowed(false);
        themeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        themeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                themeTableMouseReleased(evt);
            }
        });
        themeTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                themeTableKeyReleased(evt);
            }
        });
        themeScrollPane.setViewportView(themeTable);

        themeFileSelect.setText("File Select");
        themeFileSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                themeFileSelectActionPerformed(evt);
            }
        });

        themeApplyCustom.setText("Apply");
        themeApplyCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                themeApplyCustomActionPerformed(evt);
            }
        });

        themeCustomDarkTheme.setText("Dark");
        themeCustomDarkTheme.setToolTipText("Turn this on if your custom theme is a dark theme.");
        themeCustomDarkTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                themeCustomDarkThemeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout themePanelLayout = new javax.swing.GroupLayout(themePanel);
        themePanel.setLayout(themePanelLayout);
        themePanelLayout.setHorizontalGroup(
                themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(themePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(themeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, themePanelLayout.createSequentialGroup()
                                                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(themeFileSelect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(themeCustomPath))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(themeApplyCustom, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                                                        .addComponent(themeCustomDarkTheme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(8, 8, 8)))
                                .addContainerGap())
        );
        themePanelLayout.setVerticalGroup(
                themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(themePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(themeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(themeCustomPath)
                                        .addComponent(themeApplyCustom))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(themePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(themeFileSelect)
                                        .addComponent(themeCustomDarkTheme))
                                .addContainerGap())
        );

        sidebarPane.addTab("Theme", themePanel);

        fontSizeSet.setText("Apply");
        fontSizeSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeSetActionPerformed(evt);
            }
        });

        fontSet.setText("Apply");
        fontSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSetActionPerformed(evt);
            }
        });

        fontSizeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fontSizeLabel.setText("Font Size");

        fontLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fontLabel.setText("Font");

        fontSelect.setText("Courier New");
        fontSizeSelect.setText("12");

        javax.swing.GroupLayout fontPanelLayout = new javax.swing.GroupLayout(fontPanel);
        fontPanel.setLayout(fontPanelLayout);
        fontPanelLayout.setHorizontalGroup(
                fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(fontPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(fontLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(fontSizeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addGroup(fontPanelLayout.createSequentialGroup()
                                                .addComponent(fontSelect)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fontSet))
                                        .addGroup(fontPanelLayout.createSequentialGroup()
                                                .addComponent(fontSizeSelect)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fontSizeSet)))
                                .addContainerGap())
        );
        fontPanelLayout.setVerticalGroup(
                fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(fontPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fontLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fontSelect)
                                        .addComponent(fontSet))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(fontPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(fontSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fontSizeSelect)
                                        .addComponent(fontSizeSet))
                                .addContainerGap(467, Short.MAX_VALUE))
        );

        sidebarPane.addTab("Font", fontPanel);

        splitPane.setRightComponent(sidebarPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1231, Short.MAX_VALUE)
                                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE)
                                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtCommandKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_txtCommandKeyPressed
    {//GEN-HEADEREND:event_txtCommandKeyPressed
        if (!txtCommand.isEnabled())
        {
            return;
        }
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            getConnectionManager().sendCommand(txtCommand.getText());
            txtCommand.selectAll();
        }
    }//GEN-LAST:event_txtCommandKeyPressed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnConnectActionPerformed
    {//GEN-HEADEREND:event_btnConnectActionPerformed
        if (!btnConnect.isEnabled())
        {
            return;
        }
        saveServersAndTriggerConnect();
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnDisconnectActionPerformed
    {//GEN-HEADEREND:event_btnDisconnectActionPerformed
        if (!btnDisconnect.isEnabled())
        {
            return;
        }
        getConnectionManager().triggerDisconnect();
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnSendActionPerformed
    {//GEN-HEADEREND:event_btnSendActionPerformed
        if (!btnSend.isEnabled())
        {
            return;
        }
        getConnectionManager().sendCommand(txtCommand.getText());
        txtCommand.selectAll();
    }//GEN-LAST:event_btnSendActionPerformed

    private void clearLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogsActionPerformed
        mainOutput.setText("");
    }//GEN-LAST:event_clearLogsActionPerformed

    private void adminListCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminListCleanActionPerformed
        getConnectionManager().sendCommand("saconfig clean");
    }//GEN-LAST:event_adminListCleanActionPerformed

    private void adminListViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminListViewActionPerformed
        getConnectionManager().sendCommand("saconfig list");
    }//GEN-LAST:event_adminListViewActionPerformed

    private void adminListSetRankActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminListSetRankActionPerformed
        String name = adminListNameText.getText();
        if (name.isEmpty())
        {
            return;
        }

        String rank = adminListRank.getSelectedItem().toString().toLowerCase().replace(" ", "_");

        getConnectionManager().sendCommand("saconfig setrank " + rank + " " + name);
    }//GEN-LAST:event_adminListSetRankActionPerformed

    private void adminListInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminListInfoActionPerformed
        String name = adminListNameText.getText();
        if (name.isEmpty())
        {
            return;
        }
        getConnectionManager().sendCommand("saconfig info " + name);
    }//GEN-LAST:event_adminListInfoActionPerformed

    private void adminListRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminListRemoveActionPerformed
        String name = adminListNameText.getText();
        if (name.isEmpty())
        {
            return;
        }
        getConnectionManager().sendCommand("saconfig remove " + name);
    }//GEN-LAST:event_adminListRemoveActionPerformed

    private void adminListAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminListAddActionPerformed
        String name = adminListNameText.getText();
        if (name.isEmpty())
        {
            return;
        }
        getConnectionManager().sendCommand("saconfig add " + name);
    }//GEN-LAST:event_adminListAddActionPerformed

    private void themeCustomDarkThemeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_themeCustomDarkThemeActionPerformed
        if (themes.useCustomTheme)
        {
            themes.darkTheme = themeCustomDarkTheme.isSelected();
            themes.customThemeDarkTheme = themeCustomDarkTheme.isSelected();
            BukkitTelnetClient.config.save();
        }
    }//GEN-LAST:event_themeCustomDarkThemeActionPerformed

    private void themeApplyCustomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_themeApplyCustomActionPerformed
        themes.applyCustomTheme(true);
    }//GEN-LAST:event_themeApplyCustomActionPerformed

    private void themeFileSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_themeFileSelectActionPerformed
        String path = themes.selectFile();
        if (path != null)
        {
            themeCustomPath.setText(path);
        }
    }//GEN-LAST:event_themeFileSelectActionPerformed

    private void themeTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_themeTableKeyReleased
        int[] keys = {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_TAB};
        List<Integer> keyCodes = Arrays.stream(keys).boxed().collect(Collectors.toList()); // literally had to google this bc u cant fuckin List<int> for some reason
        if (keyCodes.contains(evt.getKeyCode()))
        {
            themes.selectTheme(themeTable, true);
        }
    }//GEN-LAST:event_themeTableKeyReleased

    private void themeTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_themeTableMouseReleased
        themes.selectTheme(themeTable, true);
    }//GEN-LAST:event_themeTableMouseReleased

    private void fontSizeSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeSetActionPerformed
        this.fontSizeString = fontSizeSelect.getText();
        this.fontSize = Integer.parseInt(fontSizeString);
        mainOutput.setFont(new java.awt.Font(mainOutput.getFont().getFontName(), 0, fontSize)); // NOI18N
    }//GEN-LAST:event_fontSizeSetActionPerformed

    private void fontSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSetActionPerformed
        this.font = fontSelect.getText();
        mainOutput.setFont(new java.awt.Font(font, 0, mainOutput.getFont().getSize())); // NOI18N
    }//GEN-LAST:event_fontSetActionPerformed


    private void adminWorldWeatherSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminWorldWeatherSetActionPerformed
        String weather = adminWorldWeatherSelect.getSelectedItem().toString().toLowerCase();

        getConnectionManager().sendCommand("adminworld weather " + weather);
    }//GEN-LAST:event_adminWorldWeatherSetActionPerformed

    private void adminWorldTimeSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminWorldTimeSetActionPerformed
        String time = adminWorldTimeSelect.getSelectedItem().toString().toLowerCase();

        getConnectionManager().sendCommand("adminworld time " + time);
    }//GEN-LAST:event_adminWorldTimeSetActionPerformed

    private void adminWorldGuestPurgeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminWorldGuestPurgeActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to purge the guest list?", "Purge Guest List?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
        {
            getConnectionManager().sendCommand("adminworld guest purge");
        }
    }//GEN-LAST:event_adminWorldGuestPurgeActionPerformed

    private void adminWorldGuestListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminWorldGuestListActionPerformed
        getConnectionManager().sendCommand("adminworld guest list");
    }//GEN-LAST:event_adminWorldGuestListActionPerformed

    private void adminWorldGuestRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminWorldGuestRemoveActionPerformed
        String name = adminWorldGuestNameText.getText();
        if (name.isEmpty())
        {
            return;
        }

        getConnectionManager().sendCommand("adminworld guest remove " + name);
    }//GEN-LAST:event_adminWorldGuestRemoveActionPerformed

    private void adminWorldGuestAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminWorldGuestAddActionPerformed
        String name = adminWorldGuestNameText.getText();
        if (name.isEmpty())
        {
            return;
        }

        getConnectionManager().sendCommand("adminworld guest add " + name);
    }//GEN-LAST:event_adminWorldGuestAddActionPerformed

    private void purgeBanlistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_purgeBanlistButtonActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to purge the ban list?", "Purge Ban List?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
        {
            getConnectionManager().sendCommand("banlist purge");
        }
    }//GEN-LAST:event_purgeBanlistButtonActionPerformed

    private void totalBansButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalBansButtonActionPerformed
        getConnectionManager().sendCommand("banlist");
    }//GEN-LAST:event_totalBansButtonActionPerformed

    private void tempbanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tempbanButtonActionPerformed
        String name = tempbanNameText.getText();
        if (name.isEmpty())
        {
            return;
        }
        String time = tempbanTimeText.getText();
        if (time.isEmpty())
        {
            return;
        }
        String reason = tempbanReasonText.getText();
        String command = "tempban " + name + " " + time;
        if (!reason.isEmpty())
        {
            command += " " + reason;
        }
        getConnectionManager().sendCommand(command);
    }//GEN-LAST:event_tempbanButtonActionPerformed

    private void unbanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unbanButtonActionPerformed
        String name = unbanNameText.getText();
        if (name.isEmpty())
        {
            return;
        }
        String command = "unban " + name;
        getConnectionManager().sendCommand(command);
    }//GEN-LAST:event_unbanButtonActionPerformed

    private void banButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_banButtonActionPerformed
        String name = banNameText.getText();
        if (name.isEmpty())
        {
            return;
        }
        String reason = banReasonText.getText();
        Boolean rollback = banRollbackToggle.isSelected();
        String command = "ban " + name;
        if (!reason.isEmpty())
        {
            command += " " + reason;
        }
        if (!rollback)
        {
            command += " -nrb";
        }
        getConnectionManager().sendCommand(command);

    }//GEN-LAST:event_banButtonActionPerformed

    private void announceSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_announceSendActionPerformed
        String message = announceText.getText();
        if (!message.isEmpty())
        {
            getConnectionManager().sendCommand("announce " + message);
        }
    }//GEN-LAST:event_announceSendActionPerformed

    private void adminChatSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adminChatSendActionPerformed
        String message = adminChatText.getText();
        if (!message.isEmpty())
        {
            getConnectionManager().sendCommand("o " + message);
        }
    }//GEN-LAST:event_adminChatSendActionPerformed

    private void rawsaySendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rawsaySendActionPerformed
        String message = rawsayText.getText();
        if (!message.isEmpty())
        {
            getConnectionManager().sendCommand("rawsay " + message);
        }
    }//GEN-LAST:event_rawsaySendActionPerformed

    private void cSaySendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cSaySendActionPerformed
        String message = cSayText.getText();
        if (!message.isEmpty())
        {
            getConnectionManager().sendCommand("csay " + message);
        }
    }//GEN-LAST:event_cSaySendActionPerformed

    private void saySendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saySendActionPerformed
        String message = sayText.getText();
        if (!message.isEmpty())
        {
            getConnectionManager().sendCommand("say " + message);
        }
    }//GEN-LAST:event_saySendActionPerformed

    private void chkShowChatOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowChatOnlyActionPerformed
        BukkitTelnetClient.config.filterShowChatOnly = chkShowChatOnly.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkShowChatOnlyActionPerformed

    private void chkIgnorePreprocessCommandsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnorePreprocessCommandsActionPerformed
        BukkitTelnetClient.config.filterIgnorePreprocessCommands = chkIgnorePreprocessCommands.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkIgnorePreprocessCommandsActionPerformed

    private void chkIgnoreServerCommandsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnoreServerCommandsActionPerformed
        BukkitTelnetClient.config.filterIgnoreServerCommands = chkIgnoreServerCommands.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkIgnoreServerCommandsActionPerformed

    private void chkIgnoreWarningsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnoreWarningsActionPerformed
        BukkitTelnetClient.config.filterIgnoreWarnings = chkIgnoreWarnings.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkIgnoreWarningsActionPerformed

    private void chkIgnoreErrorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnoreErrorsActionPerformed
        BukkitTelnetClient.config.filterIgnoreErrors = chkIgnoreErrors.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkIgnoreErrorsActionPerformed

    private void chkShowAdminChatOnlyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowAdminChatOnlyActionPerformed
        BukkitTelnetClient.config.filterShowAdminChatOnly = chkShowAdminChatOnly.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkShowAdminChatOnlyActionPerformed

    private void chkIgnoreAsyncWorldEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkIgnoreAsyncWorldEditActionPerformed
        BukkitTelnetClient.config.filterIgnoreAsyncWorldEdit = chkIgnoreAsyncWorldEdit.isSelected();
        BukkitTelnetClient.config.save();
    }//GEN-LAST:event_chkIgnoreAsyncWorldEditActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminChatLabel;
    private javax.swing.JButton adminChatSend;
    private javax.swing.JTextField adminChatText;
    private javax.swing.JButton adminListAdd;
    private javax.swing.JButton adminListClean;
    private javax.swing.JButton adminListInfo;
    private javax.swing.JLabel adminListNameLabel;
    private javax.swing.JTextField adminListNameText;
    private javax.swing.JPanel adminListPanel;
    private javax.swing.JComboBox<String> adminListRank;
    private javax.swing.JButton adminListRemove;
    private javax.swing.JSeparator adminListSeparator;
    private javax.swing.JButton adminListSetRank;
    private javax.swing.JButton adminListView;
    private javax.swing.JButton adminWorldGuestAdd;
    private javax.swing.JButton adminWorldGuestList;
    private javax.swing.JLabel adminWorldGuestNameLabel;
    private javax.swing.JTextField adminWorldGuestNameText;
    private javax.swing.JButton adminWorldGuestPurge;
    private javax.swing.JButton adminWorldGuestRemove;
    private javax.swing.JSeparator adminWorldGuestSeparator;
    private javax.swing.JPanel adminWorldPanel;
    private javax.swing.JComboBox<String> adminWorldTimeSelect;
    private javax.swing.JButton adminWorldTimeSet;
    private javax.swing.JComboBox<String> adminWorldWeatherSelect;
    private javax.swing.JButton adminWorldWeatherSet;
    private javax.swing.JLabel announceLabel;
    private javax.swing.JButton announceSend;
    private javax.swing.JTextField announceText;
    private javax.swing.JButton banButton;
    private javax.swing.JLabel banLabel;
    private javax.swing.JPanel banListPanel;
    private javax.swing.JLabel banNameLabel;
    private javax.swing.JTextField banNameText;
    private javax.swing.JLabel banReasonLabel;
    private javax.swing.JTextField banReasonText;
    private javax.swing.JCheckBox banRollbackToggle;
    private javax.swing.JSeparator banSeparator;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnSend;
    private javax.swing.JLabel cSayLabel;
    private javax.swing.JButton cSaySend;
    private javax.swing.JTextField cSayText;
    private javax.swing.JPanel chatPanel;
    private javax.swing.JCheckBox chkAutoScroll;
    private javax.swing.JCheckBox chkIgnoreAsyncWorldEdit;
    private javax.swing.JCheckBox chkIgnoreErrors;
    private javax.swing.JCheckBox chkIgnorePreprocessCommands;
    private javax.swing.JCheckBox chkIgnoreServerCommands;
    private javax.swing.JCheckBox chkIgnoreWarnings;
    private javax.swing.JCheckBox chkShowAdminChatOnly;
    private javax.swing.JCheckBox chkShowChatOnly;
    private javax.swing.JButton clearLogs;
    private javax.swing.JPanel commandsPanel;
    private javax.swing.JPanel favoriteButtonsPanel;
    private javax.swing.JPanel favoriteButtonsPanelHolder;
    private javax.swing.JScrollPane favoriteButtonsPanelScroll;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JPanel main;
    public javax.swing.JTextPane mainOutput;
    private javax.swing.JScrollPane mainOutputScoll;
    private javax.swing.JPanel playerListPanel;
    private javax.swing.JButton purgeBanlistButton;
    private javax.swing.JLabel rawsayLabel;
    private javax.swing.JButton rawsaySend;
    private javax.swing.JTextField rawsayText;
    private javax.swing.JLabel sayLabel;
    private javax.swing.JButton saySend;
    private javax.swing.JTextField sayText;
    private javax.swing.JTabbedPane sidebarPane;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTable tblPlayers;
    private javax.swing.JScrollPane tblPlayersScroll;
    private javax.swing.JButton tempbanButton;
    private javax.swing.JLabel tempbanLabel;
    private javax.swing.JLabel tempbanNameLabel;
    private javax.swing.JTextField tempbanNameText;
    private javax.swing.JLabel tempbanReasonLabel;
    private javax.swing.JTextField tempbanReasonText;
    private javax.swing.JSeparator tempbanSeparator;
    private javax.swing.JLabel tempbanTimeLabel;
    private javax.swing.JTextField tempbanTimeText;
    private javax.swing.JButton themeApplyCustom;
    private javax.swing.JCheckBox themeCustomDarkTheme;
    public javax.swing.JTextField themeCustomPath;
    private javax.swing.JButton themeFileSelect;
    private javax.swing.JPanel themePanel;
    private javax.swing.JScrollPane themeScrollPane;
    private javax.swing.JTable themeTable;
    private javax.swing.JButton totalBansButton;
    public static javax.swing.JLabel tps;
    private javax.swing.JTextField txtCommand;
    private javax.swing.JTextField txtNumPlayers;
    private javax.swing.JComboBox<me.StevenLawson.BukkitTelnetClient.ServerEntry> txtServer;
    private javax.swing.JButton unbanButton;
    private javax.swing.JLabel unbanLabel;
    private javax.swing.JLabel unbanNameLabel;
    private javax.swing.JTextField unbanNameText;
    private javax.swing.JSeparator unbanSeparator;
    private javax.swing.JPanel fontPanel;
    private javax.swing.JTextField fontSizeSelect;
    private javax.swing.JButton fontSizeSet;
    private javax.swing.JLabel fontSizeLabel;
    private String fontSizeString;
    private int fontSize;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JTextField fontSelect;
    private javax.swing.JButton fontSet;
    private String font;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JButton getBtnConnect()
    {
        return btnConnect;
    }

    public javax.swing.JButton getBtnDisconnect()
    {
        return btnDisconnect;
    }

    public javax.swing.JButton getBtnSend()
    {
        return btnSend;
    }

    public javax.swing.JTextPane getMainOutput()
    {
        return mainOutput;
    }

    public javax.swing.JTextField getTxtCommand()
    {
        return txtCommand;
    }

    public javax.swing.JComboBox<ServerEntry> getTxtServer()
    {
        return txtServer;
    }

    public JCheckBox getChkAutoScroll()
    {
        return chkAutoScroll;
    }

    public JCheckBox getChkIgnorePreprocessCommands()
    {
        return chkIgnorePreprocessCommands;
    }

    public JCheckBox getChkIgnoreServerCommands()
    {
        return chkIgnoreServerCommands;
    }

    public JCheckBox getChkShowChatOnly()
    {
        return chkShowChatOnly;
    }

    public JCheckBox getChkIgnoreWarnings()
    {
        return chkIgnoreWarnings;
    }

    public JCheckBox getChkIgnoreErrors()
    {
        return chkIgnoreErrors;
    }

    public JCheckBox getChkShowAdminChatOnly()
    {
        return chkShowAdminChatOnly;
    }

    public JCheckBox getChkIgnoreAsyncWorldEdit()
    {
        return chkIgnoreAsyncWorldEdit;
    }

    public List<PlayerInfo> getPlayerList()
    {
        return playerList;
    }

    public BTC_ConnectionManager getConnectionManager()
    {
        return connectionManager;
    }

}