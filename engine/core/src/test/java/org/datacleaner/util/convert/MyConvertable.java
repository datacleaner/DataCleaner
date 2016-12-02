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
package org.datacleaner.util.convert;

import org.datacleaner.api.Convertable;
import org.datacleaner.api.Converter;

@Convertable(MyConvertable.DefaultConverter.class)
public class MyConvertable {

    public static class DefaultConverter implements Converter<MyConvertable> {

        @Override
        public MyConvertable fromString(final Class<?> type, final String serializedForm) {
            final String[] tokens = serializedForm.split(":");
            final MyConvertable instance = new MyConvertable();
            instance.setName(tokens[0]);
            instance.setDescription(tokens[1]);
            return instance;
        }

        @Override
        public String toString(final MyConvertable instance) {
            return instance.getName() + ":" + instance.getDescription();
        }

        @Override
        public boolean isConvertable(final Class<?> type) {
            return type == MyConvertable.class;
        }
    }

    public static class SecondaryConverter implements Converter<MyConvertable> {

        @Override
        public MyConvertable fromString(final Class<?> type, final String serializedForm) {
            final String[] tokens = serializedForm.split("\\|");
            final MyConvertable instance = new MyConvertable();
            instance.setName(tokens[0]);
            instance.setDescription(tokens[1]);
            return instance;
        }

        @Override
        public String toString(final MyConvertable instance) {
            return instance.getName() + "|" + instance.getDescription();
        }

        @Override
        public boolean isConvertable(final Class<?> type) {
            return type == MyConvertable.class;
        }
    }

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
