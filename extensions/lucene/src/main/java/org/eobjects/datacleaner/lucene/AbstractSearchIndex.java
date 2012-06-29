/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.eobjects.analyzer.reference.AbstractReferenceData;
import org.eobjects.metamodel.util.Action;

/**
 * An abstract {@link SearchIndex} implementation.
 */
public abstract class AbstractSearchIndex extends AbstractReferenceData implements SearchIndex {

    public static final Version VERSION = Version.LUCENE_36;

    private static final long serialVersionUID = 1L;

    private final String[] _fieldNames;

    public AbstractSearchIndex(String name, String[] fieldNames) {
        super(name);
        _fieldNames = fieldNames;
    }

    @Override
    public String[] getFieldNames() {
        return _fieldNames;
    }

    protected abstract Directory getDirectory();

    protected final IndexReader getIndexReader() {
        try {
            final IndexReader reader = IndexReader.open(getDirectory());
            return reader;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read from directory", e);
        }
    }

    @Override
    public IndexSearcher getSearcher() {
        final IndexReader indexReader = getIndexReader();
        return new IndexSearcher(indexReader);
    }

    @Override
    public void write(Action<IndexWriter> writerAction) {
        try {
            Analyzer analyzer = new SimpleAnalyzer(VERSION);
            IndexWriterConfig conf = new IndexWriterConfig(VERSION, analyzer);
            Directory directory = getDirectory();
            IndexWriter writer = new IndexWriter(directory, conf);
            try {
                writerAction.run(writer);
                writer.commit();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new IllegalStateException("Write action threw exception", e);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to directory", e);
        }
    }
}
