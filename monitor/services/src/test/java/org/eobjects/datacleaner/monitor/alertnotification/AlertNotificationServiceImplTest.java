package org.eobjects.datacleaner.monitor.alertnotification;

import junit.framework.TestCase;

public class AlertNotificationServiceImplTest extends TestCase {

    public void testIsBeyondThreshold() throws Exception {
        AlertNotificationServiceImpl service = new AlertNotificationServiceImpl();
        
        assertFalse(service.isBeyondThreshold(10, 5, 15));
        assertFalse(service.isBeyondThreshold(10, null, null));
        
        assertTrue(service.isBeyondThreshold(10, 11, 15));
        assertTrue(service.isBeyondThreshold(10, 5, 9));
        
        assertTrue(service.isBeyondThreshold(10, null, 9));
        assertTrue(service.isBeyondThreshold(10, 11, null));
        
        assertFalse(service.isBeyondThreshold(10, null, 11));
        assertFalse(service.isBeyondThreshold(10, 5, null));
    }
}
