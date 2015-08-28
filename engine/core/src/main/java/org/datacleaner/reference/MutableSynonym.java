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
package org.datacleaner.reference;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple, mutable implementation of the Synonym interface
 */
public class MutableSynonym implements Synonym {

    private final String _masterTerm;
    private final Set<String> _synonyms;

    public MutableSynonym(String masterTerm) {
        _masterTerm = masterTerm;
        _synonyms = new HashSet<String>();
    }

    public void addSynonym(String synonym) {
        _synonyms.add(synonym);
    }

    @Override
    public String getMasterTerm() {
        return _masterTerm;
    }

    @Override
    public Collection<String> getSynonyms() {
        return _synonyms;
    }

}
