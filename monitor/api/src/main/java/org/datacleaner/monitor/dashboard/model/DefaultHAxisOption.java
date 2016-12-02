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

public class DefaultHAxisOption implements ChartOptions.HorizontalAxisOption {

    private static final long serialVersionUID = 1L;

    private Date _endDate;
    private Date _beginDate;

    public DefaultHAxisOption() {
        this(null, null);
    }

    public DefaultHAxisOption(final Date beginDate, final Date endDate) {
        _beginDate = beginDate;
        _endDate = endDate;
    }

    @Override
    public Date getBeginDate() {
        return _beginDate;
    }

    @Override
    public Date getEndDate() {
        return _endDate;
    }

}
