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
package org.eobjects.analyzer.job;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A very simple ID generator that generates id with a prefix and an
 * incrementing suffix.
 * 
 * 
 */
public final class PrefixedIdGenerator implements IdGenerator {

	private final String prefix;
	private final AtomicInteger counter;
    private final String idNumberPattern;
	
	public PrefixedIdGenerator() {
	    this("");
    }

	public PrefixedIdGenerator(String prefix) {
		this(prefix, 4);
	}
	
	public PrefixedIdGenerator(String prefix, int idNumberSize) {
	    if (idNumberSize <= 0) {
	        throw new IllegalArgumentException("idNumberSize must be a postive integer");
	    }
	    this.prefix = prefix;
        this.counter = new AtomicInteger(0);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idNumberSize; i++) {
            sb.append('0');
        }
        this.idNumberPattern = sb.toString();
	}

	@Override
	public String nextId() {
		int i = counter.incrementAndGet();
		DecimalFormat format = new DecimalFormat(idNumberPattern);
		
		return prefix + format.format(i);
	}

}
