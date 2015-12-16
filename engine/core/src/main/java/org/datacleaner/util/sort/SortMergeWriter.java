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
package org.datacleaner.util.sort;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.datacleaner.util.ImmutableEntry;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sorter, deduplicator and writer that uses temporary files as storage to
 * support high volume sorted data.
 * 
 * @param <R>
 *            the row type, HAS to be serializable
 * @param <W>
 *            the writer type used when writing a row to the final destination
 *            file.
 */
public abstract class SortMergeWriter<R extends Serializable, W extends Closeable> {

    private static final Logger logger = LoggerFactory.getLogger(SortMergeWriter.class);

    /**
     * Size of the "records in memory" buffer
     */
    private final int _bufferSize;

    /**
     * Comparator for row sorting
     */
    private final Comparator<? super R> _comparator;

    /**
     * List of temporary files containing values
     */
    private final List<File> _tempFiles;

    /**
     * Buffer containing sorted rows in memory
     */
    private final Map<R, Integer> _buffer;
    private AtomicInteger _nullCount;

    public SortMergeWriter(Comparator<? super R> comparator) {
        this(50000, comparator);
    }

    public SortMergeWriter(int bufferSize, Comparator<? super R> comparator) {
        _bufferSize = bufferSize;
        _tempFiles = new ArrayList<File>();
        _buffer = new TreeMap<R, Integer>(comparator);
        _comparator = comparator;
        _nullCount = new AtomicInteger();
    }

    public void append(R line) {
        append(line, 1);
    }

    public void append(R line, int frequency) {
        if (line == null) {
            // special handling of null
            _nullCount.addAndGet(frequency);
        } else {
            synchronized (this) {
                Integer count = _buffer.get(line);
                if (count == null) {
                    if (_buffer.size() == _bufferSize) {
                        flushBuffer();
                    }
                    count = 0;
                }
                count += frequency;
                _buffer.put(line, count);
            }
        }
    }

    private void flushBuffer() {
        logger.debug("flushBuffer()");
        ObjectOutputStream oos = null;
        try {
            File file = createTempFile();
            logger.info("Writing {} rows to temporary file: {}", _bufferSize, file);

            oos = new ObjectOutputStream(new FileOutputStream(file));

            final List<Entry<R, Integer>> copyOfEntries;
            synchronized (this) {
                final Set<Entry<R, Integer>> entries = _buffer.entrySet();
                copyOfEntries = new ArrayList<>(entries);
                _buffer.clear();
                _tempFiles.add(file);
            }

            for (Entry<R, Integer> entry : copyOfEntries) {
                oos.writeObject(entry.getKey());
                oos.writeInt(entry.getValue());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(oos);
        }
    }

    protected File createTempFile() throws IOException {
        File file = File.createTempFile("sort_merge", ".dat");
        file.deleteOnExit();
        return file;
    }

    /**
     * Should null rows (if any) be written in the beginning or in the end of
     * the written file? Subclasses can overwrite this method to define that
     * behaviour.
     * 
     * @return
     */
    protected boolean writeNullsFirst() {
        return true;
    }

    protected abstract void writeHeader(W writer) throws IOException;

    protected abstract void writeRow(W writer, R row, int count) throws IOException;

    protected abstract W createWriter(Resource resource);

    protected void writeNull(W writer, int nullCount) throws IOException {
        writeRow(writer, null, nullCount);
    }

    public File write(String filename) {
        File file = new File(filename);
        write(file);
        return file;
    }

    /**
     * @param file
     * @return the written count of rows
     */
    public int write(final File file) {
        return write(new FileResource(file));
    }

    /**
     * 
     * @param resource
     * @return the written count of rows
     */
    public int write(Resource resource) {
        W writer = null;
        ObjectInputStream[] tempFileObjectInputStreams = null;
        try {
            writer = createWriter(resource);
            writeHeader(writer);

            int rowCount = 0;

            final boolean writeNullsFirst = writeNullsFirst();

            final int nullCount = _nullCount.get();
            if (nullCount > 0 && writeNullsFirst) {
                writeNull(writer, nullCount);
                rowCount++;
            }

            if (_tempFiles.isEmpty()) {
                logger.info("No temp files created yet, flushing buffer directly to target: {}", resource);
                Set<Entry<R, Integer>> entries = _buffer.entrySet();
                for (Entry<R, Integer> entry : entries) {
                    writeRow(writer, entry.getKey(), entry.getValue());
                    rowCount++;
                }
                _buffer.clear();

                if (nullCount > 0 && !writeNullsFirst) {
                    writeNull(writer, nullCount);
                    rowCount++;
                }

                return rowCount;
            }

            if (!_buffer.isEmpty()) {
                flushBuffer();
            }

            tempFileObjectInputStreams = createTempFileObjectInputStreams();

            final List<Entry<R, Integer>> rowCandidates = new ArrayList<Entry<R, Integer>>(_tempFiles.size());
            for (int i = 0; i < _tempFiles.size(); i++) {
                rowCandidates.add(null);
            }

            while (true) {
                readNextRows(rowCandidates, tempFileObjectInputStreams);

                Entry<R, Integer> currentRow = null;

                // find the next row to write
                for (Entry<R, Integer> rowCandidate : rowCandidates) {
                    if (rowCandidate != null) {
                        if (currentRow == null) {
                            currentRow = rowCandidate;
                        } else {
                            if (_comparator.compare(rowCandidate.getKey(), currentRow.getKey()) < 0) {
                                currentRow = rowCandidate;
                            }
                        }
                    }
                }

                if (currentRow == null) {
                    // the writing is done!
                    break;
                }

                // set count to 0 (the next loop will increment it)
                currentRow = new ImmutableEntry<R, Integer>(currentRow.getKey(), 0);

                for (int i = 0; i < rowCandidates.size(); i++) {
                    Entry<R, Integer> rowCandidate = rowCandidates.get(i);
                    if (rowCandidate != null) {
                        if (_comparator.compare(rowCandidate.getKey(), currentRow.getKey()) == 0) {
                            // sum up a new count
                            final int newCount = currentRow.getValue().intValue() + rowCandidate.getValue().intValue();
                            currentRow = new ImmutableEntry<R, Integer>(currentRow.getKey(), newCount);
                            rowCandidates.set(i, null);
                        }
                    }
                }

                writeRow(writer, currentRow.getKey(), currentRow.getValue());
                rowCount++;
            }

            if (nullCount > 0 && !writeNullsFirst) {
                writeNull(writer, nullCount);
                rowCount++;
            }

            return rowCount;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            FileHelper.safeClose(writer);
            if (tempFileObjectInputStreams != null) {
                for (int i = 0; i < tempFileObjectInputStreams.length; i++) {
                    FileHelper.safeClose(tempFileObjectInputStreams[i]);
                }
            }
        }
    }

    private void readNextRows(List<Entry<R, Integer>> nextRows, ObjectInputStream[] tempFileObjectInputStreams)
            throws Exception {
        for (int i = 0; i < tempFileObjectInputStreams.length; i++) {
            if (tempFileObjectInputStreams[i] != null) {
                if (nextRows.get(i) == null) {

                    try {
                        @SuppressWarnings("unchecked")
                        final R row = (R) tempFileObjectInputStreams[i].readObject();
                        final int count = tempFileObjectInputStreams[i].readInt();

                        final Entry<R, Integer> entry = new ImmutableEntry<R, Integer>(row, count);
                        nextRows.set(i, entry);
                    } catch (EOFException e) {
                        FileHelper.safeClose(tempFileObjectInputStreams[i]);
                        tempFileObjectInputStreams[i] = null;
                    }
                }
            }
        }
    }

    @SuppressWarnings("resource")
    private ObjectInputStream[] createTempFileObjectInputStreams() throws IOException {
        final ObjectInputStream[] tempFileObjectInputStreams = new ObjectInputStream[_tempFiles.size()];
        for (int i = 0; i < tempFileObjectInputStreams.length; i++) {
            final File tempFile = _tempFiles.get(i);
            final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(tempFile));
            tempFileObjectInputStreams[i] = ois;
        }
        return tempFileObjectInputStreams;
    }
}
