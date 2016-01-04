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
package org.datacleaner.descriptors;

import java.util.Objects;

/**
 * Represents the status/state of a {@link DescriptorProvider}.
 */
public class DescriptorProviderStatus {
    
    public enum Level {
        INFO, ERROR;
    };

    private final Level level;
    private final String message;

    public DescriptorProviderStatus(Level level, String message) {
        this.level = level;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DescriptorProviderStatus) {
            final DescriptorProviderStatus other = (DescriptorProviderStatus) o;
            return Objects.equals(this.level, other.level) && Objects.equals(this.message, other.message);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message);
    }
}
