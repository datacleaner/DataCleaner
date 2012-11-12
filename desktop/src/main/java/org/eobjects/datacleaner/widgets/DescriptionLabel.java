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

import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import org.eobjects.datacleaner.panels.DCBannerPanel;
import org.eobjects.datacleaner.util.WidgetUtils;

/**
 * A specific type of label with description texts, typically placed below a
 * {@link DCBannerPanel}.
 */
public class DescriptionLabel extends DCLabel {

    private static final long serialVersionUID = 1L;
    
    public DescriptionLabel() {
        this(null);
    }

    public DescriptionLabel(String text) {
        super(true, text, WidgetUtils.BG_COLOR_DARK, null);
        setFont(WidgetUtils.FONT_SMALL);
        setOpaque(true);
        setBackground(WidgetUtils.BG_COLOR_PALE_YELLOW);
        setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, WidgetUtils.BG_COLOR_MEDIUM), WidgetUtils.BORDER_EMPTY));
    }
}
