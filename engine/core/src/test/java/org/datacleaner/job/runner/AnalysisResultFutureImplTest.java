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

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.job.AnalyzerJob;
import org.datacleaner.job.ComponentJob;
import org.datacleaner.job.concurrent.StatusAwareTaskListener;
import org.datacleaner.result.NumberResult;
import org.easymock.EasyMock;

import junit.framework.TestCase;

public class AnalysisResultFutureImplTest extends TestCase {

    public void testIsSuccessful() throws Exception {
        final Queue<JobAndResult> resultQueue = new LinkedList<>();
        final StatusAwareTaskListener jobCompletionListener = EasyMock.createMock(StatusAwareTaskListener.class);
        final ErrorAware errorAware = EasyMock.createMock(ErrorAware.class);

        EasyMock.expect(jobCompletionListener.isDone()).andReturn(true);
        EasyMock.expect(errorAware.isErrornous()).andReturn(false);
        EasyMock.expect(errorAware.isErrornous()).andReturn(true);

        EasyMock.replay(jobCompletionListener, errorAware);

        final AnalysisResultFutureImpl resultFuture =
                new AnalysisResultFutureImpl(resultQueue, jobCompletionListener, errorAware);
        assertTrue(resultFuture.isSuccessful());

        assertFalse(resultFuture.isSuccessful());

        EasyMock.verify(jobCompletionListener, errorAware);
    }

    public void testGetResultByJob() throws Exception {
        final AnalyzerJob analyzerJob1 = EasyMock.createMock(AnalyzerJob.class);
        final AnalyzerJob analyzerJob2 = EasyMock.createMock(AnalyzerJob.class);
        final AnalyzerJob analyzerJob3 = EasyMock.createMock(AnalyzerJob.class);

        final Queue<JobAndResult> resultQueue = new LinkedList<>();

        resultQueue.add(new JobAndResult(analyzerJob1, new NumberResult(1)));
        resultQueue.add(new JobAndResult(analyzerJob2, new NumberResult(2)));

        final StatusAwareTaskListener jobCompletionListener = EasyMock.createMock(StatusAwareTaskListener.class);
        final ErrorAware errorAware = EasyMock.createMock(ErrorAware.class);
        EasyMock.expect(jobCompletionListener.isDone()).andReturn(true);
        EasyMock.expect(errorAware.isErrornous()).andReturn(false).times(4);

        EasyMock.replay(jobCompletionListener, errorAware);

        final AnalysisResultFutureImpl resultFuture =
                new AnalysisResultFutureImpl(resultQueue, jobCompletionListener, errorAware);

        resultFuture.await();

        assertEquals("1", resultFuture.getResult(analyzerJob1).toString());
        assertEquals("2", resultFuture.getResult(analyzerJob2).toString());
        assertNull(resultFuture.getResult(analyzerJob3));

        final Map<ComponentJob, AnalyzerResult> resultMap = resultFuture.getResultMap();

        EasyMock.verify(jobCompletionListener, errorAware);

        assertEquals(2, resultMap.size());
        assertEquals("1", resultMap.get(analyzerJob1).toString());
        assertEquals("2", resultMap.get(analyzerJob2).toString());
    }

    public void testCancel() throws Exception {
        final Queue<JobAndResult> resultQueue = new LinkedList<>();
        final StatusAwareTaskListener jobCompletionListener = EasyMock.createMock(StatusAwareTaskListener.class);
        final ErrorAware errorAware = EasyMock.createMock(ErrorAware.class);

        jobCompletionListener.onError(null, new AnalysisJobCancellation());
        EasyMock.expect(errorAware.isCancelled()).andReturn(true);

        EasyMock.replay(jobCompletionListener, errorAware);

        final AnalysisResultFutureImpl resultFuture =
                new AnalysisResultFutureImpl(resultQueue, jobCompletionListener, errorAware);
        resultFuture.cancel();

        assertTrue(resultFuture.isCancelled());

        EasyMock.verify(jobCompletionListener, errorAware);
    }
}
