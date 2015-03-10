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
package org.datacleaner.widgets.visualization;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.datacleaner.panels.DCPanel;
import org.datacleaner.user.UserPreferences;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.elasticsearch.common.base.Strings;
import org.jdesktop.swingx.HorizontalLayout;

/**
 * Panel which is shown in a popup to modify visual/UI preferences of the job
 * graph.
 */
public class JobGraphPreferencesPanel extends DCPanel {

    private static final long serialVersionUID = 1L;

    private final UserPreferences _userPreferences;
    private final JobGraph _jobGraph;

    public JobGraphPreferencesPanel(UserPreferences userPreferences, JobGraph jobGraph) {
        super(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        setBorder(null);
        setLayout(new HorizontalLayout(4));

        _userPreferences = userPreferences;
        _jobGraph = jobGraph;

        add(createHintsToggle());

        add(Box.createHorizontalStrut(10));

        addLabel("Line style:");
        add(createLineStyleButton("images/menu/edge-straight.png", JobGraphTransformers.EDGE_STYLE_NAME_STRAIGHT));
        add(createLineStyleButton("images/menu/edge-curved.png", JobGraphTransformers.EDGE_STYLE_NAME_CURVED));

        // the orthogonal line style does not work well enough for inclusion
        // yet.

        // add(createLineStyleButton("images/menu/edge-orthogonal.png",
        // JobGraphTransformers.EDGE_STYLE_NAME_ORTOGHONAL));

        add(Box.createHorizontalStrut(10));

        addLabel("Font size:");
        add(createFontFactorButton(IconUtils.ACTION_ADD, 0.1f));
        add(createFontFactorButton(IconUtils.ACTION_REMOVE, -0.1f));
        add(Box.createHorizontalStrut(10));
    }

    private JComponent createHintsToggle() {
        final ImageIcon icon = ImageManager.get().getImageIcon(IconUtils.ACTION_HELP, IconUtils.ICON_SIZE_SMALL);
        final JToggleButton toggle = new JToggleButton("Show hints", icon, true);
        WidgetUtils.setDefaultButtonStyle(toggle);
        toggle.setMargin(WidgetUtils.INSETS_EMPTY);
        
        toggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String existingValue = _userPreferences.getAdditionalProperties().get(
                        JobGraphTransformers.USER_PREFERENCES_PROPERTY_SHOW_CANVAS_HINTS);
                final String newValue;
                if (existingValue == null || "true".equals(existingValue)) {
                    newValue = "false";
                } else {
                    newValue = "true";
                }
                _userPreferences.getAdditionalProperties().put(
                        JobGraphTransformers.USER_PREFERENCES_PROPERTY_SHOW_CANVAS_HINTS, newValue);
                updateToggleText(toggle);
                _jobGraph.refresh();
            }
        });
        updateToggleText(toggle);
        return toggle;
    }

    protected void updateToggleText(JToggleButton toggle) {
        final String showHints = _userPreferences.getAdditionalProperties().get(
                JobGraphTransformers.USER_PREFERENCES_PROPERTY_SHOW_CANVAS_HINTS);
        if (showHints == null || "true".equals(showHints)) {
            toggle.setSelected(true);
        } else {
            toggle.setSelected(false);
        }
    }

    private Component createFontFactorButton(final String imagePath, final float increment) {
        final JButton button = WidgetFactory.createSmallButton(imagePath);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String factorString = _userPreferences.getAdditionalProperties().get(
                        JobGraphTransformers.USER_PREFERENCES_PROPERTY_FONT_SIZE_FACTOR);
                final float existingFactor;
                if (Strings.isNullOrEmpty(factorString)) {
                    existingFactor = 1.0f;
                } else {
                    existingFactor = Float.parseFloat(factorString);
                }

                final float newFactor = existingFactor + increment;
                _userPreferences.getAdditionalProperties().put(
                        JobGraphTransformers.USER_PREFERENCES_PROPERTY_FONT_SIZE_FACTOR, newFactor + "");
                _jobGraph.refresh();
            }
        });
        return button;
    }

    private JButton createLineStyleButton(final String imagePath, final String typeName) {
        final JButton button = WidgetFactory.createSmallButton(imagePath);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _userPreferences.getAdditionalProperties().put(
                        JobGraphTransformers.USER_PREFERENCES_PROPERTY_EDGE_STYLE, typeName);
                _jobGraph.refresh();
            }
        });
        return button;
    }

    private void addLabel(String text) {
        DCLabel label = DCLabel.dark(text);
        label.setVerticalAlignment(SwingConstants.BOTTOM);
        add(label);
    }
}
