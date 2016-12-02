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
package org.datacleaner.monitor.server.jaxb;

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

import org.datacleaner.monitor.scheduling.model.ExecutionIdentifier;
import org.datacleaner.monitor.scheduling.model.ExecutionStatus;
import org.datacleaner.monitor.scheduling.model.TriggerType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX based reader of {@link ExecutionIdentifier}s.
 */
public class SaxExecutionIdentifierReader extends DefaultHandler {

    private static class StopParsingException extends SAXException {
        private static final long serialVersionUID = 1L;
    }

    private static final Set<String> ELEMENT_NAMES =
            new HashSet<>(Arrays.asList("result-id", "execution-status", "trigger-type", "job-begin-date"));
    private final String _name;
    private final Map<String, String> _valueMap;
    private final StringBuilder _valueBuilder;

    private boolean _readChars;

    private SaxExecutionIdentifierReader() {
        this("<unknown>");
    }

    private SaxExecutionIdentifierReader(final String name) {
        _name = name;
        _valueMap = new HashMap<>();
        _valueBuilder = new StringBuilder();

        _readChars = false;
    }

    @Deprecated
    public static ExecutionIdentifier read(final InputStream in) {
        return new SaxExecutionIdentifierReader().parse(in);
    }

    public static ExecutionIdentifier read(final InputStream in, final String name) {
        return new SaxExecutionIdentifierReader(name).parse(in);
    }

    public ExecutionIdentifier parse(final InputStream in) {
        final SAXParserFactory parserFactory = SAXParserFactory.newInstance();

        parserFactory.setNamespaceAware(false);
        parserFactory.setValidating(false);

        final SAXParser parser;
        try {
            parser = parserFactory.newSAXParser();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to create SAX parser", e);
        }

        try {
            parser.parse(in, this);
        } catch (final Exception e) {
            if (e instanceof StopParsingException) {
                // parsing was stopped intentionally
            } else {
                throw new IllegalStateException("Failed to parse ExecutionIdentifier document: " + _name, e);
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

    private Date toDate(final String string) {
        if (string == null) {
            return null;
        }
        final Calendar cal = DatatypeConverter.parseDate(string);
        return cal.getTime();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        final String name = stripNamespace(qName);
        if (ELEMENT_NAMES.contains(name)) {
            _readChars = true;
        }
    }

    private String stripNamespace(final String qName) {
        final int colonIndex = qName.indexOf(':');
        if (colonIndex == -1) {
            return qName;
        }
        return qName.substring(colonIndex + 1);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (_readChars) {
            _valueBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
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
