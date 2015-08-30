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
package org.datacleaner.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.datacleaner.api.Configured;
import org.datacleaner.connection.Datastore;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.DictionaryConnection;
import org.junit.Ignore;

@Ignore
public class SampleCustomDictionary implements Dictionary {

    private static final long serialVersionUID = 1L;

    @Configured
    String name;

    @Configured
    int values;

    @Configured
    Datastore datastore;

    @Configured
    String description;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public DictionaryConnection openConnection(DataCleanerConfiguration arg0) {
        final List<String> valueList = new ArrayList<String>();
        for (int i = 0; i < SampleCustomDictionary.this.values; i++) {
            valueList.add("value" + i);
        }
        return new DictionaryConnection() {

            @Override
            public Iterator<String> getAllValues() {
                return valueList.iterator();
            }

            @Override
            public boolean containsValue(String str) {
                return valueList.contains(str);
            }

            @Override
            public void close() {
            }
        };
    }
}
