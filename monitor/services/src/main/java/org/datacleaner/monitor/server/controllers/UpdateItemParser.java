package org.datacleaner.monitor.server.controllers;

import org.apache.metamodel.query.parser.QueryParserException;
import org.apache.metamodel.query.parser.QueryPartProcessor;

public class UpdateItemParser implements QueryPartProcessor {

    private final UpdateQuery _query;
    
    public UpdateItemParser(UpdateQuery query) {
        this._query = query;
    }

    @Override
    public void parse(String delim, String token) {

        String[] parts = token.split("=");
        if(parts.length != 2) {
            throw new QueryParserException("Not a valid column specification: " + token);
        }
        _query.addUpdateColumn(parts[0], parts[1]);
    }

}
