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
package org.datacleaner.job;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.util.ReadObjectBuilder;

public final class ImmutableFilterJob extends ImmutableComponentJob implements FilterJob {

    private static final long serialVersionUID = 1L;

    public ImmutableFilterJob(String name, FilterDescriptor<?, ?> descriptor,
            ComponentConfiguration componentConfiguration, ComponentRequirement componentRequirement,
            Map<String, String> metadataProperties, OutputDataStreamJob[] outputDataStreamJobs) {
        super(name, descriptor, componentConfiguration, componentRequirement, metadataProperties, outputDataStreamJobs);
    }

    public ImmutableFilterJob(String name, FilterDescriptor<?, ?> descriptor,
            ComponentConfiguration componentConfiguration, ComponentRequirement requirement,
            Map<String, String> metadataProperties) {
        super(name, descriptor, componentConfiguration, requirement, metadataProperties);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        ReadObjectBuilder.create(this, ImmutableFilterJob.class).readObject(stream);
    }

    @Override
    public FilterDescriptor<?, ?> getDescriptor() {
        return (FilterDescriptor<?, ?>) super.getDescriptor();
    }

    @Override
    public Collection<FilterOutcome> getFilterOutcomes() {
        final EnumSet<?> categories = getDescriptor().getOutcomeCategories();
        final List<FilterOutcome> outcomes = new ArrayList<>(categories.size());
        for (final Enum<?> category : categories) {
            outcomes.add(new ImmutableFilterOutcome(this, category));
        }
        return outcomes;
    }

    @Override
    public String toString() {
        return "ImmutableFilterJob[name=" + getName() + ",filter=" + getDescriptor().getDisplayName() + "]";
    }
}
