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
package org.datacleaner.output.csv;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.datacleaner.api.InputColumn;
import org.datacleaner.output.OutputWriter;

public final class CsvOutputWriterFactory {

    private static final Map<String, AtomicInteger> counters = new HashMap<>();
    private static final Map<String, CsvOutputWriter> outputWritersPerPath = new HashMap<>();

    /**
     * Creates a CSV output writer with default configuration
     *
     * @param filename
     * @param columns
     * @return
     */
    public static OutputWriter getWriter(final String filename, final List<InputColumn<?>> columns) {
        final InputColumn<?>[] columnArray = columns.toArray(new InputColumn<?>[columns.size()]);
        final String[] headers = new String[columnArray.length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = columnArray[i].getName();
        }
        return getWriter(filename, headers, ',', '"', '\\', true, columnArray);
    }

    /**
     * Creates a CSV output writer
     *
     * @param filename
     * @param headers
     * @param separatorChar
     * @param quoteChar
     * @param escapeChar
     * @param includeHeader
     * @param columns
     * @return
     */
    public static OutputWriter getWriter(final String filename, final String[] headers, final char separatorChar,
            final char quoteChar, final char escapeChar, final boolean includeHeader, final InputColumn<?>... columns) {
        return getWriter(new FileResource(filename), headers, FileHelper.DEFAULT_ENCODING, separatorChar, quoteChar,
                escapeChar, includeHeader, columns);
    }

    public static OutputWriter getWriter(final Resource resource, final String[] headers, final String encoding,
            final char separatorChar, final char quoteChar, final char escapeChar, final boolean includeHeader,
            final InputColumn<?>... columns) {
        final CsvConfiguration csvConfiguration =
                getConfiguration(encoding, separatorChar, quoteChar, escapeChar, includeHeader);

        CsvOutputWriter outputWriter;
        final String qualifiedPath = resource.getQualifiedPath();
        synchronized (outputWritersPerPath) {
            outputWriter = outputWritersPerPath.get(qualifiedPath);
            if (outputWriter == null) {

                if (resource instanceof FileResource) {
                    final File file = ((FileResource) resource).getFile();
                    final File parentFile = file.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                }

                outputWritersPerPath.put(qualifiedPath, outputWriter);
                counters.put(qualifiedPath, new AtomicInteger(1));
                outputWriter = new CsvOutputWriter(resource, csvConfiguration, headers, columns);

                // write the headers
            } else {
                outputWriter = new CsvOutputWriter(resource, csvConfiguration, headers, columns);
                counters.get(qualifiedPath).incrementAndGet();
            }
        }

        return outputWriter;
    }

    private static CsvConfiguration getConfiguration(final String encoding, final char separatorChar,
            final char quoteChar, final char escapeChar, final boolean includeHeader) {
        final int headerLine;
        if (includeHeader) {
            headerLine = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
        } else {
            headerLine = CsvConfiguration.NO_COLUMN_NAME_LINE;
        }
        return new CsvConfiguration(headerLine, encoding, separatorChar, quoteChar, escapeChar);
    }

    protected static void release(final String filename) {
        final int count = counters.get(filename).decrementAndGet();
        if (count == 0) {
            synchronized (outputWritersPerPath) {
                outputWritersPerPath.remove(filename);
            }
        }
    }

}
