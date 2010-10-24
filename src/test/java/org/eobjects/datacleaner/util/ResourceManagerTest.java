package org.eobjects.datacleaner.util;

import junit.framework.TestCase;

public class ResourceManagerTest extends TestCase {

	public void testGetUrl() throws Exception {
		assertNotNull(ResourceManager.getInstance().getUrl("images/menu/about.png"));
	}
}
