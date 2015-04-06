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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.metamodel.util.Action;
import org.apache.metamodel.util.Func;

public class RepositoryFileResourceTest extends TestCase {

    public void testAppend() throws Exception {
        final RepositoryFile file = new RepositoryFile() {

            private static final long serialVersionUID = 1L;
            private byte[] contents = new byte[] { 1, 2, 3 };

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
            public void writeFile(Action<OutputStream> writeCallback) {
                writeFile(writeCallback, false);
            }

            @Override
            public void writeFile(Action<OutputStream> writeCallback, boolean append) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    if (append && contents != null) {
                        out.write(contents);
                    }
                    writeCallback.run(out);
                } catch (Exception e) {
                    throw new UnsupportedOperationException();
                }
                contents = out.toByteArray();
            }

            @Override
            public void readFile(Action<InputStream> readCallback) {
                try {
                    readCallback.run(new ByteArrayInputStream(contents));
                } catch (Exception e) {
                    throw new UnsupportedOperationException();
                }
            }

            @Override
            public <E> E readFile(Func<InputStream, E> readCallback) {
                return readCallback.eval(new ByteArrayInputStream(contents));
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
                return contents.length;
            }

            @Override
            public int compareTo(RepositoryNode o) {
                throw new UnsupportedOperationException();
            }
        };

        RepositoryFileResource resource = new RepositoryFileResource(file);
        resource.append(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                out.write(4);
                out.write(5);
                out.write(6);
            }
        });

        file.readFile(new Action<InputStream>() {
            @Override
            public void run(InputStream in) throws Exception {
                assertEquals(1, in.read());
                assertEquals(2, in.read());
                assertEquals(3, in.read());
                assertEquals(4, in.read());
                assertEquals(5, in.read());
                assertEquals(6, in.read());
                assertEquals(-1, in.read());
            }
        });
    }
}
