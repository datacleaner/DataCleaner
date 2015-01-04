/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.concurrent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A task listener that has the ability to block (using the await(...) methods)
 * and to tell if it has finished or not (using the isDone() method):
 * 
 * 
 */
public interface StatusAwareTaskListener extends TaskListener {

	public boolean isDone();

	public void await() throws InterruptedException;

	public void await(long timeout, TimeUnit timeUnit) throws InterruptedException;

	public Date getCompletionTime();
}
