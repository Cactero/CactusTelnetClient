package me.StevenLawson.BukkitTelnetClient;

import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.util.stream.Stream;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Themes {

    public HashMap<String, FlatLaf> defaultThemes = new HashMap<>();
    public HashMap<String, String> basicThemes = new HashMap<>();
    public HashMap<String, String> lightThemes = new HashMap<>();
    public HashMap<String, String> darkThemes = new HashMap<>();
    public String lastSelectedTheme = null;
    public String customThemePath = null;
    public int lastSelectedFontSize = 0;
    public String lastSelectedFont = null;
    public boolean useCustomTheme = false;
    public boolean darkTheme = false;
    public boolean customThemeDarkTheme = false;
    private DefaultTableModel table;

    public void setupThemeMaps()
    {
        defaultThemes.put("Light", new FlatLightLaf());
        defaultThemes.put("Dark", new FlatDarkLaf());

        if (SystemInfo.IS_MAC)
        {
            basicThemes.put("Apple", "com.apple.laf.AquaLookAndFeel");
        }
        if (SystemInfo.IS_LINUX)
        {
            basicThemes.put("GTK", "com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        }
        basicThemes.put("Metal", "javax.swing.plaf.metal.MetalLookAndFeel");
        basicThemes.put("Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        basicThemes.put("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel");
        if (SystemInfo.IS_WINDOWS)
        {
            basicThemes.put("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }

        lightThemes.put("Arc", "arc");
        lightThemes.put("Arc Orange", "arc-orange");
        lightThemes.put("Cyan", "cyan");
        lightThemes.put("GitHub", "github");
        lightThemes.put("IntellIJ", "intellij-light");
        lightThemes.put("Light Owl", "intellij-light");
        lightThemes.put("Lighter", "material-lighter");
        lightThemes.put("Lighter 2", "material-lighter-contrast");
        lightThemes.put("Solarized", "solarized");
        lightThemes.put("Solarized 2", "solarized-contrast");

        darkThemes.put("Arc Dark", "arc-dark");
        darkThemes.put("Arc Dark 2", "arc-dark-contrast");
        darkThemes.put("Atom One", "atom-one");
        darkThemes.put("Atom One 2", "atom-one-contrast");
        darkThemes.put("Dark Flat", "dark-flat");
        darkThemes.put("VS Dark", "dark-purple");
        darkThemes.put("Dracula", "dracula");
        darkThemes.put("Dracula 2", "dracula-2");
        darkThemes.put("Uchsia", "gradianto-dark-uchsia");
        darkThemes.put("Deep Ocean", "gradianto-deep-ocean");
        darkThemes.put("Midnight Blue", "gradianto-midnight-blue");
        darkThemes.put("Gruvbox", "gruvbox");
        darkThemes.put("Hiberbee", "hiberbee");
        darkThemes.put("High Contrast", "high-contrast");
        darkThemes.put("Darker", "material-darker");
        darkThemes.put("Darker 2", "material-darker-contrast");
        darkThemes.put("Design", "material-design");
        darkThemes.put("Oceanic", "material-oceanic");
        darkThemes.put("Oceanic 2", "material-oceanic-contrast");
        darkThemes.put("Palenight", "material-palenight-contrast");
        darkThemes.put("Monocai", "monocai");
        darkThemes.put("Monocai Pro", "monocai-pro");
        darkThemes.put("Monocai Pro 2", "monokai-pro-contrast");
        darkThemes.put("Night Owl", "night-owl-contrast");
        darkThemes.put("Nord", "nord");
        darkThemes.put("Spacegray", "spacegray");
        darkThemes.put("Vuesion", "vuesion");
        //darkThemes.put("Lyicx", "lyicx"); Soon
    }

    public void setupThemeTable(JTable themeTable)
    {
        String[] categoryNameList = {"Default", "Basic", "Light", "Dark"};

        String[] defaultThemeList = hashMapKeysToSortedStringArray(defaultThemes);
        String[] basicThemeList = hashMapKeysToSortedStringArray(basicThemes);
        String[] lightThemeList = hashMapKeysToSortedStringArray(lightThemes);
        String[] darkThemeList = hashMapKeysToSortedStringArray(darkThemes);

        int[] lengths = {defaultThemeList.length, basicThemeList.length, lightThemeList.length, darkThemeList.length};
        int biggestColumnSize = NumberUtils.max(lengths);

        String[][] emptyTable = new String[biggestColumnSize][4];
        table = new DefaultTableModel(emptyTable, categoryNameList);

        populateColumn(defaultThemeList, 0);
        populateColumn(basicThemeList, 1);
        populateColumn(lightThemeList, 2);
        populateColumn(darkThemeList, 3);

        themeTable.setModel(table);
        themeTable.setCellSelectionEnabled(true);
        themeTable.setDefaultEditor(Object.class, null);
        themeTable.getTableHeader().setReorderingAllowed(false);
        themeTable.getTableHeader().setResizingAllowed(false);
    }

    private String[] hashMapKeysToSortedStringArray(HashMap<String, ?> hashMap)
    {
        String[] stringList = {};
        String[] list = hashMap.keySet().toArray(stringList);
        list = Stream.of(list).sorted().toArray(String[]::new);
        return list;
    }

    private void populateColumn(String[] themeList, int column)
    {
        int row = 0;
        for (String themeName : themeList)
        {
            table.setValueAt(themeName, row, column);
            row++;
        }
    }

    public String selectFile()
    {
        JFileChooser fileChooser = newFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"));
        int status = fileChooser.showOpenDialog(null);
        if (status == JFileChooser.APPROVE_OPTION)
        {
            customThemePath = fileChooser.getSelectedFile().getPath();
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public JFileChooser newFileChooser()
    {
        String path = customThemePath;
        if (path == null)
        {
            path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        }
        JFileChooser fileChooser = new JFileChooser(new File(path))
        {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException
            {
                JDialog dialog = super.createDialog(parent);
                dialog.setIconImage(Toolkit.getDefaultToolkit().createImage(dialog.getClass().getResource("/icon.png")));
                return dialog;

            }
        };
        return fileChooser;
    }

    public void applyCustomTheme(boolean save)
    {
        BTC_MainPanel panel = BukkitTelnetClient.mainPanel;
        String path = panel.themeCustomPath.getText();
        panel.setCustomTheme(path, save);
    }

    public void selectTheme(JTable themeTable, boolean save)
    {
        BTC_MainPanel panel = BukkitTelnetClient.mainPanel;
        String selectedTheme = getSelectedTableCell(themeTable);
        lastSelectedTheme = selectedTheme;
        useCustomTheme = false;
        if (selectedTheme == null)
        {
            return;
        }
        if (basicThemes.get(selectedTheme) != null)
        {
            panel.setLookAndFeel(basicThemes.get(selectedTheme));
            darkTheme = false;
        }
        else if (defaultThemes.get(selectedTheme) != null)
        {
            panel.setFlatLookAndFeel(defaultThemes.get(selectedTheme));
            if (selectedTheme.equals("Dark"))
            {
                darkTheme = true;
            }
            else
            {
                darkTheme = false;
            }
        }
        else if (lightThemes.get(selectedTheme) != null)
        {
            panel.setFlatLookAndFeel(lightThemes.get(selectedTheme), false);
            darkTheme = false;
        }
        else if (darkThemes.get(selectedTheme) != null)
        {
            panel.setFlatLookAndFeel(darkThemes.get(selectedTheme),true);
            darkTheme = true;
        }

        if (save)
        {
            BukkitTelnetClient.config.save();
        }
    }

    public String getSelectedTableCell(JTable themeTable)
    {
        int selectedRow = themeTable.getSelectedRow();
        int selectedColumn = themeTable.getSelectedColumn();
        try
        {
            String selectedTheme = themeTable.getValueAt(selectedRow, selectedColumn).toString();
            return selectedTheme;
        }
        catch (NullPointerException e)
        {
            return null;
        }
    }

    public void loadSettings(JTable themeTable, JTextField customThemePathField)
    {
        customThemePathField.setText(customThemePath);
        if (!useCustomTheme)
        {
            selectLastTheme(themeTable, lastSelectedTheme);
            selectTheme(themeTable, false);
        }
        else
        {
            applyCustomTheme(false);
            darkTheme = customThemeDarkTheme;
        }
    }

    public void selectLastTheme(JTable themeTable, String lastTheme)
    {
        for (int row = 0; row < themeTable.getRowCount(); row++)
        {
            for (int column = 0; column < 4; column++)
            {
                String cellValue = String.valueOf(themeTable.getValueAt(row, column));
                if (cellValue.equals(lastTheme))
                {
                    themeTable.changeSelection(row, column, false, false);
                }
            }
        }
    }

    public Element toXML(Document doc)
    {
        Element themes = doc.createElement("theme");

        Element lastTheme = doc.createElement("lastSelectedTheme");
        lastTheme.appendChild(doc.createTextNode(lastSelectedTheme));
        themes.appendChild(lastTheme);

        Element customPath = doc.createElement("customThemePath");
        customPath.appendChild(doc.createTextNode(customThemePath));
        themes.appendChild(customPath);

        Element useCustom = doc.createElement("useCustomTheme");
        useCustom.appendChild(doc.createTextNode(String.valueOf(useCustomTheme)));
        themes.appendChild(useCustom);

        Element isDarkTheme = doc.createElement("darkTheme");
        isDarkTheme.appendChild(doc.createTextNode(String.valueOf(darkTheme)));
        themes.appendChild(isDarkTheme);

        Element isCustomThemeDarkTheme = doc.createElement("customThemeDarkTheme");
        isCustomThemeDarkTheme.appendChild(doc.createTextNode(String.valueOf(customThemeDarkTheme)));
        themes.appendChild(isCustomThemeDarkTheme);

        Element lastFontSize = doc.createElement("lastSelectedFontSize");
        lastFontSize.appendChild(doc.createTextNode(String.valueOf(lastSelectedFontSize)));
        themes.appendChild(lastFontSize);

        Element lastFont = doc.createElement("lastSelectedFont");
        lastFont.appendChild(doc.createTextNode(lastSelectedFont));
        themes.appendChild(lastFont);

        return themes;
    }

    public Color checkColor(Color color)
    {
        Color PURPLE = BTC_TelnetMessage.PURPLE;
        Color LIGHT_PURPLE = new Color(180, 160, 255);
        Color DARK_GREEN = BTC_TelnetMessage.DARK_GREEN;
        Color LIGHT_GREEN = new Color(106, 150, 23);
        Color LIGHT_BLUE = new Color(130, 210, 255);
        Color DARK_ORANGE = new Color(255, 147, 5);
        Color LIGHT_RED = new Color(255, 138, 138);
        if (BukkitTelnetClient.themes.darkTheme)
        {
            if (color == Color.BLACK)
            {
                color = Color.WHITE;
            }
            else if (color == PURPLE)
            {
                color = LIGHT_PURPLE;
            }
            else if (color == DARK_GREEN)
            {
                color = LIGHT_GREEN;
            }
            else if (color == Color.BLUE)
            {
                color = LIGHT_BLUE;
            }
            else if (color == Color.RED)
            {
                color = LIGHT_RED;
            }
        }
        else
        {
            if (color == Color.YELLOW)
            {
                color = DARK_ORANGE;
            }
        }


        return color;
    }
}