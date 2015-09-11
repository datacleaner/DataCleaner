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
package org.datacleaner.monitor.server.controllers;

import org.apache.metamodel.query.parser.QueryParserException;
import org.apache.metamodel.query.parser.QueryPartProcessor;

/**
 * Parses a single item in the SET clause of an UPDATE statement, e.g. (id=42)
 *
 */
public class UpdateItemParser implements QueryPartProcessor {

    private final UpdateQuery _query;

    public UpdateItemParser(UpdateQuery query) {
        this._query = query;
    }

    @Override
    public void parse(String delim, String token) {

        String[] parts = token.split("=");
        if (parts.length != 2) {
            throw new QueryParserException("Not a valid column specification: " + token);
        }
        _query.addUpdateColumn(parts[0], parts[1]);
    }

}
