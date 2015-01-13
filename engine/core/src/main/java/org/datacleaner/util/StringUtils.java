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

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * Contains various utility methods regarding string handling.
 * 
 * Consider using Guava's {@link Strings} instead.
 */
public final class StringUtils {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\p{Zs}\\p{javaWhitespace}]+");

    public static final String LATIN_CHARACTERS = "";

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isDiacritic(char c) {
        if (Character.isLetter(c)) {
            return !isLatin(c);
        }
        return false;
    }

    public static boolean isLatin(char c) {
        return c >= 'A' && c <= 'z';
    }

    public static String leftTrim(String str) {
        int i = 0;
        while (i < str.length() && Character.isWhitespace(str.charAt(i))) {
            i++;
        }
        return str.substring(i);
    }

    public static String rightTrim(String str) {
        int i = str.length() - 1;
        while (i >= 0 && Character.isWhitespace(str.charAt(i))) {
            i--;
        }
        return str.substring(0, i + 1);
    }

    public static String replaceWhitespaces(String inString, String with) {
        return WHITESPACE_PATTERN.matcher(inString).replaceAll(with);
    }

    public static int indexOf(final char c, final char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            if (c == chars[i]) {
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

        Matcher matcher = WHITESPACE_PATTERN.matcher(name);
        if (!matcher.find()) {
            return name;
        }

        int indexOfWhitespace = matcher.start();
        if (indexOfWhitespace == -1) {
            return name;
        }

        String substring1 = name.substring(0, indexOfWhitespace);

        String substring2 = name.substring(indexOfWhitespace + 1);
        substring2 = Character.toUpperCase(substring2.charAt(0)) + substring2.substring(1);

        name = substring1 + substring2;

        return toCamelCase(name);
    }

    public static String getLongestCommonToken(Iterable<String> iterable, char tokenSeparatorChar) {
        Iterator<String> it = iterable.iterator();
        String commonToken = it.next();
        while (it.hasNext()) {
            if (commonToken == "") {
                return null;
            }
            String name = it.next();
            if (!name.startsWith(commonToken)) {
                commonToken = getLongestCommonToken(commonToken, name, tokenSeparatorChar);
            }
        }
        return commonToken;
    }

    public static String getLongestCommonToken(String str1, String str2, char tokenSeparatorChar) {
        StringBuilder result = new StringBuilder();
        String[] tokens1 = str1.split("\\" + tokenSeparatorChar);
        String[] tokens2 = str2.split("\\" + tokenSeparatorChar);
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
     * @param v
     * @param searchToken
     * @param replacement
     * @return
     */
    public static String replaceAll(String v, String searchToken, String replacement) {
        if (v == null) {
            return v;
        }
        while (v.indexOf(searchToken) != -1) {
            v = v.replace(searchToken, replacement);
        }
        return v;
    }
}
