/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.util.convert;

import org.eobjects.analyzer.beans.api.Convertable;
import org.eobjects.analyzer.beans.api.Converter;

@Convertable(MyConvertable.DefaultConverter.class)
public class MyConvertable {

	public static class DefaultConverter implements Converter<MyConvertable> {

		@Override
		public MyConvertable fromString(Class<?> type, String serializedForm) {
			String[] tokens = serializedForm.split(":");
			MyConvertable instance = new MyConvertable();
			instance.setName(tokens[0]);
			instance.setDescription(tokens[1]);
			return instance;
		}

		@Override
		public String toString(MyConvertable instance) {
			return instance.getName() + ":" + instance.getDescription();
		}

		@Override
		public boolean isConvertable(Class<?> type) {
			return type == MyConvertable.class;
		}
	}
	
	public static class SecondaryConverter implements Converter<MyConvertable> {

		@Override
		public MyConvertable fromString(Class<?> type, String serializedForm) {
			String[] tokens = serializedForm.split("\\|");
			MyConvertable instance = new MyConvertable();
			instance.setName(tokens[0]);
			instance.setDescription(tokens[1]);
			return instance;
		}

		@Override
		public String toString(MyConvertable instance) {
			return instance.getName() + "|" + instance.getDescription();
		}

		@Override
		public boolean isConvertable(Class<?> type) {
			return type == MyConvertable.class;
		}
	}

	private String name;
	private String description;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
