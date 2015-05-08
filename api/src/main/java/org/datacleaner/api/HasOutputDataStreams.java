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
package org.datacleaner.api;

import org.apache.metamodel.query.Query;

/**
 * Interface for components that produce data streams as an output of their
 * work.
 * 
 * Each output data stream has a {@link OutputDataStream} that describe the
 * metadata and structure of the output data stream.
 * 
 * For each output data stream that is relevant (consumed by one or more
 * components) the
 * {@link #initializeOutputDataStream(OutputDataStream, Query, OutputRowCollector)}
 * method is invoked at initialization time of this component.
 */
public interface HasOutputDataStreams {

    /**
     * Gets the {@link OutputDataStream}s that this component can produce.
     * 
     * @return
     */
    public OutputDataStream[] getOutputDataStreams();

    /**
     * Method invoked for each {@link OutputDataStream} that is consumed. The
     * method is invoked after validation time (see {@link Validate} ) and
     * before initialization time (see {@link Initialize}) of the component. The
     * method passes on an {@link OutputRowCollector} which makes it possible
     * for this component to post records into the output data stream.
     * 
     * If a particular {@link OutputDataStream} is NOT consumed by any following
     * components then this method will not be called.
     * 
     * @param outputDataStream
     * @param query
     *            the query posted towards the {@link OutputDataStream}. In most
     *            cases this will be a plain "SELECT * FROM table" query, but if
     *            {@link OutputDataStream#getPerformanceCharacteristics()}
     *            indicates that query optimization is possible, then the query
     *            may be adapted.
     * @param outputRowCollector
     *            an {@link OutputRowCollector} which the component should use
     *            to post records into the output stream.
     */
    public void initializeOutputDataStream(OutputDataStream outputDataStream, Query query,
            OutputRowCollector outputRowCollector);
}
