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
package org.datacleaner.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.InMemoryResource;
import org.apache.metamodel.util.Resource;

import junit.framework.TestCase;

public class RepositoryFileResourceTest extends TestCase {

    public void testAppend() throws Exception {
        final RepositoryFile file = new RepositoryFile() {

            private static final long serialVersionUID = 1L;

            private final InMemoryResource _resource = new InMemoryResource("foo.dat", new byte[] { 1, 2, 3 }, -1);

            @Override
            public RepositoryFolder getParent() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getQualifiedPath() {
                return "foo.dat";
            }

            @Override
            public void delete() throws IllegalStateException {
                throw new UnsupportedOperationException();
            }

            @Override
            public InputStream readFile() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void writeFile(final Action<OutputStream> writeCallback) {
                writeFile(writeCallback, false);
            }

            @Override
            public void writeFile(final Action<OutputStream> writeCallback, final boolean append) {
                final OutputStream out = writeFile(append);
                try {
                    writeCallback.run(out);
                } catch (final Exception e) {
                    throw new UnsupportedOperationException();
                }
            }

            @Override
            public void readFile(final Action<InputStream> readCallback) {
                _resource.read(readCallback);
            }

            @Override
            public <E> E readFile(final Function<InputStream, E> readCallback) {
                return _resource.read(readCallback);
            }

            @Override
            public Type getType() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getLastModified() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getSize() {
                return _resource.getSize();
            }

            @Override
            public int compareTo(final RepositoryNode o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public OutputStream writeFile(final boolean append) {
                if (append) {
                    return _resource.append();
                }
                return _resource.write();
            }

            @Override
            public Resource toResource() {
                throw new UnsupportedOperationException();
            }
        };

        final RepositoryFileResource resource = new RepositoryFileResource(file);
        resource.append(out -> {
            out.write(4);
            out.write(5);
            out.write(6);
        });

        file.readFile(in -> {
            assertEquals(1, in.read());
            assertEquals(2, in.read());
            assertEquals(3, in.read());
            assertEquals(4, in.read());
            assertEquals(5, in.read());
            assertEquals(6, in.read());
            assertEquals(-1, in.read());
        });
    }
}
