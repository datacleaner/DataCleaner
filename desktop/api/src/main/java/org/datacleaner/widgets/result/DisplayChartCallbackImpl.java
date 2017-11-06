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
package org.datacleaner.widgets.result;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXCollapsiblePane;

/**
 * Default {@link DisplayChartCallback} implementation.
 */
public class DisplayChartCallbackImpl implements DisplayChartCallback {

    private final JXCollapsiblePane _chartContainer;

    /**
     * @deprecated use {@link #DisplayChartCallbackImpl(JXCollapsiblePane)} instead
     */
    @Deprecated
    public DisplayChartCallbackImpl(final JXCollapsiblePane chartContainer, final boolean allowAnimations) {
        this(chartContainer);
    }

    public DisplayChartCallbackImpl(final JXCollapsiblePane chartContainer) {
        _chartContainer = chartContainer;
    }

    @Override
    public void displayChart(final JComponent chart) {
        _chartContainer.getContentPane().removeAll();
        _chartContainer.getContentPane().add(chart);
        _chartContainer.setCollapsed(false);

        _chartContainer.updateUI();
    }
}
