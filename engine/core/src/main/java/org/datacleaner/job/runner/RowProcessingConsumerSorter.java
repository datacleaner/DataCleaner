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
package org.datacleaner.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.datacleaner.api.ExpressionBasedInputColumn;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.MultiStreamComponent;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.FilterOutcome;
import org.datacleaner.job.FilterOutcomes;
import org.datacleaner.job.HasFilterOutcomes;
import org.datacleaner.job.InputColumnSourceJob;

/**
 * Helping class for the row processing publisher, that will help sort the
 * consumers correctly
 */
class RowProcessingConsumerSorter {

    private final Collection<? extends RowProcessingConsumer> _consumers;

    public RowProcessingConsumerSorter(final Collection<? extends RowProcessingConsumer> consumers) {
        _consumers = consumers;
    }

    public List<RowProcessingConsumer> createProcessOrderedConsumerList() {
        final List<RowProcessingConsumer> orderedConsumers = new ArrayList<>();
        final Collection<RowProcessingConsumer> remainingConsumers = new LinkedList<>(_consumers);
        final Set<InputColumn<?>> availableVirtualColumns = new HashSet<>();
        final FilterOutcomes availableOutcomes = new FilterOutcomesImpl();

        while (!remainingConsumers.isEmpty()) {
            boolean changed = false;
            for (final Iterator<RowProcessingConsumer> it = remainingConsumers.iterator(); it.hasNext(); ) {
                final RowProcessingConsumer consumer = it.next();

                boolean accepted = true;

                // make sure that any dependent filter outcome is evaluated
                // before this component
                accepted = consumer.satisfiedForFlowOrdering(availableOutcomes);

                // make sure that all the required colums are present
                if (accepted) {
                    final InputColumn<?>[] requiredInput = consumer.getRequiredInput();
                    if (requiredInput != null) {
                        for (final InputColumn<?> inputColumn : requiredInput) {
                            if (!inputColumn.isPhysicalColumn()) {
                                if (!(inputColumn instanceof ExpressionBasedInputColumn)) {
                                    if (!availableVirtualColumns.contains(inputColumn)) {
                                        accepted = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (accepted) {
                    orderedConsumers.add(consumer);
                    it.remove();
                    changed = true;

                    final ComponentJob componentJob = consumer.getComponentJob();

                    final InputColumn<?>[] requiredInput = consumer.getRequiredInput();
                    for (final InputColumn<?> inputColumn : requiredInput) {
                        if (inputColumn instanceof ExpressionBasedInputColumn) {
                            availableVirtualColumns.add(inputColumn);
                        }
                    }

                    if (componentJob instanceof InputColumnSourceJob) {
                        final InputColumn<?>[] output = ((InputColumnSourceJob) componentJob).getOutput();
                        for (final InputColumn<?> col : output) {
                            availableVirtualColumns.add(col);
                        }
                    }

                    if (componentJob instanceof HasFilterOutcomes) {
                        final Collection<FilterOutcome> outcomes =
                                ((HasFilterOutcomes) componentJob).getFilterOutcomes();
                        for (final FilterOutcome outcome : outcomes) {
                            availableOutcomes.add(outcome);
                        }
                    }
                }
            }

            if (!changed) {
                // handle special case where a multistream component has a
                // requirement from another stream
                for (final Iterator<RowProcessingConsumer> it = remainingConsumers.iterator(); it.hasNext(); ) {
                    final RowProcessingConsumer consumer = it.next();
                    if (consumer.getComponent() instanceof MultiStreamComponent) {
                        orderedConsumers.add(consumer);
                        it.remove();
                        changed = true;
                    }
                }
            }

            if (!changed) {
                // should never happen, but if a bug enters the
                // algorithm this exception will quickly expose it
                throw new IllegalStateException("Could not detect next consumer in processing order");
            }
        }
        return orderedConsumers;
    }

}
