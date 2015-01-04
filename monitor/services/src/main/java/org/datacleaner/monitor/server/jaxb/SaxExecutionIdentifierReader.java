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
package org.eobjects.datacleaner.monitor.server.jaxb;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.eobjects.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.eobjects.datacleaner.monitor.scheduling.model.TriggerType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX based reader of {@link ExecutionIdentifier}s.
 */
public class SaxExecutionIdentifierReader extends DefaultHandler {

    private static final Set<String> ELEMENT_NAMES = new HashSet<String>(Arrays.asList("result-id", "execution-status",
            "trigger-type", "job-begin-date"));

    private static class StopParsingException extends SAXException {
        private static final long serialVersionUID = 1L;
    }

    private final Map<String, String> _valueMap;
    private final StringBuilder _valueBuilder;

    private boolean _readChars;

    public static ExecutionIdentifier read(InputStream in) {
        return new SaxExecutionIdentifierReader().parse(in);
    }

    private SaxExecutionIdentifierReader() {
        _valueMap = new HashMap<String, String>();
        _valueBuilder = new StringBuilder();

        _readChars = false;
    }

    public ExecutionIdentifier parse(InputStream in) {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();

        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(false);

        final SAXParser parser;
        try {
            parser = parserFactory.newSAXParser();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SAX parser", e);
        }

        try {
            parser.parse(in, this);
        } catch (Exception e) {
            if (e instanceof StopParsingException) {
                // parsing was stopped intentionally
            } else {
                throw new IllegalStateException("Failed to parse ExecutionIdentifier document", e);
            }
        }
        return createExecutionIdentifier();
    }

    private ExecutionIdentifier createExecutionIdentifier() {
        final ExecutionIdentifier result = new ExecutionIdentifier();
        result.setExecutionStatus(ExecutionStatus.valueOf(_valueMap.get("execution-status")));
        result.setResultId(_valueMap.get("result-id"));
        result.setJobBeginDate(toDate(_valueMap.get("job-begin-date")));
        result.setTriggerType(TriggerType.valueOf(_valueMap.get("trigger-type")));
        return result;
    }

    private Date toDate(String string) {
		if (string == null) {
			return null;
		}
		Calendar cal = DatatypeConverter.parseDate(string);
		return cal.getTime();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        final String name = stripNamespace(qName);
        if (ELEMENT_NAMES.contains(name)) {
            _readChars = true;
        }
    }

    private String stripNamespace(String qName) {
        final int colonIndex = qName.indexOf(':');
        if (colonIndex == -1) {
            return qName;
        }
        return qName.substring(colonIndex + 1);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (_readChars) {
            _valueBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (_readChars) {
            final String name = stripNamespace(qName);
            _valueMap.put(name, _valueBuilder.toString());
            _valueBuilder.setLength(0);
            _readChars = false;

            if (_valueMap.size() == ELEMENT_NAMES.size()) {
                // we're done!
                throw new StopParsingException();
            }
        }
    }
}
