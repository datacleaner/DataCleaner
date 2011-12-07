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
import javax.swing.SwingConstants;

import org.eobjects.datacleaner.util.WidgetUtils;

public class DCLabel extends JLabel {

	private static final long serialVersionUID = 1L;

	private final boolean _multiline;

	public static DCLabel bright(String text) {
		return new DCLabel(false, text, WidgetUtils.BG_COLOR_BRIGHTEST, null);
	}

	public static DCLabel dark(String text) {
		return new DCLabel(false, text, WidgetUtils.BG_COLOR_DARKEST, null);
	}

	public static DCLabel brightMultiLine(String text) {
		return new DCLabel(true, text, WidgetUtils.BG_COLOR_BRIGHTEST, null);
	}

	public static DCLabel darkMultiLine(String text) {
		return new DCLabel(true, text, WidgetUtils.BG_COLOR_DARKEST, null);
	}

	public DCLabel(boolean multiline, String text, Color textColor, Icon icon) {
		super();
		_multiline = multiline;
		if (text != null) {
			setText(text);
		}
		if (textColor != null) {
			setForeground(textColor);
		}
		if (icon != null) {
			setIcon(icon);
		}
		setVerticalAlignment(SwingConstants.TOP);
	}

	@Override
	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		if (_multiline) {
			if (text.indexOf("</p>") == -1) {
				text = "<p>" + text + "</p>";
			}
			if (text.indexOf("<html>") == -1) {
				text = "<html>" + text + "</html>";
			}
			if (text.indexOf("<br") == -1) {
				text = text.replaceAll("\n", "<br>");
			}
		}
		super.setText(text);
	}

	public void setMaximumWidth(int width) {
		String text = getText();
		if (text.startsWith("<html>") && text.endsWith("</html>")) {
			// remove <html> tags since that will be added below also
			text = text.substring(6, text.length() - 7);
		}
		super.setText("<html><table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"" + width + "\"><tr><td>"
				+ text + "</td></tr></table></html>");
	}
}
