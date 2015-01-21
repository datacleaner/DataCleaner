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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

final class BatchEntry<I, O> {

    private final I _input;
    private final CountDownLatch _countDownLatch;
    private volatile O _output;

    public BatchEntry(I input) {
        _input = input;
        _countDownLatch = new CountDownLatch(1);
    }

    public I getInput() {
        return _input;
    }

    public O getOuput() {
        return _output;
    }

    public void setOutput(O output) {
        _output = output;
        _countDownLatch.countDown();
    }

    public boolean await(long waitMillis) throws InterruptedException {
        return _countDownLatch.await(waitMillis, TimeUnit.MILLISECONDS);
    }
}
