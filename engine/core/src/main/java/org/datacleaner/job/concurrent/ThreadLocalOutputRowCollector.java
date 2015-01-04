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
package org.datacleaner.job.concurrent;

import org.datacleaner.beans.api.OutputRowCollector;

/**
 * Thread local implementation of the {@link OutputRowCollector} interface.
 * 
 * This implementation holds a listener which recieves the values that are put
 * to the collector. Consumers should register a listener and remove it after
 * invocation.
 */
public class ThreadLocalOutputRowCollector implements OutputRowCollector {

	/**
	 * Listener interface to be implemented by users of the output row
	 * collector.
	 */
	public static interface Listener {

		public void onValues(Object[] values);

	}

	private final ThreadLocal<Listener> _listener;

	public ThreadLocalOutputRowCollector() {
		_listener = new ThreadLocal<Listener>();
	}

	public void setListener(Listener listener) {
		_listener.set(listener);
	}

	public void removeListener() {
		_listener.remove();
	}

	@Override
	public void putValues(Object... values) {
		Listener listener = _listener.get();
		if (listener == null) {
			throw new IllegalStateException("No thread local listener registered!");
		}
		listener.onValues(values);
	}

}
