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
package org.datacleaner.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.csv.CsvDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.InMemoryResource;
import org.apache.metamodel.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Provides an auto-detection mechanism for the properties of a
 * {@link CsvConfiguration}.
 */
public class CsvConfigurationDetection {

    private static final Logger logger = LoggerFactory.getLogger(CsvConfigurationDetection.class);

    /**
     * Amount of bytes to read for autodetection of encoding, separator and
     * quotes
     */
    private static final int SAMPLE_BUFFER_SIZE = 128 * 1024;

    private final Resource _resource;

    public CsvConfigurationDetection(File file) {
        _resource = new FileResource(file);
    }

    public CsvConfigurationDetection(Resource resource) {
        _resource = resource;
    }

    protected byte[] getSampleBuffer() {
        byte[] bytes = new byte[SAMPLE_BUFFER_SIZE];
        InputStream inputStream = _resource.read();
        try {
            int bufferSize = inputStream.read(bytes, 0, SAMPLE_BUFFER_SIZE);
            if (bufferSize != -1 && bufferSize != SAMPLE_BUFFER_SIZE) {
                bytes = Arrays.copyOf(bytes, bufferSize);
            }
            return bytes;
        } catch (IOException e) {
            logger.error("IOException occurred while reading sample buffer", e);
            return new byte[0];
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    /**
     * Auto-detects the file encoding of a file
     * 
     * @return
     */
    public String suggestEncoding() {
        return suggestEncoding(getSampleBuffer());
    }

    protected String suggestEncoding(byte[] bytes) {
        final CharsetDetector cd = new CharsetDetector();
        cd.setText(bytes);

        final CharsetMatch charsetMatch = cd.detect();
        final String charSet = charsetMatch.getName();

        int confidence = charsetMatch.getConfidence();
        logger.info("CharsetMatch: {} ({}% confidence)", charSet, confidence);
        return charSet;
    }

    /**
     * Auto-detect the {@link CsvConfiguration} of a CSV style data file,
     * providing the encoding externally.
     * 
     * @param encoding
     * @return
     * @throws IllegalStateException
     *             if an error occurs during auto-detection
     */
    public CsvConfiguration suggestCsvConfiguration(String encoding) throws IllegalStateException {
        final byte[] sample = getSampleBuffer();
        return suggestCsvConfiguration(sample, encoding);
    }

    /**
     * Auto-detects the {@link CsvConfiguration} of a CSV style data file.
     * 
     * @return
     * @throws IllegalStateException
     *             if an error occurs during auto-detection
     */
    public CsvConfiguration suggestCsvConfiguration() throws IllegalStateException {
        final byte[] sample = getSampleBuffer();
        final String encoding = suggestEncoding(sample);

        return suggestCsvConfiguration(sample, encoding);
    }

    private CsvConfiguration suggestCsvConfiguration(byte[] sample, String encoding) throws IllegalStateException {

        char[] sampleChars = readSampleBuffer(sample, encoding);

        if (indexOf('\n', sampleChars) == -1 && indexOf('\r', sampleChars) == -1) {
            throw new IllegalStateException("No newline in first " + sampleChars.length + " chars");
        }

        int newlines = 0;
        int tabs = 0;
        int commas = 0;
        int semicolons = 0;
        int pipes = 0;
        int singleQuotes = 0;
        int doubleQuotes = 0;
        int backslashes = 0;

        for (int i = 0; i < sampleChars.length; i++) {
            char c = sampleChars[i];
            if (c == '\n') {
                newlines++;
            } else if (c == '\t') {
                tabs++;
            } else if (c == ',') {
                commas++;
            } else if (c == ';') {
                semicolons++;
            } else if (c == '\'') {
                singleQuotes++;
            } else if (c == '|') {
                pipes++;
            } else if (c == '"') {
                doubleQuotes++;
            } else if (c == '\\') {
                backslashes++;
            }
        }

        char separatorChar;
        char quoteChar;
        char escapeChar;

        int detectedSeparator = Math.max(tabs, Math.max(commas, Math.max(semicolons, pipes)));
        if (detectedSeparator == 0 || detectedSeparator < newlines) {
            separatorChar = ',';
        } else {
            // set the separator
            if (detectedSeparator == commas) {
                separatorChar = ',';
            } else if (detectedSeparator == semicolons) {
                separatorChar = ';';
            } else if (detectedSeparator == tabs) {
                separatorChar = '\t';
            } else if (detectedSeparator == pipes) {
                separatorChar = '|';
            } else {
                separatorChar = ',';
            }
        }

        if (backslashes > 0) {
            escapeChar = '\\';
        } else {
            escapeChar = CsvConfiguration.NOT_A_CHAR;
        }

        int detectedQuote = Math.max(singleQuotes, doubleQuotes);
        if (detectedQuote == 0 || detectedQuote < newlines) {
            quoteChar = '"';
        } else {
            // set the quote
            if (detectedQuote == singleQuotes) {
                quoteChar = '\'';
            } else if (detectedQuote == doubleQuotes) {
                quoteChar = '"';
            } else {
                quoteChar = '"';
            }
        }

        // detect if multi line values occur
        boolean multiline = false;
        final CsvConfiguration multiLineConfiguration = new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE,
                encoding, separatorChar, quoteChar, escapeChar, false, true);
        try {
            final CsvDataContext testDataContext = new CsvDataContext(new InMemoryResource("foo.txt", sample,
                    System.currentTimeMillis()), multiLineConfiguration);
            final Table table = testDataContext.getDefaultSchema().getTable(0);
            try (final DataSet dataSet = testDataContext.query().from(table).select(table.getColumns()).execute()) {
                while (dataSet.next()) {
                    final Row row = dataSet.getRow();
                    final Object[] values = row.getValues();
                    for (Object value : values) {
                        if (value != null && value instanceof String) {
                            if (((String) value).indexOf('\n') != -1) {
                                // found a multi line value
                                multiline = true;
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to detect multiline property of CsvConfiguration, defaulting to 'true'", e);
            return multiLineConfiguration;
        }

        return new CsvConfiguration(CsvConfiguration.DEFAULT_COLUMN_NAME_LINE, encoding, separatorChar, quoteChar,
                escapeChar, false, multiline);
    }

    private int indexOf(char c, char[] sampleChars) {
        for (int i = 0; i < sampleChars.length; i++) {
            if (c == sampleChars[i]) {
                return i;
            }
        }
        return -1;
    }

    protected char[] readSampleBuffer(byte[] bytes, final String charSet) throws IllegalStateException {
        char[] buffer = new char[bytes.length];
        Reader reader = null;
        try {
            reader = new InputStreamReader(new ByteArrayInputStream(bytes), charSet);

            // read a sample of the file to auto-detect quotes and separators
            int bufferSize = reader.read(buffer);
            if (bufferSize != -1) {
                buffer = Arrays.copyOf(buffer, bufferSize);
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Error reading from file: " + e.getMessage(), e);
            }
            throw new IllegalStateException("Error reading from file: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    logger.debug("Could not close reader", ioe);
                }
            }
        }
        return buffer;
    }
}
