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

import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.eobjects.metamodel.util.Action;

final class WriteSearchIndexAction implements Action<Iterable<Object[]>> {

    private final SearchIndex _searchIndex;
    private final String[] _fields;

    public WriteSearchIndexAction(SearchIndex searchIndex, String[] fields) {
        _searchIndex = searchIndex;
        _fields = fields;
    }

    @Override
    public void run(final Iterable<Object[]> iterable) throws Exception {
        _searchIndex.write(new Action<IndexWriter>() {
            @Override
            public void run(final IndexWriter writer) throws Exception {
                final Iterator<Object[]> iterator = iterable.iterator();
                while (iterator.hasNext()) {
                    Object[] rowData = iterator.next();

                    final StringBuilder searchText = new StringBuilder();

                    Document doc = new Document();
                    for (int i = 0; i < rowData.length; i++) {
                        final Object value = rowData[i];
                        if (value != null) {
                            final String field = _fields[i];
                            final String stringValue = value.toString().trim();

                            if (searchText.length() != 0) {
                                searchText.append(' ');
                            }
                            searchText.append(stringValue);
                            
                            doc.add(new Field(field, stringValue, Field.Store.YES, Field.Index.NOT_ANALYZED));
                        }
                    }
                    
                    doc.add(new Field(Constants.SEARCH_FIELD_NAME, searchText.toString(), Field.Store.NO, Field.Index.ANALYZED));
                    
                    writer.addDocument(doc);
                }
            }
        });
    }

}