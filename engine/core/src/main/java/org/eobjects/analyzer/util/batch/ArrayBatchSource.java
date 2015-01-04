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
package org.eobjects.analyzer.util.batch;

import java.util.ArrayList;
import java.util.List;

final class ArrayBatchSource<I> implements BatchSource<I> {

    private final Object[] _input;

    public ArrayBatchSource(Object[] input) {
        _input = input;
    }

    @Override
    public int size() {
        return _input.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public I getInput(int index) {
        return (I) _input[index];
    }

    @Override
    public List<I> toList() {
        final int size = size();
        final List<I> list = new ArrayList<I>(size);
        for (int i = 0; i < size; i++) {
            list.add(getInput(i));
        }
        return list;
    }
}
