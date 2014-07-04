/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.util;

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.util.HasAliases;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.metamodel.util.HasName;

import com.google.common.base.Strings;

/**
 * Default {@link EnumMatcher} implementation that uses a normalized/trimmed
 * version of the following values for exact matching:
 * 
 * <ul>
 * <li>The constant name of the enum</li>
 * <li>The name of the enum, if it implements {@link HasName}</li>
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
                        putExactMatch(alias, e);
                    }
                }
            }
        }

        if (ReflectionUtils.is(enumClass, HasName.class)) {
            for (final E e : enumConstants) {
                final HasName hasName = (HasName) e;
                final String name = hasName.getName();
                putExactMatch(name, e);
            }
        }

        for (final E e : enumConstants) {
            final String constantName = e.name();
            putExactMatch(constantName, e);
        }
    }

    private void putExactMatch(String string, E e) {
        final String normalizedString = normalize(string);
        if (Strings.isNullOrEmpty(normalizedString)) {
            return;
        }
        _exactMatchesMap.put(normalizedString, e);
    }

    @Override
    public E suggestMatch(final String string) {
        final String normalizedString = normalize(string);
        if (Strings.isNullOrEmpty(normalizedString)) {
            return null;
        }
        final E exactMatchResult = _exactMatchesMap.get(normalizedString);
        if (exactMatchResult != null) {
            return exactMatchResult;
        }

        return null;
    }

    /**
     * Normalizes the incoming string before doing matching
     * 
     * @param string
     * @return
     */
    protected String normalize(String string) {
        if (string == null) {
            return null;
        }
        string = StringUtils.replaceWhitespaces(string, "");
        string = StringUtils.replaceAll(string, "-", "");
        string = StringUtils.replaceAll(string, "_", "");
        string = StringUtils.replaceAll(string, "|", "");
        string = StringUtils.replaceAll(string, "*", "");
        string = string.toUpperCase();
        return string;
    }
}
