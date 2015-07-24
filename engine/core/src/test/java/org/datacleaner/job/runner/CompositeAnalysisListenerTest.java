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
package org.datacleaner.job.runner;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.ComponentMessage;
import org.datacleaner.api.InputRow;
import org.datacleaner.job.AnalysisJob;
import org.datacleaner.job.ComponentJob;

public class CompositeAnalysisListenerTest extends TestCase {
    public void testHandlingErrors() {
        final AtomicInteger counter = new AtomicInteger(0);

        AnalysisListener faultyListener = new AnalysisListener() {
            @Override
            public void jobBegin(final AnalysisJob job, final AnalysisJobMetrics metrics) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void jobSuccess(final AnalysisJob job, final AnalysisJobMetrics metrics) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void rowProcessingProgress(final AnalysisJob job, final RowProcessingMetrics metrics,
                    final InputRow row, final int rowNumber) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void onComponentMessage(final AnalysisJob job, final ComponentJob componentJob,
                    final ComponentMessage message) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void rowProcessingSuccess(final AnalysisJob job, final RowProcessingMetrics metrics) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void componentBegin(final AnalysisJob job, final ComponentJob componentJob,
                    final ComponentMetrics metrics) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void componentSuccess(final AnalysisJob job, final ComponentJob componentJob,
                    final AnalyzerResult result) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void errorInComponent(final AnalysisJob job, final ComponentJob componentJob, final InputRow row,
                    final Throwable throwable) {
                throw new RuntimeException("OUCH!");
            }

            @Override
            public void errorUnknown(final AnalysisJob job, final Throwable throwable) {
                throw new RuntimeException("OUCH!");
            }
        };

        AnalysisListener goodListener = new AnalysisListener() {
            @Override
            public void jobBegin(final AnalysisJob job, final AnalysisJobMetrics metrics) {
                counter.incrementAndGet();
            }

            @Override
            public void jobSuccess(final AnalysisJob job, final AnalysisJobMetrics metrics) {
                counter.incrementAndGet();
            }

            @Override
            public void rowProcessingBegin(final AnalysisJob job, final RowProcessingMetrics metrics) {
                counter.incrementAndGet();
            }

            @Override
            public void rowProcessingProgress(final AnalysisJob job, final RowProcessingMetrics metrics,
                    final InputRow row, final int rowNumber) {
                counter.incrementAndGet();
            }

            @Override
            public void onComponentMessage(final AnalysisJob job, final ComponentJob componentJob,
                    final ComponentMessage message) {
                counter.incrementAndGet();
            }

            @Override
            public void rowProcessingSuccess(final AnalysisJob job, final RowProcessingMetrics metrics) {
                counter.incrementAndGet();
            }

            @Override
            public void componentBegin(final AnalysisJob job, final ComponentJob componentJob,
                    final ComponentMetrics metrics) {
                counter.incrementAndGet();
            }

            @Override
            public void componentSuccess(final AnalysisJob job, final ComponentJob componentJob,
                    final AnalyzerResult result) {
                counter.incrementAndGet();
            }

            @Override
            public void errorInComponent(final AnalysisJob job, final ComponentJob componentJob, final InputRow row,
                    final Throwable throwable) {
                counter.incrementAndGet();
            }

            @Override
            public void errorUnknown(final AnalysisJob job, final Throwable throwable) {
                counter.incrementAndGet();
            }
        };


        final CompositeAnalysisListener compositeAnalysisListener = new CompositeAnalysisListener(faultyListener, goodListener);

        compositeAnalysisListener.componentBegin(null, null , null);
        assertEquals(1, counter.get());
        compositeAnalysisListener.componentSuccess(null, null, null);
        assertEquals(2, counter.get());
        compositeAnalysisListener.errorInComponent(null, null, null, null);
        assertEquals(3, counter.get());
        compositeAnalysisListener.errorUnknown(null, null);
        assertEquals(4, counter.get());
        compositeAnalysisListener.jobBegin(null, null);
        assertEquals(5, counter.get());
        compositeAnalysisListener.jobSuccess(null, null);
        assertEquals(6, counter.get());
        compositeAnalysisListener.onComponentMessage(null, null, null);
        assertEquals(7, counter.get());
        compositeAnalysisListener.rowProcessingBegin(null, null);
        assertEquals(8, counter.get());
        compositeAnalysisListener.rowProcessingProgress(null, null, null, 0);
        assertEquals(9, counter.get());
        compositeAnalysisListener.rowProcessingSuccess(null, null);
        assertEquals(10, counter.get());
    }
}