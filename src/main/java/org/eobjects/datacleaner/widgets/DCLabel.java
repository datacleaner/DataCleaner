/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.widgets;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.eobjects.datacleaner.util.WidgetUtils;

public class DCLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	public static DCLabel bright(String text) {
		return new DCLabel(text, WidgetUtils.BG_COLOR_BRIGHTEST, null);
	}

	public static DCLabel dark(String text) {
		return new DCLabel(text, WidgetUtils.BG_COLOR_DARKEST, null);
	}

	public static DCLabel brightMultiLine(String text) {
		return bright("<html>" + prepareMultiline(text) + "</html>");
	}

	public static DCLabel darkMultiLine(String text) {
		return dark("<html>" + prepareMultiline(text) + "</html>");
	}

	private static String prepareMultiline(String text) {
		if (text == null) {
			return "";
		}
		if (text.indexOf("<br") == -1) {
			return text.replaceAll("\n", "<br>");
		}
		return text;
	}

	public DCLabel(String text, Color textColor, Icon icon) {
		super();
		if (text != null) {
			setText(text);
		}
		if (textColor != null) {
			setForeground(textColor);
		}
		if (icon != null) {
			setIcon(icon);
		}
	}
}
