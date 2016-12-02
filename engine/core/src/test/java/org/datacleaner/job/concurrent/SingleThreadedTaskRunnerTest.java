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

import org.datacleaner.job.tasks.Task;

import junit.framework.TestCase;

public class SingleThreadedTaskRunnerTest extends TestCase {

    private static class SimpleRecursiveTask implements Task {
        private TaskRunner taskRunner;
        private Task nextTask;
        private char charToPrint;
        private StringBuilder sb;

        public SimpleRecursiveTask(final StringBuilder sb, final char c, final Task nextTask,
                final TaskRunner taskRunner) {
            this.sb = sb;
            this.charToPrint = c;
            this.nextTask = nextTask;
            this.taskRunner = taskRunner;
        }

        @Override
        public void execute() throws Exception {
            sb.append(charToPrint);
            if (nextTask != null) {
                taskRunner.run(nextTask, null);
            }
            sb.append(charToPrint);
        }
    }

    public void testNonQueuedChronology() throws Exception {
        final SingleThreadedTaskRunner runner = new SingleThreadedTaskRunner();

        final StringBuilder sb = new StringBuilder();
        final Task task3 = new SimpleRecursiveTask(sb, 'c', null, null);
        final Task task2 = new SimpleRecursiveTask(sb, 'b', task3, runner);
        final Task task1 = new SimpleRecursiveTask(sb, 'a', task2, runner);

        runner.run(task1, null);

        assertEquals("abccba", sb.toString());
    }
}
