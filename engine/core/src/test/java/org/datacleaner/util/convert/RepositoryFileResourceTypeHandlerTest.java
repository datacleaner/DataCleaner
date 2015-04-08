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
package org.datacleaner.util.convert;

import org.datacleaner.repository.Repository;
import org.datacleaner.repository.RepositoryFile;
import org.datacleaner.repository.RepositoryFileResource;
import org.datacleaner.repository.RepositoryFolder;
import org.datacleaner.util.convert.RepositoryFileResourceTypeHandler;
import org.easymock.EasyMock;

import junit.framework.TestCase;

public class RepositoryFileResourceTypeHandlerTest extends TestCase {

    public void testConvertFromAndToString() throws Exception {
        RepositoryFile mockFile = EasyMock.createMock(RepositoryFile.class);
        RepositoryFolder mockFolder = EasyMock.createMock(RepositoryFolder.class);
        Repository mockRepo = EasyMock.createMock(Repository.class);
        
        EasyMock.expect(mockRepo.getFolder("ten1")).andReturn(mockFolder);
        EasyMock.expect(mockFolder.getQualifiedPath()).andReturn("/ten1").times(2);
        EasyMock.expect(mockFile.getQualifiedPath()).andReturn("/ten1/foo/bar.txt");
        EasyMock.expect(mockRepo.getRepositoryNode("/ten1/foo/bar.txt")).andReturn(mockFile);
        
        EasyMock.replay(mockFile, mockFolder, mockRepo);
        
        RepositoryFileResourceTypeHandler handler = new RepositoryFileResourceTypeHandler(mockRepo, "ten1");
        
        String path = handler.createPath(new RepositoryFileResource(mockFile));
        assertEquals("foo/bar.txt", path);
        
        RepositoryFileResource resource = handler.parsePath(path);
        RepositoryFile returnedFile = resource.getRepositoryFile();
        assertSame(mockFile, returnedFile);
        
        EasyMock.verify(mockFile, mockFolder, mockRepo);
    }
}
