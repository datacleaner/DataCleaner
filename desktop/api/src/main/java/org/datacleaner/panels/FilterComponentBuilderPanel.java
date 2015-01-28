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
package org.datacleaner.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.datacleaner.actions.DisplayOptionsForFilterOutcomeActionListener;
import org.datacleaner.api.Filter;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.job.builder.FilterChangeListener;
import org.datacleaner.job.builder.FilterComponentBuilder;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.widgets.Alignment;
import org.datacleaner.widgets.ChangeRequirementMenuBuilder;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.PopupButton;
import org.datacleaner.widgets.properties.FormPanel;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

/**
 * Specialization of {@link AbstractComponentBuilderPanel} for {@link Filter}s.
 */
public class FilterComponentBuilderPanel extends AbstractComponentBuilderPanel implements
        FilterComponentBuilderPresenter, FilterChangeListener {

    private static final long serialVersionUID = 1L;

    private static final ImageManager imageManager = ImageManager.get();

    private final FilterComponentBuilder<?, ?> _filterComponentBuilder;
    private final DCPanel _outcomePanel;

    public FilterComponentBuilderPanel(FilterComponentBuilder<?, ?> filterJobBuilder, WindowContext windowContext,
            PropertyWidgetFactory propertyWidgetFactory) {
        this(null, 95, 95, filterJobBuilder, windowContext, propertyWidgetFactory);
    }

    protected FilterComponentBuilderPanel(Image watermarkImage, int watermarkHorizontalPosition,
            int watermarkVerticalPosition, FilterComponentBuilder<?, ?> filterComponentBuilder,
            WindowContext windowContext, PropertyWidgetFactory propertyWidgetFactory) {
        super(watermarkImage, watermarkHorizontalPosition, watermarkVerticalPosition, filterComponentBuilder,
                propertyWidgetFactory);

        _filterComponentBuilder = filterComponentBuilder;

        _outcomePanel = new DCPanel();
        _outcomePanel.setLayout(new FlowLayout(Alignment.LEFT.getFlowLayoutAlignment(), 0, 0));
        final Set<String> categoryNames = _filterComponentBuilder.getDescriptor().getOutcomeCategoryNames();
        for (final String categoryName : categoryNames) {
            final PopupButton outcomeButton = WidgetFactory.createDefaultPopupButton(categoryName,
                    IconUtils.FILTER_OUTCOME_PATH);

            outcomeButton.addActionListener(new DisplayOptionsForFilterOutcomeActionListener(outcomeButton,
                    _filterComponentBuilder, categoryName));
            _outcomePanel.add(outcomeButton);
            _outcomePanel.add(Box.createHorizontalStrut(4));
        }

        final JButton helpButton = WidgetFactory.createSmallButton(IconUtils.ACTION_HELP);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final DCPanel messagePanel = new DCPanel();
                messagePanel.setLayout(new BorderLayout());
                messagePanel.add(new JLabel(imageManager.getImageIcon("images/help/help_requirement_mapping.png")),
                        BorderLayout.WEST);
                messagePanel.add(DCLabel.darkMultiLine("Filter outcomes can be set as requirements<br>"
                        + "for other components, using the<br>" + "\""
                        + ChangeRequirementMenuBuilder.NO_REQUIREMENT_TEXT + "\" buttons.<br><br>"
                        + "You can also click the categories directly to eg.<br>"
                        + "write categorized records or to map them<br>"
                        + "as requirements for existing or new components."));
                JOptionPane.showMessageDialog(FilterComponentBuilderPanel.this, messagePanel,
                        "Help: Filter categories / outcomes", JOptionPane.PLAIN_MESSAGE);
            }
        });
        _outcomePanel.add(helpButton);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        _filterComponentBuilder.addChangeListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        _filterComponentBuilder.removeChangeListener(this);
    }

    protected JComponent decorateMainPanel(DCPanel panel) {
        final JComponent result = super.decorateMainPanel(panel);

        final FormPanel outcomeFormPanel = new FormPanel();
        outcomeFormPanel.addFormEntry("This filter categorizes records as", null, _outcomePanel);

        addTaskPane(IconUtils.FILTER_OUTCOME_PATH, "Outcomes", outcomeFormPanel);
        return result;
    }

    @Override
    public FilterComponentBuilder<?, ?> getComponentBuilder() {
        return _filterComponentBuilder;
    }

    @Override
    public void onAdd(FilterComponentBuilder<?, ?> fjb) {
    }

    @Override
    public void onConfigurationChanged(FilterComponentBuilder<?, ?> fjb) {
        onConfigurationChanged();
    }

    @Override
    public void onRemove(FilterComponentBuilder<?, ?> fjb) {
    }

    @Override
    public void onRequirementChanged(FilterComponentBuilder<?, ?> filterJobBuilder) {
    }
}
