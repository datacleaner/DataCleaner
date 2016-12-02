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
package org.datacleaner.connection;

import java.util.List;

import org.apache.metamodel.json.JsonDataContext;
import org.apache.metamodel.schema.builder.SchemaBuilder;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.SerializableRef;

/**
 * Datastore implementation for JSON files
 */
public class JsonDatastore extends UsageAwareDatastore<JsonDataContext> implements ResourceDatastore {

    private static final long serialVersionUID = 1L;

    private final SerializableRef<Resource> _resourceRef;
    private final SerializableRef<SchemaBuilder> _schemaBuilderRef;

    public JsonDatastore(final String name, final Resource resource) {
        this(name, resource, null);
    }

    public JsonDatastore(final String name, final Resource resource, final SchemaBuilder schemaBuilder) {
        super(name);
        _resourceRef = new SerializableRef<>(resource);
        _schemaBuilderRef = new SerializableRef<>(schemaBuilder);
    }

    @Override
    public PerformanceCharacteristics getPerformanceCharacteristics() {
        return new PerformanceCharacteristicsImpl(false, true);
    }

    @Override
    protected UsageAwareDatastoreConnection<JsonDataContext> createDatastoreConnection() {
        final Resource resource = _resourceRef.get();
        final JsonDataContext dataContext;
        final SchemaBuilder schemaBuilder = _schemaBuilderRef.get();
        if (schemaBuilder == null) {
            dataContext = new JsonDataContext(resource);
        } else {
            dataContext = new JsonDataContext(resource, schemaBuilder);
        }
        return new DatastoreConnectionImpl<>(dataContext, this);
    }

    public SchemaBuilder getSchemaBuilder() {
        return _schemaBuilderRef.get();
    }

    @Override
    public Resource getResource() {
        return _resourceRef.get();
    }

    @Override
    protected void decorateIdentity(final List<Object> identifiers) {
        super.decorateIdentity(identifiers);
        identifiers.add(_resourceRef);
    }

}
