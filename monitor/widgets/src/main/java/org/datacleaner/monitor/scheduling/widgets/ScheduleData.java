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
package org.datacleaner.monitor.scheduling.widgets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractHasData;
import com.google.gwt.view.client.SelectionModel;

class ScheduleData extends AbstractHasData<ScheduleDefinition> {
    private final ScheduleDataPanel _panel;
    private final SchedulingOverviewPanel _overviewPanel;
    private final Element _childContainer;

    ScheduleData(final ScheduleDataPanel panel, final SchedulingOverviewPanel overviewPanel) {
        super(Document.get().createDivElement(), SchedulingOverviewPanel.PAGE_SIZE, null);

        _panel = panel;
        _overviewPanel = overviewPanel;

        _childContainer = Document.get().createDivElement();
        final DivElement outerDiv = getElement().cast();
        outerDiv.appendChild(_childContainer);
    }

    @Override
    protected boolean dependsOnSelection() {
        return false;
    }

    @Override
    protected Element getChildContainer() {
        return _childContainer;
    }

    @Override
    protected Element getKeyboardSelectedElement() {
        return null;
    }

    @Override
    protected boolean isKeyboardNavigationSuppressed() {
        return false;
    }

    @Override
    protected void renderRowValues(final SafeHtmlBuilder sb, final List<ScheduleDefinition> values, int start,
            final SelectionModel<? super ScheduleDefinition> selectionModel) throws UnsupportedOperationException {
        _panel.clearGroupPanels();

        final Map<String, ScheduleGroupPanel> scheduleGroupPanels = new HashMap<>();
        for (final ScheduleDefinition scheduleDefinition : values) {
            final ScheduleGroupPanel groupPanel = _overviewPanel.addScheduleInGroup(scheduleDefinition,
                    SchedulingOverviewPanel.JOB_GROUPING_CATEGORY, _panel, scheduleGroupPanels);
            _panel.registerGroupPanel(groupPanel);
        }
    }

    @Override
    protected boolean resetFocusOnCell() {
        return false;
    }

    @Override
    protected void setKeyboardSelected(final int index, final boolean selected, final boolean stealFocus) {
        // Do nothing.
    }
}
