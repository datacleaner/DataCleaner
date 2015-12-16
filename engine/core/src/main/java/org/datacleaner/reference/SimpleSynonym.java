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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class SimpleSynonym implements Synonym, Serializable {

    private static final long serialVersionUID = 1L;

    private final String _masterTerm;
    private final String[] _synonyms;

    public SimpleSynonym(String masterTerm, String... synonyms) {
        _masterTerm = masterTerm;
        _synonyms = synonyms;
    }

    @Override
    public String getMasterTerm() {
        return _masterTerm;
    }

    @Override
    public List<String> getSynonyms() {
        return Arrays.asList(_synonyms);
    }

}
