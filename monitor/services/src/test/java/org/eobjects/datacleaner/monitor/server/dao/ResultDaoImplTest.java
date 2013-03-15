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
package org.eobjects.datacleaner.monitor.server.dao;

import java.io.File;
import java.util.List;

import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.datacleaner.repository.file.FileRepositoryFolder;

import junit.framework.TestCase;

public class ResultDaoImplTest extends TestCase {
    
    public void testReturnOnlyJobSpecificResult() throws Exception {
        File file = new File("src/test/resources/example_dubious_result_files");
        FileRepositoryFolder folder = new FileRepositoryFolder(null, file);
        List<RepositoryFile> results;
        
        results = ResultDaoImpl.getResultsForJob("bar",folder);
        assertTrue(results.isEmpty());
        
        results = ResultDaoImpl.getResultsForJob("foo-job",folder);
        assertEquals(1, results.size());
        
        results = ResultDaoImpl.getResultsForJob("foo2",folder);
        assertEquals(1, results.size());
        
        results = ResultDaoImpl.getResultsForJob("foo",folder);
        assertEquals(2, results.size());
    }

}
