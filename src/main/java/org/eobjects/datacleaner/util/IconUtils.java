package org.eobjects.datacleaner.util;

import javax.swing.Icon;

import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;

public final class IconUtils {

	public static int ICON_SIZE_LARGE = 32;
	public static int ICON_SIZE_SMALL = 20;

	public static final String GENERIC_DATASTORE_IMAGEPATH = "images/model/datastore.png";
	public static final String CSV_IMAGEPATH = "images/datastore-types/csv.png";
	public static final String EXCEL_IMAGEPATH = "images/datastore-types/excel.png";
	public static final String ACCESS_IMAGEPATH = "images/datastore-types/access.png";
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
		String imagePath;
		if (descriptor instanceof TransformerBeanDescriptor<?>) {
			imagePath = TRANSFORMER_IMAGEPATH;
		} else if (descriptor instanceof FilterBeanDescriptor<?, ?>) {
			imagePath = FILTER_IMAGEPATH;
		} else {
			imagePath = ANALYZER_IMAGEPATH;
		}
		String displayName = descriptor.getDisplayName().toLowerCase();
		if (displayName.indexOf("string") != -1 || displayName.indexOf("word") != -1) {
			imagePath = "images/component-types/type_string.png";
		}
		if (displayName.indexOf("time") != -1 || descriptor.getDisplayName().indexOf("date") != -1) {
			imagePath = "images/component-types/type_time.png";
		}
		if (displayName.indexOf("number") != -1) {
			imagePath = "images/component-types/type_number.png";
		}
		if (displayName.indexOf("compare") != -1) {
			imagePath = "images/component-types/type_compare.png";
		}
		if (displayName.indexOf("pattern") != -1 || displayName.indexOf("expression") != -1) {
			imagePath = "images/component-types/type_expression.png";
		}
		if (displayName.indexOf("distribution") != -1) {
			imagePath = "images/component-types/type_distribution.png";
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
		} else if (datastore instanceof CompositeDatastore) {
			imagePath = COMPOSITE_IMAGEPATH;
		}
		return imagePath;
	}
}
