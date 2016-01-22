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
package org.datacleaner.job.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.datacleaner.api.Component;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.data.MutableInputColumn;
import org.datacleaner.data.TransformedInputColumn;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.RemoteTransformerDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.AnalysisJobImmutabilizer;
import org.datacleaner.job.ComponentRequirement;
import org.datacleaner.job.HasComponentRequirement;
import org.datacleaner.job.IdGenerator;
import org.datacleaner.job.ImmutableComponentConfiguration;
import org.datacleaner.job.ImmutableTransformerJob;
import org.datacleaner.job.InputColumnSinkJob;
import org.datacleaner.job.InputColumnSourceJob;
import org.datacleaner.job.OutputDataStreamJob;
import org.datacleaner.job.TransformerJob;
import org.datacleaner.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ComponentBuilder} for {@link Transformer}s
 * 
 * @param <T>
 *            the type of {@link Transformer} being built
 */
public final class TransformerComponentBuilder<T extends Transformer> extends
        AbstractComponentBuilder<TransformerDescriptor<T>, T, TransformerComponentBuilder<T>> implements
        InputColumnSourceJob,
        InputColumnSinkJob,
        HasComponentRequirement {

    private static final Logger logger = LoggerFactory.getLogger(TransformerComponentBuilder.class);
    private final String _id;
    private final List<MutableInputColumn<?>> _outputColumns = new ArrayList<>();
    private final List<String> _automaticOutputColumnNames = new ArrayList<>();
    private final IdGenerator _idGenerator;
    private final List<TransformerChangeListener> _localChangeListeners;

    public TransformerComponentBuilder(AnalysisJobBuilder analysisJobBuilder, TransformerDescriptor<T> descriptor,
            IdGenerator idGenerator) {
        super(analysisJobBuilder, descriptor, TransformerComponentBuilder.class);
        _id = "trans-" + idGenerator.nextId();
        _idGenerator = idGenerator;
        _localChangeListeners = new ArrayList<>(0);
    }

    /**
     * Gets the output column of this transformation with it's current
     * configuration.
     * 
     * @return transformer's output columns
     */
    public List<MutableInputColumn<?>> getOutputColumns() {
        final Component component = getComponentInstanceForQuestioning();
        if (component == null) {
            // as long as the transformer is not configured, just return an
            // empty list
            return Collections.emptyList();
        }

        final Transformer transformer = (Transformer) component;
        ComponentDescriptor<?> componentDescriptor = getDescriptor();

        if (componentDescriptor instanceof RemoteTransformerDescriptor) {
            boolean serverUp = ((RemoteTransformerDescriptor<?>) componentDescriptor).getRemoteDescriptorProvider()
                    .isServerUp();

            if (!serverUp) {
                logger.warn("Output columns for transformer '" + transformer
                        + "' can not be retrieved because the remote server is down. ");
                return Collections.emptyList();
            }
        }

        final OutputColumns outputColumns;
        try {
            outputColumns = transformer.getOutputColumns();
        } catch (Exception e) {
            logger.error("getOutputColumn() threw unexpected exception on transformer: " + transformer, e);
            return Collections.emptyList();
        }
        if (outputColumns == null) {
            logger.error("getOutputColumns() returned null on transformer: " + transformer);
            return Collections.emptyList();
        }
        boolean changed = false;

        // adjust the amount of output columns
        final int expectedCols = outputColumns.getColumnCount();
        final int existingCols = _outputColumns.size();
        if (expectedCols != existingCols) {
            changed = true;
            int colDiff = expectedCols - existingCols;
            if (colDiff > 0) {
                for (int i = 0; i < colDiff; i++) {
                    final int nextIndex = _outputColumns.size();
                    final String name = getColumnName(outputColumns, nextIndex);
                    final String id = _id + "-" + _idGenerator.nextId();
                    _outputColumns.add(new TransformedInputColumn<>(name, id));
                    _automaticOutputColumnNames.add(name);
                }
            } else if (colDiff < 0) {
                for (int i = 0; i < Math.abs(colDiff); i++) {
                    // remove from the tail
                    _outputColumns.remove(_outputColumns.size() - 1);
                    _automaticOutputColumnNames.remove(_automaticOutputColumnNames.size() - 1);
                }
            }

            // reset the names when the number of output columns change and the
            // initial name has changed
            for (int i = 0; i < expectedCols; i++) {
                final MutableInputColumn<?> column = _outputColumns.get(i);
                final String previousProposedName = column.getInitialName();
                final String newProposedName = outputColumns.getColumnName(i);
                if (newProposedName != null && !newProposedName.equals(previousProposedName)) {
                    column.setName(newProposedName);
                }
            }
        }

        // automatically update names and types of columns if they have not been
        // manually set
        for (int i = 0; i < expectedCols; i++) {
            final String proposedName = getColumnName(outputColumns, i);
            final Class<?> dataType = outputColumns.getColumnType(i);

            final TransformedInputColumn<?> col = (TransformedInputColumn<?>) _outputColumns.get(i);
            col.setInitialName(proposedName);
            if (dataType != col.getDataType()) {
                col.setDataType(dataType);
                changed = true;
            }

            final String automaticName = _automaticOutputColumnNames.get(i);
            final String columnName = col.getName();
            if (StringUtils.isNullOrEmpty(columnName) || automaticName.equals(columnName)) {
                if (proposedName != null) {
                    col.setName(proposedName);
                    _automaticOutputColumnNames.set(i, proposedName);
                }
            }
        }

        if (changed) {
            // notify listeners
            onOutputChanged();
        }

        return Collections.unmodifiableList(_outputColumns);
    }

    private String getColumnName(OutputColumns outputColumns, int index) {
        String name = outputColumns.getColumnName(index);
        if (name == null) {
            name = getDescriptor().getDisplayName() + " (" + (index + 1) + ")";
        }
        return name;
    }

    public void onOutputChanged() {
        // notify listeners
        List<TransformerChangeListener> listeners = getAllListeners();
        for (TransformerChangeListener listener : listeners) {
            listener.onOutputChanged(this, _outputColumns);
        }
    }

    public TransformerJob toTransformerJob() throws IllegalStateException {
        return toTransformerJob(true);
    }

    public TransformerJob toTransformerJob(final AnalysisJobImmutabilizer immutabilizer) throws IllegalStateException {
        return toTransformerJob(true, immutabilizer);
    }

    public TransformerJob toTransformerJob(final boolean validate) {
        return toTransformerJob(validate, new AnalysisJobImmutabilizer());
    }

    public TransformerJob toTransformerJob(final boolean validate, final AnalysisJobImmutabilizer immutabilizer) {
        if (validate && !isConfigured(true)) {
            throw new IllegalStateException("Transformer job is not correctly configured");
        }

        final ComponentRequirement componentRequirement = immutabilizer.load(getComponentRequirement());
        final OutputDataStreamJob[] outputDataStreamJobs = immutabilizer.load(getOutputDataStreamJobs(), validate);

        return new ImmutableTransformerJob(getName(), getDescriptor(), new ImmutableComponentConfiguration(
                getConfiguredProperties()), getOutputColumns(), componentRequirement, getMetadataProperties(),
                outputDataStreamJobs);
    }

    @Override
    public String toString() {
        return "TransformerComponentBuilder[transformer=" + getDescriptor().getDisplayName() + ",inputColumns="
                + getInputColumns() + "]";
    }

    /**
     * Builds a temporary list of all listeners, both global and local
     * 
     * @return a list of global and local listeners
     */
    private List<TransformerChangeListener> getAllListeners() {
        @SuppressWarnings("deprecation")
        List<TransformerChangeListener> globalChangeListeners = getAnalysisJobBuilder().getTransformerChangeListeners();

        List<TransformerChangeListener> list = new ArrayList<>(globalChangeListeners.size() + _localChangeListeners
                .size());
        list.addAll(globalChangeListeners);
        list.addAll(_localChangeListeners);
        return list;
    }

    /**
     * Gets an output column by name.
     * 
     * @see #getOutputColumns()
     * 
     * @param name
     *            name of the output column
     * @return output column
     */
    public MutableInputColumn<?> getOutputColumnByName(String name) {
        if (StringUtils.isNullOrEmpty(name)) {
            return null;
        }

        final List<MutableInputColumn<?>> outputColumns = getOutputColumns();
        for (MutableInputColumn<?> inputColumn : outputColumns) {
            if (name.equals(inputColumn.getName())) {
                return inputColumn;
            }
        }
        return null;
    }

    @Override
    public void onConfigurationChanged() {
        super.onConfigurationChanged();

        // trigger getOutputColumns which will notify consumers in the case of
        // output changes
        if (isConfigured()) {
            getOutputColumns();
        }

        List<TransformerChangeListener> listeners = getAllListeners();
        for (TransformerChangeListener listener : listeners) {
            listener.onConfigurationChanged(this);
        }
    }

    @Override
    public void onRequirementChanged() {
        super.onRequirementChanged();
        List<TransformerChangeListener> listeners = getAllListeners();
        for (TransformerChangeListener listener : listeners) {
            listener.onRequirementChanged(this);
        }
    }

    @Override
    public InputColumn<?>[] getInput() {
        return getInputColumns().toArray(new InputColumn<?>[0]);
    }

    @Override
    public MutableInputColumn<?>[] getOutput() {
        return getOutputColumns().toArray(new MutableInputColumn<?>[0]);
    }

    /**
     * Notification method invoked when transformer is removed.
     */
    @Override
    protected void onRemovedInternal() {
        List<TransformerChangeListener> listeners = getAllListeners();
        for (TransformerChangeListener listener : listeners) {
            listener.onOutputChanged(this, new LinkedList<MutableInputColumn<?>>());
            listener.onRemove(this);
        }
    }

    /**
     * Adds a change listener to this component
     * 
     * @param listener
     *            a new change listener
     */
    public void addChangeListener(TransformerChangeListener listener) {
        _localChangeListeners.add(listener);
    }

    /**
     * Removes a change listener from this component
     * 
     * @param listener
     *            the change listener to be removed
     * @return whether or not the listener was found and removed.
     */
    public boolean removeChangeListener(TransformerChangeListener listener) {
        return _localChangeListeners.remove(listener);
    }
}
