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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.datacleaner.monitor.scheduling.model.ScheduleDefinition;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.RangeChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;

class ScheduleDataPanel extends FlowPanel implements HasRows, HasData<ScheduleDefinition> {
    private final ScheduleData _data;

    private final Set<ScheduleGroupPanel> _groupPanels = new HashSet<>();

    ScheduleDataPanel(final SchedulingOverviewPanel overviewPanel) {
        super();

        _data = new ScheduleData(this, overviewPanel);
    }

    @Override
    public HandlerRegistration addRangeChangeHandler(Handler handler) {
        return _data.addRangeChangeHandler(handler);
    }

    @Override
    public HandlerRegistration addRowCountChangeHandler(
            com.google.gwt.view.client.RowCountChangeEvent.Handler handler) {
        return _data.addRowCountChangeHandler(handler);
    }

    @Override
    public int getRowCount() {
        return _data.getRowCount();
    }

    @Override
    public Range getVisibleRange() {
        return _data.getVisibleRange();
    }

    @Override
    public boolean isRowCountExact() {
        return _data.isRowCountExact();
    }

    @Override
    public void setRowCount(int count) {
        _data.setRowCount(count);
    }

    @Override
    public void setRowCount(int count, boolean isExact) {
        _data.setRowCount(count, isExact);
    }

    @Override
    public void setVisibleRange(int start, int length) {
        _data.setVisibleRange(start, length);
    }

    @Override
    public void setVisibleRange(Range range) {
        _data.setVisibleRange(range);
    }

    @Override
    public HandlerRegistration addCellPreviewHandler(
            com.google.gwt.view.client.CellPreviewEvent.Handler<ScheduleDefinition> handler) {
        return _data.addCellPreviewHandler(handler);
    }

    @Override
    public SelectionModel<? super ScheduleDefinition> getSelectionModel() {
        return _data.getSelectionModel();
    }

    @Override
    public ScheduleDefinition getVisibleItem(int indexOnPage) {
        return _data.getVisibleItem(indexOnPage);
    }

    @Override
    public int getVisibleItemCount() {
        return _data.getVisibleItemCount();
    }

    @Override
    public Iterable<ScheduleDefinition> getVisibleItems() {
        return _data.getVisibleItems();
    }

    @Override
    public void setRowData(int start, List<? extends ScheduleDefinition> values) {
        _data.setRowData(start, values);
    }

    @Override
    public void setSelectionModel(SelectionModel<? super ScheduleDefinition> selectionModel) {
        _data.setSelectionModel(selectionModel);
    }

    @Override
    public void setVisibleRangeAndClearData(Range range, boolean forceRangeChangeEvent) {
        _data.setVisibleRangeAndClearData(range, forceRangeChangeEvent);
    }

    void registerGroupPanel(ScheduleGroupPanel groupPanel) {
        _groupPanels.add(groupPanel);
    }

    void clearGroupPanels() {
        for (ScheduleGroupPanel groupPanel : _groupPanels) {
            remove(groupPanel);
        }

        _groupPanels.clear();
    }
}
