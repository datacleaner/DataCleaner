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
package org.datacleaner.beans.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Converter;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.api.Validate;
import org.datacleaner.components.categories.TextCategory;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.job.builder.ComponentBuilder;
import org.datacleaner.util.convert.MapStringToStringConverter;
import org.datacleaner.util.convert.SerializationStringEscaper;
import org.datacleaner.util.convert.StringConverter;

import com.google.common.base.Strings;

@Named("Plain search/replace")
@Description("Search and replace text in String values.")
@Categorized(TextCategory.class)
public class PlainSearchReplaceTransformer implements Transformer {

    private static final String REPLACEMENTS_PROPERTY_NAME = "Replacements";

    private static final String REPLACEMENT_STRING_PROPERTY_NAME = "Replacement string";

    private static final String SEARCH_STRING_PROPERTY_NAME = "Search string";

    private static final String CASE_SENSITIVE_PROPERTY_NAME = "Case sensitive";

    @Configured(value = "Value", order = 1)
    InputColumn<String> valueColumn;

    @Configured(order = 2)
    @Description("A mapping of strings to do replacements with.")
    Map<String, String> replacements;

    @Configured(order = 3)
    @Description("Replace the entire string when the search string is found.")
    boolean replaceEntireString = false;

    @Configured(value = CASE_SENSITIVE_PROPERTY_NAME, order = 4)
    @Description("Case sensitivity mode. ")
    boolean caseSensitive = true;

    public static void processRemovedProperties(final ComponentBuilder builder, final StringConverter stringConverter,
            final ComponentDescriptor<?> descriptor, final Map<String, String> removedProperties) {
        if (removedProperties.containsKey(SEARCH_STRING_PROPERTY_NAME) && removedProperties
                .containsKey(REPLACEMENT_STRING_PROPERTY_NAME)) {
            final ConfiguredPropertyDescriptor configuredProperty =
                    descriptor.getConfiguredProperty(REPLACEMENTS_PROPERTY_NAME);

            final Converter<?> customConverter = configuredProperty.createCustomConverter();

            final Map<String, String> replacements = new HashMap<>();
            replacements.put(SerializationStringEscaper.unescape(removedProperties.get(SEARCH_STRING_PROPERTY_NAME)),
                    SerializationStringEscaper.unescape(removedProperties.get(REPLACEMENT_STRING_PROPERTY_NAME)));

            final Object value = stringConverter
                    .deserialize(new MapStringToStringConverter().toString(replacements), configuredProperty.getType(),
                            customConverter);

            builder.setConfiguredProperty(configuredProperty, value);
        }
    }

    public static boolean isRemovedProperty(final ComponentDescriptor<?> descriptor, final String name) {
        return (SEARCH_STRING_PROPERTY_NAME.equals(name) || REPLACEMENT_STRING_PROPERTY_NAME.equals(name));
    }

    @Validate
    public void validate() {
        for (final Entry<String, String> entry : replacements.entrySet()) {
            final String searchString = entry.getKey();
            if (Strings.isNullOrEmpty(searchString)) {
                throw new IllegalArgumentException("Search string cannot be empty");
            }
            final String replacementString = entry.getValue();
            if (!replaceEntireString && replacementString.indexOf(searchString) != -1) {
                throw new IllegalArgumentException(
                        "Replacement string cannot contain the search string (implies an infinite replacement loop)");
            }
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(String.class, valueColumn.getName() + " (search/replaced)");
    }

    @Override
    public String[] transform(final InputRow row) {
        final String[] result = new String[1];
        final String value = row.getValue(valueColumn);

        if (value != null) {
            result[0] = replaceEntireString ? replaceEntireString(value) : replaceFirstOccurrence(value);
        }

        return result;
    }

    private String replaceEntireString(final String value) {
        for (final Entry<String, String> entry : replacements.entrySet()) {
            final String replacementString = entry.getValue();
            final String searchString = entry.getKey();

            if (caseSensitive) {
                if (value.contains(searchString)) {
                    return replacementString;
                }
            } else if (value.toLowerCase().contains(searchString.toLowerCase())) {
                return replacementString;
            }
        }

        return value;
    }

    private String replaceFirstOccurrence(final String value) {
        String result = value;

        for (final Entry<String, String> entry : replacements.entrySet()) {
            final String replacementString = entry.getValue();
            final String searchString = entry.getKey();
            final Pattern pattern = caseSensitive ? Pattern.compile(searchString)
                    : Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
            result = pattern.matcher(result).replaceAll(replacementString);
        }

        return result;
    }

    public void setReplacements(final Map<String, String> replacements) {
        this.replacements = replacements;
    }

    public void setReplaceEntireString(final boolean replaceEntireString) {
        this.replaceEntireString = replaceEntireString;
    }

}
