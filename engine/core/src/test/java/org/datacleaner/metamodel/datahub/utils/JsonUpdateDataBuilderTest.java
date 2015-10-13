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
import java.util.List;

import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.datacleaner.metamodel.datahub.update.UpdateField;
import org.junit.Test;

public class JsonUpdateDataBuilderTest {

    @Test
    public void shouldConvertQueriesToJson() throws IOException {
        UpdateField[] fields =  { new UpdateField("testfield", "testvalue") };
        UpdateData data = new UpdateData("21", fields );
        List<UpdateData> updateData = new ArrayList<UpdateData>();
        updateData.add(data);
        String jsonString = JsonUpdateDataBuilder.buildJsonArray(updateData);
        assertThat(jsonString, is("[{\"grId\":\"21\",\"fields\":[{\"name\":\"testfield\",\"value\":\"testvalue\"}]}]"));
    }
    
    @Test
    public void shouldHandleNullValuesInUpdateData() throws IOException {
        UpdateField[] fields =  { new UpdateField("testfield", null) };
        UpdateData data = new UpdateData("21", fields );
        List<UpdateData> updateData = new ArrayList<UpdateData>();
        updateData.add(data);
        String jsonString = JsonUpdateDataBuilder.buildJsonArray(updateData);
        assertThat(jsonString, is("[{\"grId\":\"21\",\"fields\":[{\"name\":\"testfield\",\"value\":null}]}]"));
    }
}
