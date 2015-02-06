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
package org.datacleaner.api;

import org.apache.metamodel.util.HasName;
import org.apache.metamodel.util.Ref;

/**
 * Represents an {@link AnalyzerResult} that is still being produced.
 * 
 * Usually {@link AnalyzerResult}s are produced immediately by the
 * {@link Analyzer#getResult()} method, but in cases where this may take a long
 * time, an {@link Analyzer} can instead return a result of this type and
 * thereby indicate that some process is still going on, but the rest of the job
 * is ready to return.
 *
 * @param <R>
 *            the wrapped {@link AnalyzerResult} type.
 */
public interface AnalyzerResultFuture<R extends AnalyzerResult> extends AnalyzerResult, HasName, Ref<R> {

    /**
     * Listener interface for objects that want to be notified when the wrapped
     * {@link AnalyzerResult} is ready.
     *
     * @param <R>
     */
    public static interface Listener<R extends AnalyzerResult> {

        public void onSuccess(R result);

        public void onError(RuntimeException error);
    }

    /**
     * Gets the name of the {@link AnalyzerResult} that is being waited on. This
     * is useful for presenting to the user what he is waiting for.
     * 
     * @return the name of the result being waited for
     */
    @Override
    public String getName();

    /**
     * Adds a {@link Listener} to this {@link AnalyzerResultFuture}.
     * 
     * @param listener
     */
    public void addListener(Listener<? super R> listener);

    /**
     * Removes a {@link Listener} from this {@link AnalyzerResultFuture}.
     * 
     * @param listener
     */
    public void removeListener(Listener<R> listener);

    /**
     * Determines if the wrapped {@link AnalyzerResult} is ready or if
     * processing is still going on to produce it.
     * 
     * Once ready, call {@link #get()} to get it.
     * 
     * @return true if the wrapped {@link AnalyzerResult} is ready or false if
     *         it is not.
     */
    public boolean isReady();

    /**
     * Gets (and awaits the processing in a blocking fashion if necesary) the
     * wrapped {@link AnalyzerResult}.
     * 
     * @return the wrapped {@link AnalyzerResult}
     */
    @Override
    public R get();
}
