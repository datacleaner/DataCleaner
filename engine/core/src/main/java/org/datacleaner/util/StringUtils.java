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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Contains various utility methods regarding string handling.
 *
 * Consider using Guava's {@link Strings} instead.
 */
public final class StringUtils {

    public static final String LATIN_CHARACTERS = "";
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\p{Zs}\\p{javaWhitespace}]+");
    private static final Pattern SINGLE_WORD_PATTERN = Pattern.compile(".+\\b.+");
    private static final Pattern WORD_BOUNDARY_PATTERN = Pattern.compile("\\b");

    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isDiacritic(final char character) {
        if (Character.isLetter(character)) {
            return !isLatin(character);
        }
        return false;
    }

    public static boolean isLatin(final char character) {
        return character >= 'A' && character <= 'z';
    }

    public static String leftTrim(final String str) {
        int i = 0;
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        }
        return str.substring(i);
    }

    public static String rightTrim(final String str) {
        int i = str.length() - 1;
        while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
            i--;
        }
        return str.substring(0, i + 1);
    }

    public static String replaceWhitespaces(final String inString, final String with) {
        return WHITESPACE_PATTERN.matcher(inString).replaceAll(with);
    }

    public static int indexOf(final char character, final char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (character == chars[i]) {
                return i;
            }
        }
        return -1;
    }

    public static String toCamelCase(String name) {
        if (name == null) {
            return name;
        }

        name = name.trim();

        final Matcher matcher = WHITESPACE_PATTERN.matcher(name);
        if (!matcher.find()) {
            return name;
        }

        final int indexOfWhitespace = matcher.start();
        if (indexOfWhitespace == -1) {
            return name;
        }

        final String substring1 = name.substring(0, indexOfWhitespace);

        String substring2 = name.substring(indexOfWhitespace + 1);
        substring2 = Character.toUpperCase(substring2.charAt(0)) + substring2.substring(1);

        name = substring1 + substring2;

        return toCamelCase(name);
    }

    public static String getLongestCommonToken(final Iterable<String> iterable, final char tokenSeparatorChar) {
        final Iterator<String> it = iterable.iterator();
        String commonToken = it.next();
        while (it.hasNext()) {
            // TODO: This never worked?
            if (commonToken == "") {
                return null;
            }
            final String name = it.next();
            if (!name.startsWith(commonToken)) {
                commonToken = getLongestCommonToken(commonToken, name, tokenSeparatorChar);
            }
        }
        return commonToken;
    }

    public static String getLongestCommonToken(final String str1, final String str2, final char tokenSeparatorChar) {
        final StringBuilder result = new StringBuilder();
        final String[] tokens1 = str1.split("\\" + tokenSeparatorChar);
        final String[] tokens2 = str2.split("\\" + tokenSeparatorChar);
        for (int i = 0; i < Math.min(tokens1.length, tokens2.length); i++) {
            if (!tokens1[i].equals(tokens2[i])) {
                break;
            }
            if (i != 0) {
                result.append(tokenSeparatorChar);
            }
            result.append(tokens1[i]);
        }
        return result.toString();
    }

    /**
     * Utility method that will do replacement multiple times until no more
     * occurrences are left.
     *
     * Note that this is NOT the same as
     * {@link String#replaceAll(String, String)} which will only do one
     * run-through of the string, and it will use regexes instead of exact
     * searching.
     *
     * @param str
     * @param searchToken
     * @param replacement
     * @return
     */
    public static String replaceAll(String str, final String searchToken, final String replacement) {
        if (str == null) {
            return str;
        }
        str = str.replace(searchToken, replacement);
        return str;
    }

    /**
     * Determines if a String represents a single word. A single word is defined
     * as a non-null string containing no word boundaries after trimming.
     *
     * @param value
     * @return
     */
    public static boolean isSingleWord(String value) {
        if (value == null) {
            return false;
        }
        value = value.trim();
        if (value.isEmpty()) {
            return false;
        }
        return !SINGLE_WORD_PATTERN.matcher(value).matches();
    }

    /**
     * Splits a String on word boundaries, yielding tokens that are all
     * "single words" (see {@link #isSingleWord(String)}) or delimitors (if
     * includeDelims is set to true)
     *
     * @param value
     *            the String to split
     * @param includeDelims
     *            whether or not to include the delimitors in the returned list
     * @return a list containing words and delimitors.
     */
    public static List<String> splitOnWordBoundaries(final String value, final boolean includeDelims) {
        if (value == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(WORD_BOUNDARY_PATTERN.split(value));
    }
}
