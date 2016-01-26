/**
 * DataCleaner (community edition)
 * Copyright (C) 2014 Neopost - Customer Information Management
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.datacleaner.monitor.shared.widgets;

import com.google.gwt.user.client.ui.Button;

/**
 * A utility for dealing with GWT {@link Button}s. Used to apply styling and
 * common tricks to make the buttons fit in with our site.
 */
public class DCButtons {

    public static Button primaryButton(String glyphiconName, String text) {
        Button button = new Button(createHtml(glyphiconName, text));
        applyPrimaryStyle(button);
        return button;
    }

    public static Button defaultButton(String glyphiconName, String text) {
        Button button = new Button(createHtml(glyphiconName, text));
        applyDefaultStyle(button);
        return button;
    }

    public static Button dangerButton(String glyphiconName, String text) {
        Button button = new Button(createHtml(glyphiconName, text));
        applyDangerStyle(button);
        return button;
    }

    public static void applyDefaultStyle(Button button) {
        button.setStylePrimaryName("btn");
        button.addStyleName("btn-default");
    }

    public static void applyPrimaryStyle(Button button) {
        button.setStylePrimaryName("btn");
        button.addStyleName("btn-primary");
    }

    public static void applyDangerStyle(Button button) {
        button.setStylePrimaryName("btn");
        button.addStyleName("btn-danger");
    }

    public static String createHtml(String glyphiconName, String text) {
        if (glyphiconName == null) {
            return text;
        }
        if (text == null) {
            return "<span class=\"glyphicon glyphicon-only " + glyphiconName + "\" aria-hidden=\"true\"></span>";
        }

        return "<span class=\"glyphicon " + glyphiconName + "\" aria-hidden=\"true\"></span><span>" + text + "</span>";
    }
}
