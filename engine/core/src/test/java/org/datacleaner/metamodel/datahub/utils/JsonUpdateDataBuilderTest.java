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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacleaner.metamodel.datahub.update.SourceRecordIdentifier;
import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.junit.Test;

public class JsonUpdateDataBuilderTest {

    @Test
    public void shouldConvertQueriesToJson() throws IOException {
        final Map<String, Object> fields = new HashMap<>();
        fields.put("testfield", "testvalue");
        final UpdateData data = new UpdateData("21", fields);
        final List<UpdateData> updateData = new ArrayList<>();
        updateData.add(data);
        final String jsonString = JsonUpdateDataBuilder.buildJsonArray(updateData);
        assertThat(jsonString, is("[{\"grId\":\"21\",\"fields\":{\"testfield\":\"testvalue\"}}]"));
    }

    @Test
    public void shouldHandleNullValuesInUpdateData() throws IOException {
        final Map<String, Object> fields = new HashMap<>();
        fields.put("testfield", null);
        final UpdateData data = new UpdateData("21", fields);
        final List<UpdateData> updateData = new ArrayList<>();
        updateData.add(data);
        final String jsonString = JsonUpdateDataBuilder.buildJsonArray(updateData);
        assertThat(jsonString, is("[{\"grId\":\"21\",\"fields\":{\"testfield\":null}}]"));
    }

    @Test
    public void shouldConvertSourceRecordByDescriptionIdentifierToJSON() {
        final Map<String, Object> fields = new HashMap<>();
        fields.put("testfield", "testvalue");
        final SourceRecordIdentifier data =
                new SourceRecordIdentifier("mySource", "myId", "myTypeKey", "myTypeDescription");
        final List<SourceRecordIdentifier> updateData = new ArrayList<>();
        updateData.add(data);
        final String jsonString = JsonUpdateDataBuilder.buildJsonArray(updateData);
        assertThat(jsonString,
                is("[{\"recordTypeKey\":\"myTypeKey\",\"sourceName\":\"mySource\",\"sourceRecordId\":\"myId\","
                        + "\"recordTypeDescription\":\"myTypeDescription\"}]"));

    }
}
