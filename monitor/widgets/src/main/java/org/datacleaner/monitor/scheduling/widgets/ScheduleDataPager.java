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

import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

class ScheduleDataPager extends SimplePager {
    ScheduleDataPager() {
        super(SimplePager.TextLocation.CENTER, false, true);
    }

    @Override
    public void setPageStart(final int index) {
        final HasRows display = getDisplay();

        if (display != null) {
            final Range range = display.getVisibleRange();
            final int newStartIndex = Math.max(0, index);
            if (newStartIndex != range.getStart()) {
                display.setVisibleRange(newStartIndex, range.getLength());
            }
        }
    }
}
