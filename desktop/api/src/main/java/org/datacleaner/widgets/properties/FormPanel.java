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
package org.datacleaner.widgets.properties;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.jdesktop.swingx.JXLabel;

/**
 * A panel that lays out inputs of in a vertical form-like manner
 */
public class FormPanel extends DCPanel {
    
    private static final long serialVersionUID = 1L;

    private static final int FIELD_LABEL_WIDTH = 200;
    private static final Insets insets = new Insets(4, 4, 4, 4);;
    private int _rowCounter;

    public FormPanel() {
        super();
        GridBagLayout layout = new GridBagLayout();
        layout.columnWidths = new int[] { FIELD_LABEL_WIDTH };
        setLayout(layout);
        _rowCounter = 0;
    }

    public boolean isEmpty() {
        return getComponentCount() == 0;
    }

    /**
     * Adds a form entry to the panel
     * 
     * @param mainLabelText
     * @param minorLabelText
     * @param component
     */
    public void addFormEntry(String mainLabelText, String minorLabelText, JComponent component) {
        if (!mainLabelText.endsWith(":")) {
            mainLabelText += ":";
        }

        final DCLabel mainLabel = DCLabel.dark(mainLabelText);
        mainLabel.setFont(WidgetUtils.FONT_SMALL);

        final JXLabel minorLabel;
        if (StringUtils.isNullOrEmpty(minorLabelText)) {
            minorLabel = null;
        } else {
            mainLabel.setToolTipText(minorLabelText);

            minorLabel = new JXLabel(minorLabelText);
            minorLabel.setLineWrap(true);
            minorLabel.setFont(WidgetUtils.FONT_SMALL);
            minorLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
            minorLabel.setVerticalAlignment(JXLabel.TOP);
            minorLabel.setPreferredSize(new Dimension(FIELD_LABEL_WIDTH - 4, 0));
        }

        addFormEntry(mainLabel, minorLabel, component);
    }

    /**
     * Adds a form entry to the panel
     * 
     * @param mainLabel
     * @param minorLabel
     * @param component
     */
    public void addFormEntry(JLabel mainLabel, JLabel minorLabel, JComponent component) {
        add(mainLabel, new GridBagConstraints(0, _rowCounter, 1, 1, 0d, 0d, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, insets, 0, 0));

        if (minorLabel != null) {
            add(minorLabel, new GridBagConstraints(0, _rowCounter + 1, 1, 1, 0d, 1d, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.BOTH, insets, 0, 0));
        }

        add(component, new GridBagConstraints(1, _rowCounter, 1, 2, 1d, 1d, GridBagConstraints.NORTHEAST,
                GridBagConstraints.BOTH, insets, 0, 0));

        // each property spans two "rows"
        _rowCounter = _rowCounter + 2;
    }
}
