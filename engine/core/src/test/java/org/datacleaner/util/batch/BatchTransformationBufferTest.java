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
package org.datacleaner.util.batch;

import junit.framework.TestCase;

public class BatchTransformationBufferTest extends TestCase {

    public void testSingleThreadedScenario() throws Exception {
        final BatchTransformation<Integer, String> batchTransformation = new BatchTransformation<Integer, String>() {
            @Override
            public void map(BatchSource<Integer> source, BatchSink<String> sink) {
                for (int i = 0; i < source.size(); i++) {
                    Integer input = source.getInput(i);
                    String output = input + "bar";
                    sink.setOutput(i, output);
                }
            }
        };

        final BatchTransformationBuffer<Integer, String> buffer = new BatchTransformationBuffer<Integer, String>(
                batchTransformation);
        buffer.start();
        try {
            final String[] results = new String[2];

            for (int i = 0; i < 2; i++) {
                String result = buffer.transform(i);
                results[i] = result;
            }

            for (int i = 0; i < 2; i++) {
                assertEquals(i + "bar", results[i]);
            }
        } finally {
            buffer.shutdown();
        }
    }

    public void testScenario1() throws Exception {
        runScenario(1, 10, 200);
    }

    public void testScenario10() throws Exception {
        runScenario(10, 4, 10);
    }

    public void testScenario1000() throws Exception {
        runScenario(1000, 100, 100);
    }

    public int runScenario(int numThreads, int maxBatchSize, int flushInterval) {
        System.out.println("Running scenario with " + numThreads + ", maxBatchSize=" + maxBatchSize
                + ", flushInterval=" + flushInterval + "ms");

        final BatchTransformation<Integer, String> batchTransformation = new BatchTransformation<Integer, String>() {
            @Override
            public void map(BatchSource<Integer> source, BatchSink<String> sink) {
                for (int i = 0; i < source.size(); i++) {
                    Integer input = source.getInput(i);
                    String output = input + "bar";
                    sink.setOutput(i, output);
                }
            }
        };

        final BatchTransformationBuffer<Integer, String> buffer = new BatchTransformationBuffer<Integer, String>(
                batchTransformation, maxBatchSize, flushInterval);
        buffer.start();
        try {
            final String[] results = new String[numThreads];
            final Thread[] threads = new Thread[numThreads];

            for (int i = 0; i < threads.length; i++) {
                final int index = i;
                threads[i] = new Thread() {
                    @Override
                    public void run() {
                        String result = buffer.transform(index);
                        results[index] = result;
                    }
                };
                threads[i].start();
            }

            for (int i = 0; i < threads.length; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < threads.length; i++) {
                assertEquals(i + "bar", results[i]);
            }
        } finally {
            buffer.shutdown();
        }
        return buffer.getBatchCount();
    }
}
