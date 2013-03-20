/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.jaxb;

import org.eobjects.datacleaner.monitor.server.job.CustomJavaJob;

/**
 * A sample {@link CustomJavaJob} used to show examples of DataCleaner's
 * scheduler/job mechanism.
 */
public class SampleCustomJavaJob implements CustomJavaJob {

    @Override
    public void execute() throws Exception {
        for (int i = 0; i < 30; i++) {
            Thread.sleep(100);
            System.out.println("Counting to " + (i + 1) + " of 30 ...");
        }
    }
}
