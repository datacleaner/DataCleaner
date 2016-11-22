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
package org.datacleaner.metamodel.datahub.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUpdateDataBuilder {

    public static <T> String buildJsonArray(final T objectData) {

        final OutputStream os = new ByteArrayOutputStream();
        final ObjectMapper mapper = new ObjectMapper();
        final String json;
        try {
            mapper.writeValue(os, objectData);
            final byte[] data = ((ByteArrayOutputStream) os).toByteArray();
            json = new String(data, "UTF-8");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return json;

    }

}
