/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import javax.swing.Icon;

import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.datacleaner.output.beans.OutputWriterAnalyzer;

/**
 * Contains utility methods concerned with icons, primarily datastore and
 * component icons.
 * 
 * @author Kasper Sørensen
 */
public final class IconUtils {

	public static int ICON_SIZE_LARGE = 32;
	public static int ICON_SIZE_SMALL = 20;
	
	public static final String GENERIC_DATASTORE_IMAGEPATH = "images/model/datastore.png";
	public static final String CSV_IMAGEPATH = "images/datastore-types/csv.png";
	public static final String XML_IMAGEPATH = "images/datastore-types/xml.png";
	public static final String EXCEL_IMAGEPATH = "images/datastore-types/excel.png";
	public static final String ACCESS_IMAGEPATH = "images/datastore-types/access.png";
	public static final String DBASE_IMAGEPATH = "images/datastore-types/dbase.png";
	public static final String ODB_IMAGEPATH = "images/datastore-types/odb.png";
	public static final String COMPOSITE_IMAGEPATH = "images/datastore-types/composite.png";

	public static final String TRANSFORMER_IMAGEPATH = "images/component-types/transformer.png";
	public static final String ANALYZER_IMAGEPATH = "images/component-types/analyzer.png";
	public static final String FILTER_IMAGEPATH = "images/component-types/filter.png";

	private static final ImageManager imageManager = ImageManager.getInstance();

	private IconUtils() {
		// prevent instantiation
	}

	public static Icon getDescriptorIcon(BeanDescriptor<?> descriptor, int newWidth) {
		String imagePath = getDescriptorImagePath(descriptor);

		return imageManager.getImageIcon(imagePath, newWidth);
	}

	public static Icon getDatastoreIcon(Datastore datastore, int newWidth) {
		String imagePath = getDatastoreImagePath(datastore);

		return imageManager.getImageIcon(imagePath, newWidth);
	}

	public static Icon getDescriptorIcon(BeanDescriptor<?> descriptor) {
		String imagePath = getDescriptorImagePath(descriptor);

		return imageManager.getImageIcon(imagePath);
	}

	public static Icon getDatastoreIcon(Datastore datastore) {
		String imagePath = getDatastoreImagePath(datastore);

		return imageManager.getImageIcon(imagePath);
	}

	protected static String getDescriptorImagePath(BeanDescriptor<?> descriptor) {
		if (!descriptor.getComponentClass().getPackage().getName().startsWith("org.eobjects")) {
			// plugins get a special icon
			return "images/component-types/plugin.png";
		}

		if (descriptor.getAnnotation(OutputWriterAnalyzer.class) != null) {
			return "images/component-types/type_output_writer.png";
		}

		String imagePath;
		if (descriptor instanceof TransformerBeanDescriptor<?>) {
			imagePath = TRANSFORMER_IMAGEPATH;
		} else if (descriptor instanceof FilterBeanDescriptor<?, ?>) {
			imagePath = FILTER_IMAGEPATH;
		} else {
			imagePath = ANALYZER_IMAGEPATH;
		}

		String displayName = descriptor.getDisplayName().toLowerCase();
		if (displayName.indexOf("boolean") != -1) {
			imagePath = "images/component-types/type_boolean.png";
		}
		if (displayName.indexOf("validat") != -1) {
			imagePath = "images/component-types/type_validate.png";
		}
		if (displayName.indexOf("internet") != -1 || displayName.indexOf("url") != -1) {
			imagePath = "images/component-types/type_internet.png";
		}
		if (displayName.indexOf("identity") != -1 || displayName.indexOf("name") != -1) {
			imagePath = "images/component-types/type_identity.png";
		}
		if (displayName.indexOf("string") != -1 || displayName.indexOf("word") != -1 || displayName.indexOf("token") != -1
				|| displayName.indexOf("whitespace") != -1) {
			imagePath = "images/component-types/type_string.png";
		}
		if (displayName.indexOf("time") != -1 || displayName.indexOf("date") != -1) {
			imagePath = "images/component-types/type_time.png";
		}
		if (displayName.indexOf("number") != -1) {
			imagePath = "images/component-types/type_number.png";
		}
		if (displayName.indexOf("convert") != -1) {
			imagePath = "images/component-types/type_convert.png";
		}
		if (displayName.indexOf("length") != -1) {
			imagePath = "images/component-types/type_length.png";
		}
		if (displayName.indexOf("email") != -1) {
			imagePath = "images/component-types/type_email.png";
		}
		if (displayName.indexOf("compare") != -1) {
			imagePath = "images/component-types/type_compare.png";
		}
		if (displayName.indexOf("sound") != -1 || displayName.indexOf("phonetic") != -1) {
			imagePath = "images/component-types/type_sound.png";
		}
		if (displayName.indexOf("pattern") != -1 || displayName.indexOf("expression") != -1
				|| displayName.indexOf("regex") != -1) {
			imagePath = "images/component-types/type_expression.png";
		}
		if (displayName.indexOf("dictionary") != -1) {
			imagePath = "images/component-types/type_dictionary.png";
		}
		if (displayName.indexOf("synonym") != -1) {
			imagePath = "images/component-types/type_synonym.png";
		}
		if (displayName.indexOf("match") != -1) {
			imagePath = "images/component-types/type_match.png";
		}
		if (displayName.indexOf("coalesce") != -1) {
			imagePath = "images/component-types/type_coalesce.png";
		}
		if (displayName.indexOf("expression language") != -1) {
			imagePath = "images/model/column_expression.png";
		}
		if (displayName.indexOf("javascript") != -1) {
			imagePath = "images/component-types/type_javascript.png";
		}

		// some individual icons
		if (displayName.equals("pattern finder")) {
			imagePath = "images/component-types/type_pattern_finder.png";
		}
		if (displayName.equals("string analyzer")) {
			imagePath = "images/component-types/type_string_analyzer.png";
		}
		if (displayName.equals("whitespace trimmer")) {
			imagePath = "images/component-types/type_whitespace_trimmer.png";
		}
		if (displayName.equals("value distribution")) {
			imagePath = "images/component-types/type_value_distribution.png";
		}
		return imagePath;
	}

	protected static String getDatastoreImagePath(Datastore datastore) {
		String imagePath = GENERIC_DATASTORE_IMAGEPATH;
		if (datastore instanceof CsvDatastore) {
			imagePath = CSV_IMAGEPATH;
		} else if (datastore instanceof ExcelDatastore) {
			imagePath = EXCEL_IMAGEPATH;
		} else if (datastore instanceof AccessDatastore) {
			imagePath = ACCESS_IMAGEPATH;
		} else if (datastore instanceof OdbDatastore) {
			imagePath = ODB_IMAGEPATH;
		} else if (datastore instanceof DbaseDatastore) {
			imagePath = DBASE_IMAGEPATH;
		} else if (datastore instanceof CompositeDatastore) {
			imagePath = COMPOSITE_IMAGEPATH;
		}
		return imagePath;
	}
}
