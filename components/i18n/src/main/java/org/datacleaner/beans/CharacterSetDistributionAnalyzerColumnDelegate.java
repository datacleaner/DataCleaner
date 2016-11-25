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
package org.datacleaner.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.datacleaner.api.InputRow;
import org.datacleaner.storage.RowAnnotation;
import org.datacleaner.storage.RowAnnotationFactory;
import org.datacleaner.util.CharIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.UnicodeSet;

/**
 * Performs character set distribution analysis for a single column. Used by the
 * {@link CharacterSetDistributionAnalyzer} for splitting up work.
 *
 *
 */
final class CharacterSetDistributionAnalyzerColumnDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CharacterSetDistributionAnalyzerColumnDelegate.class);
    private final RowAnnotationFactory _annotationFactory;
    private final Map<String, UnicodeSet> _unicodeSets;
    private final Map<String, RowAnnotation> _annotations;

    public CharacterSetDistributionAnalyzerColumnDelegate(final RowAnnotationFactory annotationFactory,
            final Map<String, UnicodeSet> unicodeSets) {
        _annotationFactory = annotationFactory;
        _unicodeSets = unicodeSets;
        _annotations = new HashMap<>();
        for (final String name : unicodeSets.keySet()) {
            _annotations.put(name, _annotationFactory.createAnnotation());
        }
    }

    public RowAnnotation getAnnotation(final String unicodeSetName) {
        return _annotations.get(unicodeSetName);
    }

    public synchronized void run(final String value, final InputRow row, final int distinctCount) {
        final List<Entry<String, UnicodeSet>> unicodeSetsRemaining = new ArrayList<>(_unicodeSets.entrySet());
        final CharIterator charIterator = new CharIterator(value);
        while (charIterator.hasNext()) {
            final Character c = charIterator.next();
            if (charIterator.isWhitespace() || charIterator.isDigit()) {
                logger.debug("Skipping whitespace/digit char: {}", c);
            } else {

                final Iterator<Entry<String, UnicodeSet>> it = unicodeSetsRemaining.iterator();
                while (it.hasNext()) {
                    final Entry<String, UnicodeSet> unicodeSet = it.next();
                    if (unicodeSet.getValue().contains(c)) {
                        final String name = unicodeSet.getKey();
                        final RowAnnotation annotation = _annotations.get(name);
                        _annotationFactory.annotate(row, distinctCount, annotation);

                        // remove this unicode set from the remaining checks on
                        // this value.
                        it.remove();
                    }
                }
            }
        }
    }

}
