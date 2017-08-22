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
package org.datacleaner.widgets;

import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

import org.datacleaner.panels.DCBannerPanel;
import org.datacleaner.util.WidgetUtils;

/**
 * A specific type of label with description texts, typically placed below a
 * {@link DCBannerPanel}.
 */
public class DescriptionLabel extends DCLabel {

    private static final long serialVersionUID = 1L;

    public DescriptionLabel() {
        this(null);
    }

    @SuppressWarnings("deprecation")
    public DescriptionLabel(final String text) {
        super(true, text, WidgetUtils.BG_COLOR_DARK, null);
        setFont(WidgetUtils.FONT_SMALL);
        setOpaque(true);
        setBackground(WidgetUtils.BG_COLOR_PALE_YELLOW);
        setBorder(new CompoundBorder(new MatteBorder(0, 1, 1, 1, WidgetUtils.BG_COLOR_LESS_BRIGHT),
                WidgetUtils.BORDER_EMPTY));
    }
}
