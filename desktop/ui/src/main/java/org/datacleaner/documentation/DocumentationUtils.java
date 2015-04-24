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
package org.datacleaner.documentation;

import org.datacleaner.api.Description;
import org.datacleaner.util.StringUtils;

import com.google.common.base.Strings;

public class DocumentationUtils {

    /**
     * Formats a string (typically a component {@link Description}) into an
     * appropriate set of HTML paragraphs ('p' tag).
     * 
     * @param str
     * @return
     */
    public static String createHtmlParagraphs(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return "";
        }
        str = "<p>" + str + "</p>";
        str = StringUtils.replaceAll(str, "\n\n", "\n");
        str = StringUtils.replaceAll(str, "\n", "</p><p>");
        return str;
    }
}
