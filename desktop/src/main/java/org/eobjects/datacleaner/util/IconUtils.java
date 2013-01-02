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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.eobjects.analyzer.beans.api.ComponentCategory;
import org.eobjects.analyzer.beans.writers.WriteDataCategory;
import org.eobjects.analyzer.connection.AccessDatastore;
import org.eobjects.analyzer.connection.CompositeDatastore;
import org.eobjects.analyzer.connection.CouchDbDatastore;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DbaseDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.connection.FixedWidthDatastore;
import org.eobjects.analyzer.connection.JdbcDatastore;
import org.eobjects.analyzer.connection.MongoDbDatastore;
import org.eobjects.analyzer.connection.OdbDatastore;
import org.eobjects.analyzer.connection.SasDatastore;
import org.eobjects.analyzer.connection.XmlDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.descriptors.BeanDescriptor;
import org.eobjects.analyzer.descriptors.ComponentDescriptor;
import org.eobjects.analyzer.descriptors.FilterBeanDescriptor;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.database.DatabaseDriverCatalog;
import org.eobjects.datacleaner.database.DatabaseDriverDescriptor;
import org.eobjects.metamodel.schema.Column;

/**
 * Contains utility methods concerned with icons, primarily datastore and
 * component icons.
 * 
 * @author Kasper Sørensen
 */
public final class IconUtils {

    public static int ICON_SIZE_LARGE = 32;
    public static int ICON_SIZE_MEDIUM = 22;
    public static int ICON_SIZE_SMALL = 16;

    public static final String MENU_OPTIONS = "images/menu/options.png";
    public static final String MENU_DQ_MONITOR = "images/menu/dq_monitor.png";

    public static final String ACTION_ADD = "images/actions/add.png";
    public static final String ACTION_REMOVE = "images/actions/remove.png";

    public static final String STATUS_INFO = "images/status/info.png";
    public static final String STATUS_WARNING = "images/status/warning.png";
    public static final String STATUS_ERROR = "images/status/error.png";
    public static final String STATUS_VALID = "images/status/valid.png";

    public static final String BUTTON_EXPRESSION_COLUMN_IMAGEPATH = "images/model/column_expression.png";
    public static final String BUTTON_REORDER_COLUMN_IMAGEPATH = "images/actions/reorder-columns.png";

    public static final String DICTIONARY_IMAGEPATH = "images/model/dictionary.png";
    public static final String DICTIONARY_SIMPLE_IMAGEPATH = "images/model/dictionary_simple.png";
    public static final String DICTIONARY_TEXTFILE_IMAGEPATH = "images/model/dictionary_textfile.png";
    public static final String DICTIONARY_DATASTORE_IMAGEPATH = "images/model/dictionary_datastore.png";

    public static final String SYNONYM_CATALOG_IMAGEPATH = "images/model/synonym.png";
    public static final String SYNONYM_CATALOG_TEXTFILE_IMAGEPATH = "images/model/synonym_textfile.png";
    public static final String SYNONYM_CATALOG_DATASTORE_IMAGEPATH = "images/model/synonym_datastore.png";

    public static final String STRING_PATTERN_IMAGEPATH = "images/model/stringpattern.png";
    public static final String STRING_PATTERN_SIMPLE_IMAGEPATH = "images/model/stringpattern_simple.png";
    public static final String STRING_PATTERN_REGEX_IMAGEPATH = "images/model/stringpattern_regex.png";
    public static final String STRING_PATTERN_REGEXSWAP_IMAGEPATH = "images/model/stringpattern_regexswap.png";

    public static final String GENERIC_DATASTORE_IMAGEPATH = "images/model/datastore.png";
    public static final String CSV_IMAGEPATH = "images/datastore-types/csv.png";
    public static final String XML_IMAGEPATH = "images/datastore-types/xml.png";
    public static final String EXCEL_IMAGEPATH = "images/datastore-types/excel.png";
    public static final String ACCESS_IMAGEPATH = "images/datastore-types/access.png";
    public static final String DBASE_IMAGEPATH = "images/datastore-types/dbase.png";
    public static final String SAS_IMAGEPATH = "images/datastore-types/sas.png";
    public static final String FIXEDWIDTH_IMAGEPATH = "images/datastore-types/fixedwidth.png";
    public static final String ODB_IMAGEPATH = "images/datastore-types/odb.png";
    public static final String COMPOSITE_IMAGEPATH = "images/datastore-types/composite.png";
    public static final String MONGODB_IMAGEPATH = "images/datastore-types/mongodb.png";
    public static final String COUCHDB_IMAGEPATH = "images/datastore-types/couchdb.png";

    public static final String TRANSFORMER_IMAGEPATH = "images/component-types/transformer.png";
    public static final String ANALYZER_IMAGEPATH = "images/component-types/analyzer.png";
    public static final String FILTER_IMAGEPATH = "images/component-types/filter.png";

    private static final ImageManager _imageManager = ImageManager.getInstance();

    private IconUtils() {
        // prevent instantiation
    }

    public static Icon getDescriptorIcon(ComponentDescriptor<?> descriptor, int newWidth) {
        final ClassLoader classLoader = descriptor.getComponentClass().getClassLoader();
        String imagePath = getDescriptorImagePath(descriptor, classLoader);
        return _imageManager.getImageIcon(imagePath, newWidth, classLoader);
    }

    public static Icon getDescriptorIcon(ComponentDescriptor<?> descriptor) {
        return getDescriptorIcon(descriptor, ICON_SIZE_MEDIUM);
    }

    public static Icon getDatastoreIcon(Datastore datastore, int newWidth) {
        String imagePath = getDatastoreImagePath(datastore);
        return _imageManager.getImageIcon(imagePath, newWidth);
    }

    public static Icon getDatastoreIcon(Datastore datastore) {
        String imagePath = getDatastoreImagePath(datastore);
        return _imageManager.getImageIcon(imagePath);
    }

    public static Icon getComponentCategoryIcon(ComponentCategory category) {
        Class<? extends ComponentCategory> categoryClass = category.getClass();

        final String bundledIconPath = getImagePathForClass(categoryClass);

        final int totalSize = ICON_SIZE_MEDIUM;
        final Image decoration;
        final int decorationSize = ICON_SIZE_SMALL;
        if (bundledIconPath == null) {
            decoration = null;
        } else {
            final ClassLoader classLoader = categoryClass.getClassLoader();
            decoration = _imageManager.getImage(bundledIconPath, decorationSize, classLoader);
        }

        final Image folderIcon = _imageManager.getImage("images/filetypes/folder.png", totalSize);

        if (decoration == null) {
            return new ImageIcon(folderIcon);
        }

        final BufferedImage bufferedImage = new BufferedImage(totalSize, totalSize, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getGraphics().drawImage(folderIcon, 0, 0, null);
        bufferedImage.getGraphics().drawImage(decoration, totalSize - decorationSize, totalSize - decorationSize, null);
        return new ImageIcon(bufferedImage);
    }

    public static String getImagePathForClass(Class<?> cls) {
        return getImagePathForClass(cls, cls.getClassLoader());
    }

    public static String getImagePathForClass(Class<?> cls, ClassLoader classLoader) {
        final String iconPath = cls.getName().replaceAll("\\.", "/") + ".png";
        final URL url = ResourceManager.getInstance().getUrl(iconPath, classLoader);
        if (url == null) {
            return null;
        }
        return iconPath;
    }

    protected static String getDescriptorImagePath(ComponentDescriptor<?> descriptor, ClassLoader classLoader) {
        final Class<?> componentClass = descriptor.getComponentClass();
        final String bundledIconPath = getImagePathForClass(componentClass, classLoader);
        if (bundledIconPath != null) {
            return bundledIconPath;
        }

        if (!descriptor.getComponentClass().getPackage().getName().startsWith("org.eobjects")) {
            // plugins get a special icon
            return "images/component-types/plugin.png";
        }

        final String displayName;

        if (descriptor instanceof BeanDescriptor) {
            BeanDescriptor<?> beanDescriptor = (BeanDescriptor<?>) descriptor;
            Set<ComponentCategory> categories = beanDescriptor.getComponentCategories();
            displayName = beanDescriptor.getDisplayName().toLowerCase();
            if (categories.contains(new WriteDataCategory())) {
                return "images/component-types/type_output_writer.png";
            }
        } else {
            displayName = "";
        }

        String imagePath;
        if (descriptor instanceof TransformerBeanDescriptor<?>) {
            imagePath = TRANSFORMER_IMAGEPATH;
        } else if (descriptor instanceof FilterBeanDescriptor<?, ?>) {
            imagePath = FILTER_IMAGEPATH;
        } else {
            imagePath = ANALYZER_IMAGEPATH;
        }

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
        if (displayName.indexOf("string") != -1 || displayName.indexOf("word") != -1
                || displayName.indexOf("token") != -1 || displayName.indexOf("whitespace") != -1) {
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
        if (displayName.indexOf("java") != -1) {
            imagePath = "images/component-types/type_java.png";
        }
        if (displayName.indexOf("javascript") != -1) {
            imagePath = "images/component-types/type_javascript.png";
        }
        if (displayName.indexOf("xml") != -1) {
            imagePath = "images/component-types/type_xml.png";
        }

        // some individual icons
        if (displayName.equals("concatenator")) {
            imagePath = "images/component-types/type_concatenator.png";
        }
        if (displayName.equals("date to age")) {
            imagePath = "images/component-types/type_date_to_age.png";
        }
        if (displayName.equals("convert to string")) {
            imagePath = "images/component-types/type_convert_to_string.png";
        }
        if (displayName.equals("date gap analyzer")) {
            imagePath = "images/component-types/type_date_gap_analyzer.png";
        }
        if (displayName.equals("pattern finder")) {
            imagePath = "images/component-types/type_pattern_finder.png";
        }
        if (displayName.equals("date/time analyzer")) {
            imagePath = "images/component-types/type_date_time_analyzer.png";
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

    public static Icon getColumnIcon(InputColumn<?> column, int iconSize) {
        if (column.isPhysicalColumn()) {
            return getColumnIcon(column.getPhysicalColumn(), iconSize);
        }
        return _imageManager.getImageIcon("images/model/column.png", IconUtils.ICON_SIZE_SMALL);
    }

    public static Icon getColumnIcon(Column column, int iconSize) {
        if (column.isPrimaryKey()) {
            return _imageManager.getImageIcon("images/model/column_primary_key.png", iconSize);
        }
        return _imageManager.getImageIcon("images/model/column.png", iconSize);
    }

    protected static String getDatastoreImagePath(Datastore datastore) {
        String imagePath = GENERIC_DATASTORE_IMAGEPATH;
        if (datastore == null) {
            return imagePath;
        } else if (datastore instanceof JdbcDatastore) {
            JdbcDatastore jdbcDatastore = (JdbcDatastore) datastore;
            if ("jdbc:hsqldb:res:orderdb;readonly=true".equals(jdbcDatastore.getJdbcUrl())) {
                imagePath = "images/datastore-types/orderdb.png";
            } else {
                String driverClass = jdbcDatastore.getDriverClass();
                if (!StringUtils.isNullOrEmpty(driverClass)) {
                    DatabaseDriverDescriptor driver = DatabaseDriverCatalog
                            .getDatabaseDriverByDriverClassName(driverClass);
                    if (driver != null) {
                        imagePath = driver.getIconImagePath();
                    }
                }
            }
        } else if (datastore instanceof CsvDatastore) {
            imagePath = CSV_IMAGEPATH;
        } else if (datastore instanceof ExcelDatastore) {
            imagePath = EXCEL_IMAGEPATH;
        } else if (datastore instanceof AccessDatastore) {
            imagePath = ACCESS_IMAGEPATH;
        } else if (datastore instanceof OdbDatastore) {
            imagePath = ODB_IMAGEPATH;
        } else if (datastore instanceof FixedWidthDatastore) {
            imagePath = FIXEDWIDTH_IMAGEPATH;
        } else if (datastore instanceof DbaseDatastore) {
            imagePath = DBASE_IMAGEPATH;
        } else if (datastore instanceof SasDatastore) {
            imagePath = SAS_IMAGEPATH;
        } else if (datastore instanceof XmlDatastore) {
            imagePath = XML_IMAGEPATH;
        } else if (datastore instanceof CouchDbDatastore) {
            imagePath = COUCHDB_IMAGEPATH;
        } else if (datastore instanceof MongoDbDatastore) {
            imagePath = MONGODB_IMAGEPATH;
        } else if (datastore instanceof CompositeDatastore) {
            imagePath = COMPOSITE_IMAGEPATH;
        }
        return imagePath;
    }
}
