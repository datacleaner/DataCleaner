/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.eobjects.analyzer.reference.AbstractReferenceData;
import org.apache.metamodel.util.Action;

/**
 * An abstract {@link SearchIndex} implementation.
 */
public abstract class AbstractSearchIndex extends AbstractReferenceData implements SearchIndex {

    private static final long serialVersionUID = 1L;

    public AbstractSearchIndex(String name) {
        super(name);

        // hack to ensure that Lucene loads codec properly
        ClassLoader classLoader = getClass().getClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        Codec.reloadCodecs(getClass().getClassLoader());
    }

    protected abstract Directory getDirectory();

    @Override
    public List<String> getFieldNames() {
        try {
            final List<String> fieldNames = new ArrayList<String>();
            final DirectoryReader reader = getIndexReader();
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                if (doc != null) {
                    List<IndexableField> fields = doc.getFields();
                    if (fields != null && !fields.isEmpty()) {
                        for (IndexableField indexableField : fields) {
                            fieldNames.add(indexableField.name());
                        }
                        return fieldNames;
                    }
                }
            }
            return fieldNames;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected final DirectoryReader getIndexReader() {
        try {
            final DirectoryReader reader = DirectoryReader.open(getDirectory());
            return reader;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read from directory", e);
        }
    }

    @Override
    public IndexSearcher getSearcher() {
        final DirectoryReader reader = getIndexReader();
        return new IndexSearcher(reader);
    }

    @Override
    public void write(Action<IndexWriter> writerAction) {
        try {
            final Analyzer analyzer = new SimpleAnalyzer(Constants.VERSION);
            final IndexWriterConfig conf = new IndexWriterConfig(Constants.VERSION, analyzer);
            final Directory directory = getDirectory();
            final IndexWriter writer = new IndexWriter(directory, conf);
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
