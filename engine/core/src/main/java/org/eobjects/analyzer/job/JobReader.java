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
package org.eobjects.analyzer.job;

import org.eobjects.analyzer.configuration.SourceColumnMapping;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.analyzer.descriptors.DescriptorProvider;

/**
 * An object that is capable of reading an AnalysisJob from a source.
 * 
 * 
 * 
 * @param <E>
 *            The source type, typically an InputStream, but could be another
 *            type of source as well.
 */
public interface JobReader<E> {

    /**
     * Reads a job from the source.
     * 
     * @param source
     * @return
     * @throws NoSuchDatastoreException
     *             if the datastore in the source is not found. Typically
     *             datastores are resolved using the {@link DatastoreCatalog}.
     * @throws NoSuchColumnException
     *             if a column defined in the source is not found
     * @throws NoSuchComponentException
     *             if a component defined in the source is not found. Typically
     *             components are resolved using the {@link DescriptorProvider}.
     * @throws ComponentConfigurationException
     *             if a component defined in the source is invalidly configured.
     * @throws IllegalStateException
     *             if the source has syntactical or formatting errors, or if the
     *             reading of the source was not possible.
     */
    public AnalysisJob read(E source) throws NoSuchDatastoreException, NoSuchColumnException, NoSuchComponentException,
            ComponentConfigurationException;

    /**
     * Reads a job from the source, while at the same time replacing source
     * column mappings. Using this method a job can be used as a "template" for
     * other jobs, since it can be loaded with a different source (and
     * variables).
     * 
     * @param source
     * @param sourceColumnMapping
     * @return
     * @throws NoSuchComponentException
     *             if a component defined in the source is not found. Typically
     *             components are resolved using the {@link DescriptorProvider}.
     * @throws ComponentConfigurationException
     *             if a component defined in the source is invalidly configured.
     * @throws IllegalStateException
     *             if the source has syntactical or formatting errors, or if the
     *             reading of the source was not possible.
     */
    public AnalysisJob read(E source, SourceColumnMapping sourceColumnMapping) throws NoSuchComponentException,
            ComponentConfigurationException, IllegalStateException;

    /**
     * Reads metadata about the job from the source. Typically this does not
     * involve materializing the whole job, and therefore is good for giving a
     * quick insight into the basic properties of the job.
     * 
     * @param source
     * @return
     * @throws IllegalStateException
     *             if the source has syntactical or formatting errors, or if the
     *             reading of the source was not possible.
     */
    public AnalysisJobMetadata readMetadata(E source) throws IllegalStateException;
}
