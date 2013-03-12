/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.user;

import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.util.VFSUtils;

public class UserPreferencesImplTest extends TestCase {

	public void testDeserialize21preferences() throws Exception {
		FileObject file = VFSUtils.getFileSystemManager().resolveFile("src/test/resources/userpreferences-2.1.dat");
		UserPreferences preferences = UserPreferencesImpl.load(file, false);
		assertNotNull(preferences);

		List<Datastore> datastores = preferences.getUserDatastores();
		assertEquals(2, datastores.size());

		Datastore datastore;
		datastore = datastores.get(0);
		assertEquals("JdbcDatastore[name=orderdb,url=jdbc:hsqldb:res:orderdb;readonly=true]", datastore.toString());
		assertEquals(null, datastore.getDescription());

		datastore = datastores.get(1);
		assertEquals(
				"CsvDatastore[name=foobar, filename=C:\\foobar.txt, quoteChar='\"', separatorChar=',', encoding=UTF-8, headerLineNumber=0]",
				datastore.toString());
		assertEquals("C:\\foobar.txt", ((CsvDatastore) datastore).getFilename());
		assertEquals(null, datastore.getDescription());

		List<Dictionary> dictionaries = preferences.getUserDictionaries();
		assertEquals(1, dictionaries.size());

		assertEquals("SimpleDictionary[name=my dictionary]", dictionaries.get(0).toString());
	}
}
