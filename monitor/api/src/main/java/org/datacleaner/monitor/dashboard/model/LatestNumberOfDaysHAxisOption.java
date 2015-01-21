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
package org.datacleaner.monitor.dashboard.model;

import java.util.Date;

import org.datacleaner.monitor.dashboard.model.ChartOptions.HorizontalAxisOption;

/**
 * A {@link HorizontalAxisOption} implementation which allows the user to select
 * a timeline range with the meaning of eg. "the latest 30 days".
 */
public class LatestNumberOfDaysHAxisOption implements HorizontalAxisOption {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_NUMBER_OF_DAYS = 30;
    
    private int _latestNumberOfDays;
    
    public LatestNumberOfDaysHAxisOption() {
        this(DEFAULT_NUMBER_OF_DAYS);
    }

    public LatestNumberOfDaysHAxisOption(int latestNumberOfDays) {
        _latestNumberOfDays = latestNumberOfDays;
    }

    public int getLatestNumberOfDays() {
        return _latestNumberOfDays;
    }

    @Override
    public Date getBeginDate() {
        final long latestNumberOfMillis = _latestNumberOfDays * 1000l * 60 * 60 * 24;

        final Date now = new Date();
        final long time = now.getTime() - latestNumberOfMillis;

        return new Date(time);
    }

    @Override
    public Date getEndDate() {
        return new Date();
    }

}
