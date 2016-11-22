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
package org.datacleaner.api;

import java.io.Serializable;

/**
 * Message that can be used to report a component usage (by the component). Consumer of this message type can
 * use the information provided to gather usage statistics/metering etc.
 */
public class UsageMeteringMessage implements Serializable, ComponentMessage {

    public static final char DETAILS_SEPARATOR_CHAR = ',';
    public static final char DETAILS_QUOTE_CHAR = '"';
    public static final char DETAILS_ESCAPE_CHAR = '\\';
    private static final long serialVersionUID = 1L;
    private final String type;
    private final String details;

    /**
     * A simple metering message constructor - having only 'type' with no additional details.
     */
    public UsageMeteringMessage(final String type) {
        this.type = type;
        details = "";
    }

    /**
     * Constructor for a metering message with more details.
     * The details items will be concatenated according to a CSV format and available with @{link #getDetails} method
     */
    public UsageMeteringMessage(final String type, final String... details) {
        this.type = type;
        final StringBuilder detailsBldr = new StringBuilder();
        for (int i = 0; i < details.length; i++) {
            if (i > 0) {
                detailsBldr.append(DETAILS_SEPARATOR_CHAR);
            }
            escapeValueTo(details[i], detailsBldr);
        }
        this.details = detailsBldr.toString();
    }

    /**
     * Returns a basic type of the message. To be used as a basic differentiator
     * for usage statistics by the consuming system.
     */
    public String getType() {
        return type;
    }

    /**
     * Additional info, which sematic is defined by the message producer
     * and should be understandable by consumer.
     * Contains a CSV-formatted line with detail fields.
     */
    public String getDetails() {
        return details;
    }

    public String toString() {
        return "UsageMeteringMessage[" + type + " " + details + "]";
    }

    /** Escapes value for a CSV line and appends it to the 'target'. */
    private void escapeValueTo(final String field, final StringBuilder target) {
        if (field == null) {
            target.append(DETAILS_QUOTE_CHAR);
            target.append(DETAILS_QUOTE_CHAR);
            return;
        }
        target.append(DETAILS_QUOTE_CHAR);
        if (field.indexOf(DETAILS_ESCAPE_CHAR) == -1 && field.indexOf(DETAILS_QUOTE_CHAR) == -1) {
            target.append(field);
        } else {
            final int len = field.length();
            for (int i = 0; i < len; i++) {
                final char charAt = field.charAt(i);
                if (charAt == DETAILS_ESCAPE_CHAR || charAt == DETAILS_QUOTE_CHAR) {
                    target.append(DETAILS_ESCAPE_CHAR);
                }
                target.append(charAt);
            }
        }
        target.append(DETAILS_QUOTE_CHAR);
    }

}
