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
package org.datacleaner.util.sort;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;

import junit.framework.TestCase;

import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.Resource;
import org.apache.metamodel.util.ToStringComparator;

public class SortMergeWriterTest extends TestCase {

    public void testSimpleSort() throws Exception {
        doSortTests(2);
        doSortTests(5);
        doSortTests(10);
        doSortTests(10000);
    }

    public void testSimpleDedup() throws Exception {
        doDedupTests(2);
        doDedupTests(5);
        doDedupTests(10);
        doDedupTests(10000);
    }

    // test that the comparator is being used. Here we dedup string arrays but
    // only based on the string at index 0.
    public void testDedupArray() throws Exception {
        Comparator<String[]> comparator = new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[0]);
            }
        };

        SortMergeWriter<String[], Writer> sorter = new SortMergeWriter<String[], Writer>(2, comparator) {

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }

            @Override
            protected void writeRow(Writer writer, String[] row, int count) throws IOException {
                if (row == null) {
                    writer.write("<null>," + count + "\n");
                } else {
                    writer.write(row[0] + "," + count + "\n");
                }
            }

            protected void writeHeader(Writer writer) throws IOException {
                writer.write("text,count\n");
            }

            @Override
            protected Writer createWriter(Resource file) {
                return null;
            };
        };

        sorter.append(new String[] { "foo", "foo" });
        sorter.append(new String[] { "bar", "foobar" });
        sorter.append(new String[] { "foobar", "bar" });
        sorter.append(new String[] { "barfoo", "foobar" });
        sorter.append(new String[] { "foo", "foo" });
        sorter.append(new String[] { "foobar", "bar" });
        sorter.append(new String[] { "barfoo", "foobar" });
        sorter.append(new String[] { "bar", "foo" });
        sorter.append(new String[] { "foobar", "bar" });
        sorter.append(new String[] { "barfoo", "foobar" });
        sorter.append(new String[] { "bar", "foo" });
        sorter.append(new String[] { "foobar", "bar" });

        File file = sorter.write("target/sort_merge_arrays-deduped.csv");
        assertTrue(file.exists());

        try (BufferedReader br = FileHelper.getBufferedReader(file);) {
            assertEquals("text,count", br.readLine());
            assertEquals("bar,3", br.readLine());
            assertEquals("barfoo,3", br.readLine());
            assertEquals("foo,2", br.readLine());
            assertEquals("foobar,4", br.readLine());
            assertNull(br.readLine());

            br.close();
        }
    }

    public void testUseAsUniquenessChecker() throws Exception {
        SortMergeWriter<String, Writer> sorter = new SortMergeWriter<String, Writer>(2,
                ToStringComparator.getComparator()) {

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }

            @Override
            protected void writeRow(Writer writer, String row, int count) throws IOException {
                if (count > 1) {
                    writer.write(row + "," + count + "\n");
                }
            }

            protected void writeHeader(Writer writer) throws IOException {
                writer.write("text,count\n");
            };

            @Override
            protected void writeNull(Writer writer, int nullCount) throws IOException {
                if (nullCount > 1) {
                    writeRow(writer, "<null>", nullCount);
                }
            }

            @Override
            protected Writer createWriter(Resource file) {
                //Do nothing
                return null;
            }
        };

        sorter.append("foo");
        sorter.append("bar");
        sorter.append("baz");
        sorter.append("hello");
        sorter.append("world");
        for (int i = 0; i < 100; i++) {
            sorter.append("unique" + i);
        }
        sorter.append("bar");
        sorter.append("foo");

        File file = sorter.write("target/sort_merge_uniqueness.txt");

        String str = FileHelper.readFileAsString(file);
        assertEquals("text,count\n" + "bar,2\n" + "foo,2", str);
    }

    public void testNullSafety() throws Exception {
        SortMergeWriter<String, Writer> sorter = new SortMergeWriter<String, Writer>(2,
                ToStringComparator.getComparator()) {

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }

            @Override
            protected void writeRow(Writer writer, String row, int count) throws IOException {
                writer.write(row + "," + count + "\n");
            }

            protected void writeHeader(Writer writer) throws IOException {
                writer.write("text,count\n");
            };

            @Override
            protected void writeNull(Writer writer, int nullCount) throws IOException {
                writeRow(writer, "<null>", nullCount);
            }

            @Override
            protected Writer createWriter(Resource file) {
                return null;
            }
        };

        sorter.append("1234");
        sorter.append("acb");
        sorter.append(null);
        sorter.append("5678");
        sorter.append("1234");
        sorter.append("acb", 3);
        sorter.append("acb");
        sorter.append("5678");
        sorter.append("1234");

        File file = sorter.write("target/sort_merge_null_safety.txt");
        assertTrue(file.exists());

        BufferedReader br = FileHelper.getBufferedReader(file);

        assertEquals("text,count", br.readLine());
        assertEquals("<null>,1", br.readLine());
        assertEquals("1234,3", br.readLine());
        assertEquals("5678,2", br.readLine());
        assertEquals("acb,5", br.readLine());
        assertNull(br.readLine());
    }

    public void testNoUnnescesaryTempFiles() throws Exception {
        SortMergeWriter<String, Writer> sorter = new SortMergeWriter<String, Writer>(10,
                ToStringComparator.getComparator()) {

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }

            @Override
            protected void writeRow(Writer writer, String row, int count) throws IOException {
                writer.write(row + "," + count + "\n");
            }

            protected void writeHeader(Writer writer) throws IOException {
                writer.write("text,count\n");
            };

            @Override
            protected File createTempFile() throws IOException {
                throw new IllegalStateException("This test is not supposed to require temp files!");
            }

            @Override
            protected Writer createWriter(Resource file) {
                //Do nothing
                return null;
            }
        };

        sorter.append("1234");
        sorter.append("acb");
        sorter.append("abc");
        sorter.append("acb");
        sorter.append("5678");

        File file = sorter.write("target/sort_merge_no_temp_file.txt");
        assertTrue(file.exists());

        try (BufferedReader br = FileHelper.getBufferedReader(file)) {
            assertEquals("text,count", br.readLine());
            assertEquals("1234,1", br.readLine());
            assertEquals("5678,1", br.readLine());
            assertEquals("abc,1", br.readLine());
            assertEquals("acb,2", br.readLine());
            assertNull(br.readLine());
        }
    }

    private void doSortTests(int threshold) throws Exception {
        SortMergeWriter<String, Writer> sorter = new SortMergeWriter<String, Writer>(threshold,
                ToStringComparator.getComparator()) {

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }

            @Override
            protected void writeRow(Writer writer, String row, int count) throws IOException {
                writer.write(row + "," + count + "\n");
            }

            protected void writeHeader(Writer writer) throws IOException {
                writer.write("number,count\n");
            }

            @Override
            protected Writer createWriter(Resource file) {
                // Do nothing
                return null;
            };
        };

        sorter.append("02");
        sorter.append("01");
        sorter.append("04");
        sorter.append("03");
        sorter.append("06");
        sorter.append("07");
        sorter.append("08");
        sorter.append("05");
        sorter.append("09");
        sorter.append("10");
        sorter.append("13");
        sorter.append("12");
        sorter.append("11");
        sorter.append("14");
        File file = sorter.write("target/sort_merge_sort_" + threshold + ".txt");

        assertTrue(file.exists());

        try (BufferedReader br = FileHelper.getBufferedReader(file)) {
            assertEquals("number,count", br.readLine());
            assertEquals("01,1", br.readLine());
            assertEquals("02,1", br.readLine());
            assertEquals("03,1", br.readLine());
            assertEquals("04,1", br.readLine());
            assertEquals("05,1", br.readLine());
            assertEquals("06,1", br.readLine());
            assertEquals("07,1", br.readLine());
            assertEquals("08,1", br.readLine());
            assertEquals("09,1", br.readLine());
            assertEquals("10,1", br.readLine());
            assertEquals("11,1", br.readLine());
            assertEquals("12,1", br.readLine());
            assertEquals("13,1", br.readLine());
            assertEquals("14,1", br.readLine());
            assertNull(br.readLine());
        }
    }

    private void doDedupTests(int threshold) throws Exception {
        SortMergeWriter<String, Writer> sorter = new SortMergeWriter<String, Writer>(threshold,
                ToStringComparator.getComparator()) {

            @Override
            protected Writer createWriter(File file) {
                return FileHelper.getBufferedWriter(file);
            }

            @Override
            protected void writeRow(Writer writer, String row, int count) throws IOException {
                writer.write(row + "," + count + "\n");
            }

            @Override
            protected void writeHeader(Writer writer) throws IOException {
                // do nothing
            }

            @Override
            protected Writer createWriter(Resource file) {
                // Do nothing
                return null;
            }
        };

        sorter.append("02");
        sorter.append("01");
        sorter.append("04");
        sorter.append("03");
        sorter.append("06");
        sorter.append("07");
        sorter.append("08");
        sorter.append("05");
        sorter.append("09");
        sorter.append("10");
        sorter.append("13");
        sorter.append("12");
        sorter.append("11");
        sorter.append("14");
        sorter.append("02");
        sorter.append("01");
        sorter.append("01");
        sorter.append("14");
        sorter.append("10");
        sorter.append("10");
        sorter.append("10");
        File file = sorter.write("target/sort_merge_dedup_" + threshold + ".txt");

        assertTrue(file.exists());

        try (BufferedReader br = FileHelper.getBufferedReader(file)) {
            assertEquals("01,3", br.readLine());
            assertEquals("02,2", br.readLine());
            assertEquals("03,1", br.readLine());
            assertEquals("04,1", br.readLine());
            assertEquals("05,1", br.readLine());
            assertEquals("06,1", br.readLine());
            assertEquals("07,1", br.readLine());
            assertEquals("08,1", br.readLine());
            assertEquals("09,1", br.readLine());
            assertEquals("10,4", br.readLine());
            assertEquals("11,1", br.readLine());
            assertEquals("12,1", br.readLine());
            assertEquals("13,1", br.readLine());
            assertEquals("14,2", br.readLine());
            assertNull(br.readLine());
        }
    }
}
