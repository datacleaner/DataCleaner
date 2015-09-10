package org.datacleaner.metamodel.datahub;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.schema.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataHubUpdateCallbackTest {

    @Mock
    private DataHubDataContext dataContext;

    @InjectMocks
    private DataHubUpdateCallback callback;

    private Table table;

    private static int maxBatchSize = DataHubUpdateCallback.INSERT_BATCH_SIZE;
    private static String query = "query";

    @Before
    public void init() {
        table = new MutableTable("table");
    }
    
    @Test
    public void shouldFlushBatchWhenThresholdIsReached() {

        for (int x = 0; x < maxBatchSize + 1; x++) {
            callback.executeUpdate(table, query);
        }
        
        verify(dataContext, times(1)).executeUpdate(any(PendingUpdates.class));
    }
    
    @Test
    public void shouldNotFlushBatchPrematurely() {
        for (int x = 0; x < maxBatchSize - 1; x++) {
            callback.executeUpdate(table, query);
        }
        
        verify(dataContext, never()).executeUpdate(any(PendingUpdates.class));
    }
    
    @Test
    public void shouldFlushBatchInTWRContext() {
        try(DataHubUpdateCallback callback = new DataHubUpdateCallback(dataContext)) {
            for (int x = 0; x < maxBatchSize; x++) {
                callback.executeUpdate(table, query);
            }
            verify(dataContext, times(1)).executeUpdate(any(PendingUpdates.class));
        }
        verify(dataContext, times(1)).executeUpdate(any(PendingUpdates.class)); //Tests if it was called a second time
    }
}
