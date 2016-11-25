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
package org.datacleaner.metamodel.datahub;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.datacleaner.metamodel.datahub.update.UpdateData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataHubUpdateCallbackTest {

    private static final int MAX_BATCH_SIZE = DataHubUpdateCallback.INSERT_BATCH_SIZE;
    @Mock
    private DataHubDataContext dataContext;
    @InjectMocks
    private DataHubUpdateCallback callback;
    private UpdateData updateData;

    @Before
    public void init() {
        final Map<String, Object> fields = new HashMap<>();
        fields.put("field1", "value1");
        fields.put("field2", "value2");
        updateData = new UpdateData("20", fields);
    }

    @Test
    public void shouldFlushBatchWhenThresholdIsReached() {

        for (int x = 0; x < MAX_BATCH_SIZE + 1; x++) {
            callback.executeUpdate(updateData);
        }

        verify(dataContext, times(1)).executeUpdates(Matchers.anyListOf(UpdateData.class));
    }

    @Test
    public void shouldNotFlushBatchPrematurely() {
        for (int x = 0; x < MAX_BATCH_SIZE - 1; x++) {
            callback.executeUpdate(updateData);
        }

        verify(dataContext, never()).executeUpdates(Matchers.anyListOf(UpdateData.class));
    }

    @Test
    public void shouldFlushBatchInTWRContext() {
        try (DataHubUpdateCallback callback = new DataHubUpdateCallback(dataContext)) {
            for (int x = 0; x < MAX_BATCH_SIZE; x++) {
                callback.executeUpdate(updateData);
            }
            verify(dataContext, times(1)).executeUpdates(Matchers.anyListOf(UpdateData.class));
        }
        // Tests if it was called a second time
        verify(dataContext, times(1)).executeUpdates(Matchers.anyListOf(UpdateData.class));
    }
}
