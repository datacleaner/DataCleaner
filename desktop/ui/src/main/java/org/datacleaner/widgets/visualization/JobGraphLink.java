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

import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.FilterOutcome;

/**
 * Represents a "link" (rendered as an edge) between two items in the
 * {@link JobGraph}.
 */
final class JobGraphLink {

    private final Object _from;
    private final Object _to;
    private final ComponentRequirement _requirement;
    private final FilterOutcome _filterOutcome;

    public JobGraphLink(Object from, Object to, ComponentRequirement requirement, FilterOutcome filterOutcome) {
        _from = from;
        _to = to;
        _requirement = requirement;
        _filterOutcome = filterOutcome;
    }

    public FilterOutcome getFilterOutcome() {
        return _filterOutcome;
    }

    public ComponentRequirement getRequirement() {
        return _requirement;
    }

    public Object getFrom() {
        return _from;
    }

    public Object getTo() {
        return _to;
    }

    @Override
    public String toString() {
        return "JobGraphLink[" + _from + "->" + _to + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_filterOutcome == null) ? 0 : _filterOutcome.hashCode());
        result = prime * result + ((_from == null) ? 0 : _from.hashCode());
        result = prime * result + ((_requirement == null) ? 0 : _requirement.hashCode());
        result = prime * result + ((_to == null) ? 0 : _to.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobGraphLink other = (JobGraphLink) obj;
        if (_filterOutcome == null) {
            if (other._filterOutcome != null)
                return false;
        } else if (!_filterOutcome.equals(other._filterOutcome))
            return false;
        if (_from == null) {
            if (other._from != null)
                return false;
        } else if (!_from.equals(other._from))
            return false;
        if (_requirement == null) {
            if (other._requirement != null)
                return false;
        } else if (!_requirement.equals(other._requirement))
            return false;
        if (_to == null) {
            if (other._to != null)
                return false;
        } else if (!_to.equals(other._to))
            return false;
        return true;
    }

    /**
     * Gets the label (if any) to show towards the user in the {@link JobGraph}.
     * 
     * @return
     */
    public String getLinkLabel() {
        FilterOutcome filterOutcome = getFilterOutcome();
        if (filterOutcome != null) {
            return filterOutcome.getCategory() + "";
        }
        final ComponentRequirement req = getRequirement();
        if (req != null) {
            return req.getSimpleName();
        }
        return null;
    }
}
