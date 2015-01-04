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
package org.eobjects.analyzer.util.convert;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eobjects.analyzer.beans.api.Converter;
import org.eobjects.analyzer.util.CharIterator;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Converter} implementation for array types.
 */
public class ArrayConverter implements Converter<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ArrayConverter.class);

    private final Converter<Object> _parentConvert;

    public ArrayConverter(Converter<Object> parentConverter) {
        _parentConvert = parentConverter;
    }

    @Override
    public Object fromString(Class<?> type, String str) {
        final boolean isList = ReflectionUtils.is(type, List.class);
        if (isList) {
            // Warning: We only support string lists, since component type is
            // not determinable from List.class.
            type = String[].class;
        }

        str = str.trim();
        
        Object result = fromStringInternal(type, str);

        if (isList) {
            String[] array = (String[]) result;
            result = Arrays.asList(array);
        }

        return result;
    }

    public Object fromStringInternal(Class<?> type, String str) {

        assert type.isArray();

        final Class<?> componentType = type.getComponentType();

        if ("[]".equals(str)) {
            logger.debug("found [], returning empty array");
            return Array.newInstance(componentType, 0);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("deserializeArray(\"{}\")", str);
            logger.debug("component type is: {}", componentType);

            int beginningBrackets = 0;
            int endingBrackets = 0;

            CharIterator it = new CharIterator(str);
            while (it.hasNext()) {
                it.next();
                if (it.is('[')) {
                    beginningBrackets++;
                } else if (it.is(']')) {
                    endingBrackets++;
                }
            }
            it.reset();
            logger.debug("brackets statistics: beginning={}, ending={}", beginningBrackets, endingBrackets);
            if (beginningBrackets != endingBrackets) {
                logger.warn("Unbalanced beginning and ending brackets!");
            }
        }

        if (!str.startsWith("[") || !str.endsWith("]")) {
            if (str.indexOf(',') == -1) {
                Object result = Array.newInstance(componentType, 1);
                Object singleItem = _parentConvert.fromString(componentType, str);

                Array.set(result, 0, singleItem);
                return result;
            }
            throw new IllegalArgumentException(
                    "Cannot parse string as array, bracket encapsulation and comma delimitors expected. Found: " + str);
        }

        final String innerString = str.substring(1, str.length() - 1);
        logger.debug("innerString: {}", innerString);

        List<Object> objects = new ArrayList<Object>();
        int offset = 0;
        while (offset < innerString.length()) {
            logger.debug("offset: {}", offset);
            final int commaIndex = innerString.indexOf(',', offset);
            logger.debug("commaIndex: {}", commaIndex);
            final int bracketBeginIndex = innerString.indexOf('[', offset);
            logger.debug("bracketBeginIndex: {}", bracketBeginIndex);

            if (commaIndex == -1) {
                logger.debug("no comma found");
                String s = innerString.substring(offset);
                Object item = _parentConvert.fromString(componentType, s);
                objects.add(item);
                offset = innerString.length();
            } else if (bracketBeginIndex == -1 || commaIndex < bracketBeginIndex) {
                String s = innerString.substring(offset, commaIndex);
                if ("".equals(s)) {
                    offset++;
                } else {
                    logger.debug("no brackets in next element: \"{}\"", s);
                    Object item = _parentConvert.fromString(componentType, s);
                    objects.add(item);
                    offset = commaIndex + 1;
                }
            } else {

                String s = innerString.substring(bracketBeginIndex);
                int nextBracket = 0;
                int depth = 1;
                logger.debug("substring with nested array: {}", s);

                while (depth > 0) {
                    final int searchOffset = nextBracket + 1;
                    int nextEndBracket = s.indexOf(']', searchOffset);
                    if (nextEndBracket == -1) {
                        throw new IllegalStateException("No ending bracket in array string: "
                                + s.substring(searchOffset));
                    }
                    int nextBeginBracket = s.indexOf('[', searchOffset);
                    if (nextBeginBracket == -1) {
                        nextBeginBracket = s.length();
                    }

                    nextBracket = Math.min(nextEndBracket, nextBeginBracket);
                    char c = s.charAt(nextBracket);
                    logger.debug("nextBracket: {} ({})", nextBracket, c);

                    if (c == '[') {
                        depth++;
                    } else if (c == ']') {
                        depth--;
                    } else {
                        throw new IllegalStateException("Unexpected char: " + c);
                    }
                    logger.debug("depth: {}", depth);
                    if (depth == 0) {
                        s = s.substring(0, nextBracket + 1);
                        logger.debug("identified array: {}", s);
                    }
                }

                logger.debug("recursing to nested array: {}", s);

                logger.debug("inner array string: " + s);
                Object item = _parentConvert.fromString(componentType, s);
                objects.add(item);

                offset = bracketBeginIndex + s.length();
            }
        }

        Object result = Array.newInstance(componentType, objects.size());
        for (int i = 0; i < objects.size(); i++) {
            Array.set(result, i, objects.get(i));
        }
        return result;
    }

    @Override
    public String toString(Object o) {
        assert o != null;
        assert o.getClass().isArray() || o instanceof List;
        
        if (o instanceof List) {
            o = ((List<?>) o).toArray();
        }

        StringBuilder sb = new StringBuilder();
        int length = Array.getLength(o);
        sb.append('[');
        for (int i = 0; i < length; i++) {
            Object obj = Array.get(o, i);
            if (i != 0) {
                sb.append(',');
            }
            sb.append(_parentConvert.toString(obj));
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean isConvertable(Class<?> type) {
        return type.isArray() || ReflectionUtils.is(type, List.class);
    }
}