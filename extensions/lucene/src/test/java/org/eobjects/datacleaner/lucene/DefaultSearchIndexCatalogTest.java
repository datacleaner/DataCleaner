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

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

public class DefaultSearchIndexCatalogTest extends TestCase {

    public void testCreateAndUpdateCatalog() throws Exception {
        final Map<String, String> map = new LinkedHashMap<String, String>();
        final DefaultSearchIndexCatalog catalog1 = new DefaultSearchIndexCatalog(map);

        assertEquals("[]", Arrays.toString(catalog1.getSearchIndexNames()));

        File dir1 = new File("target/example_indices/search1");
        dir1.mkdirs();
        catalog1.addSearchIndex(new FileSystemSearchIndex("search1", dir1));

        File dir2 = new File("target/example_indices/find2");
        dir2.mkdirs();
        catalog1.addSearchIndex(new FileSystemSearchIndex("find2", dir1));

        assertEquals(
                "{datacleaner.lucene.index_count=2, "
                        + "datacleaner.lucene.0.name=find2, datacleaner.lucene.0.description=null, datacleaner.lucene.0.path=target"
                        + File.separatorChar
                        + "example_indices"
                        + File.separatorChar
                        + "search1, "
                        + "datacleaner.lucene.1.name=search1, datacleaner.lucene.1.description=null, datacleaner.lucene.1.path=target"
                        + File.separatorChar + "example_indices" + File.separatorChar + "search1}", map.toString());

        assertEquals("[find2, search1]", Arrays.toString(catalog1.getSearchIndexNames()));

        final DefaultSearchIndexCatalog catalog2 = new DefaultSearchIndexCatalog(map);

        assertEquals("[find2, search1]", Arrays.toString(catalog2.getSearchIndexNames()));
    }
}
