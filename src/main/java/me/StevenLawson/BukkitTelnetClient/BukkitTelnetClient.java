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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class BukkitTelnetClient
{
    public static final String VERSION = "v1.1.1";
    public static final Logger LOGGER = Logger.getLogger(BukkitTelnetClient.class.getName());
    public static BTC_MainPanel mainPanel = null;
    public static Themes themes = new Themes();
    public static BTC_ConfigLoader config = new BTC_ConfigLoader();

    public static void main(String args[])
    {
        config.load(true);

        SwingUtilities.invokeLater(() ->
        {
            mainPanel = new BTC_MainPanel();
            mainPanel.setup();
        });
    }

    // JDK 7 safe getDeclaredAnnotation
    public static <T extends Annotation> T getDeclaredAnnotation(final Method method, final Class<T> annotationClass)
    {
        java.util.Objects.requireNonNull(annotationClass);

        T annotation = null;

        for (final Annotation _annotation : method.getDeclaredAnnotations())
        {
            if (_annotation.annotationType() == annotationClass)
            {
                annotation = annotationClass.cast(_annotation);
                break;
            }
        }

        return annotation;
    }
}
