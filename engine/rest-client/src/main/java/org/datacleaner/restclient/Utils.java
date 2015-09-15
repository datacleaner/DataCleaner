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
package org.datacleaner.restclient;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @Since 9/15/15
 */
public abstract class Utils {

    /**
     * Encodes the given URI path segment with the given encoding.
     * <P>Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency).
     */
    public static String encodeUrlPathSegment(String string) {
        try {
            byte[] bytes = encodeBytes(string.getBytes("UTF-8"));
            return new String(bytes, "US-ASCII");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String escapeComponentName(String name) {
        return name.replace("/", "_@_");
    }

    public static String unescapeComponentName(String escapedName) {
        return escapedName.replace("_@_", "/");
    }

    /** Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency) */
    private static boolean isPchar(int c) {
        return isUnreserved(c) || isSubDelimiter(c) || ':' == c || '@' == c;
    }
    /** Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency) */
    private static boolean isUnreserved(int c) {
        return isAlpha(c) || isDigit(c) || '-' == c || '.' == c || '_' == c || '~' == c;
    }
    /** Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency) */
    private static boolean isAlpha(int c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }
    /** Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency) */
    private static boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }
    /** Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency) */
    private static boolean isSubDelimiter(int c) {
        return '!' == c || '$' == c || '&' == c || '\'' == c || '(' == c || ')' == c || '*' == c || '+' == c ||
                ',' == c || ';' == c || '=' == c;
    }
    /** Copy&paste from org.springframework.web.util.UriUtils.encodePathSegment(...) (We don't want the Spring dependency) */
    private static byte[] encodeBytes(byte[] source) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(source.length);
        for (byte b : source) {
            if (b < 0) {
                b += 256;
            }
            if (isPchar(b)) {
                bos.write(b);
            }
            else {
                bos.write('%');
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
                bos.write(hex1);
                bos.write(hex2);
            }
        }
        return bos.toByteArray();
    }
}
