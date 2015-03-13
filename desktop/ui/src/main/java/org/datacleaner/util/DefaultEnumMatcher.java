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
package org.datacleaner.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.util.HasName;

import com.google.common.base.Splitter;

/**
 * Default {@link EnumMatcher} implementation that uses a normalized/trimmed
 * version of the following values for exact matching:
 * 
 * <ul>
 * <li>The constant name of the enum</li>
 * <li>The name of the enum, if it implements {@link HasName}</li>
 * <li>The keywords of the enum, if it implements {@link HasKeywords}</li>
 * <li>The alias(es) of the enum, if it implements {@link HasAliases}</li>
 * </ul>
 * 
 * @param <E>
 *            the enum class
 */
public class DefaultEnumMatcher<E extends Enum<?>> implements EnumMatcher<E> {

    private final Map<String, E> _exactMatchesMap;

    public DefaultEnumMatcher(Class<E> enumClass) {
        _exactMatchesMap = new HashMap<String, E>();

        final E[] enumConstants = enumClass.getEnumConstants();

        if (ReflectionUtils.is(enumClass, HasAliases.class)) {
            for (final E e : enumConstants) {
                final HasAliases hasAliases = (HasAliases) e;
                final String[] aliases = hasAliases.getAliases();
                if (aliases != null) {
                    for (String alias : aliases) {
                        putMatch(alias, e);
                    }
                }
            }
        }

        if (ReflectionUtils.is(enumClass, HasName.class)) {
            for (final E e : enumConstants) {
                final HasName hasName = (HasName) e;
                final String name = hasName.getName();
                putMatch(name, e);
            }
        }

        for (final E e : enumConstants) {
            final String constantName = e.name();
            putMatch(constantName, e);
        }
    }

    private void putMatch(String string, E e) {
        final Collection<String> normalizedStrings = normalize(string, false);
        for (String normalizedString : normalizedStrings) {
            _exactMatchesMap.put(normalizedString, e);
        }
    }

    @Override
    public E suggestMatch(final String string) {
        final Collection<String> normalizedStrings = normalize(string, true);
        for (String normalizedString : normalizedStrings) {
            final E exactMatchResult = _exactMatchesMap.get(normalizedString);
            if (exactMatchResult != null) {
                return exactMatchResult;
            }
        }
        return null;
    }

    /**
     * Normalizes the incoming string before doing matching
     * 
     * @param string
     * @param aggressive
     * @param tokenize
     * @return
     */
    protected Collection<String> normalize(String string, boolean tokenize) {
        if (string == null) {
            return Collections.emptyList();
        }
        if (tokenize) {
            final Collection<String> result = new LinkedHashSet<>();

            result.addAll(normalize(string, false));

            final Splitter splitter = Splitter.on(' ').omitEmptyStrings();
            final List<String> tokens = splitter.splitToList(string);
            for (String token : tokens) {
                final Collection<String> normalizedTokens = normalize(token, false);
                result.addAll(normalizedTokens);
            }
            return result;
        } else {
            string = StringUtils.replaceWhitespaces(string, "");
            string = StringUtils.replaceAll(string, "-", "");
            string = StringUtils.replaceAll(string, "_", "");
            string = StringUtils.replaceAll(string, "|", "");
            string = StringUtils.replaceAll(string, "*", "");
            string = string.toUpperCase();
            if (string.isEmpty()) {
                return Collections.emptyList();
            }
            final String withoutNumbers = string.replaceAll("[0-9]", "");
            if (withoutNumbers.equals(string) || withoutNumbers.isEmpty()) {
                return Arrays.asList(string);
            }
            return Arrays.asList(string, withoutNumbers);
        }
    }
}
