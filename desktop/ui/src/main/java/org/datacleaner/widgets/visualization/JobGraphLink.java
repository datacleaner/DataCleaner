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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.datacleaner.api.OutputDataStream;
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
    private final OutputDataStream _outputDataStream;

    public JobGraphLink(Object from, Object to, ComponentRequirement requirement, FilterOutcome filterOutcome,
            OutputDataStream outputDataStream) {
        _from = from;
        _to = to;
        _requirement = requirement;
        _filterOutcome = filterOutcome;
        _outputDataStream = outputDataStream;
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
    
    public OutputDataStream getOutputDataStream() {
        return _outputDataStream;
    }

    @Override
    public String toString() {
        return "JobGraphLink[" + _from + "->" + _to + "]";
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(_filterOutcome);
        hashCodeBuilder.append(_from);
        hashCodeBuilder.append(_requirement);
        hashCodeBuilder.append(_to);
        hashCodeBuilder.append(_outputDataStream);
        return hashCodeBuilder.toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JobGraphLink)) {
            return false;
        }
        final JobGraphLink other = (JobGraphLink) obj;
        final EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(_filterOutcome, other._filterOutcome);
        equalsBuilder.append(_from, other._from);
        equalsBuilder.append(_requirement, other._requirement);
        equalsBuilder.append(_to, other._to);
        equalsBuilder.append(_outputDataStream, other._outputDataStream);
        return equalsBuilder.isEquals();

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
        final OutputDataStream outputDataStream = getOutputDataStream();
        if (outputDataStream != null) {
            return outputDataStream.getName();
        }
        return null;
    }
}
