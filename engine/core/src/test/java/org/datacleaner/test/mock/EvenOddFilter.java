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
package org.datacleaner.test.mock;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;

import org.datacleaner.api.Configured;
import org.datacleaner.api.Filter;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;

@Named("Even/odd record filter")
public class EvenOddFilter implements Filter<EvenOddFilter.Category> {

    public static enum Category {
        EVEN, ODD
    }

    private final AtomicInteger counter = new AtomicInteger();

    @Configured
    InputColumn<String> column;

    @Override
    public EvenOddFilter.Category categorize(InputRow inputRow) {
        int v = counter.incrementAndGet();
        if (v % 2 == 0) {
            return Category.EVEN;
        }
        return Category.ODD;
    }

}