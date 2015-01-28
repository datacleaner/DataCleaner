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
package org.datacleaner.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.metamodel.schema.Column;
import org.datacleaner.api.ComponentCategory;
import org.datacleaner.api.ComponentSuperCategory;
import org.datacleaner.api.InputColumn;
import org.datacleaner.components.categories.WriteDataCategory;
import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CassandraDatastore;
import org.datacleaner.connection.CompositeDatastore;
import org.datacleaner.connection.CouchDbDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ElasticSearchDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.FixedWidthDatastore;
import org.datacleaner.connection.HBaseDatastore;
import org.datacleaner.connection.JdbcDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.MongoDbDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.SalesforceDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.SugarCrmDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.database.DatabaseDriverCatalog;
import org.datacleaner.database.DatabaseDriverDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;

/**
 * Contains utility methods concerned with icons, primarily datastore and
 * component icons.
 */
public final class IconUtils {

    public static int ICON_SIZE_LARGE = 32;
    public static int ICON_SIZE_MEDIUM = 22;
    public static int ICON_SIZE_SMALL = 16;

    public static final String MODEL_SCHEMA = "images/model/schema.png";
    public static final String MODEL_SCHEMA_INFORMATION = "images/model/schema_information.png";
    public static final String MODEL_TABLE = "images/model/table.png";
    public static final String MODEL_COLUMN = "images/model/column.png";
    public static final String MODEL_COLUMN_KEY = "images/model/column_primary_key.png";
    public static final String MODEL_COLUMN_EXPRESSION = "images/model/column_expression.png";
    public static final String MODEL_QUERY = "images/model/query.png";
    public static final String MODEL_ROW = "images/model/row.png";
    public static final String MODEL_JOB = "images/filetypes/analysis_job.png";
    public static final String MODEL_RESULT = "images/model/result.png";
    public static final String MODEL_SOURCE = "images/model/source.png";
    public static final String MODEL_METADATA = "images/model/metadata.png";

    public static final String MENU_OPEN = "images/menu/open.png";
    public static final String MENU_SAVE = "images/menu/save.png";
    public static final String MENU_NEW = "images/menu/new.png";
    public static final String MENU_EXECUTE = "images/menu/execute.png";
    public static final String MENU_OPTIONS = "images/menu/options.png";
    public static final String MENU_DQ_MONITOR = "images/menu/dq_monitor.png";

    public static final String ACTION_EXECUTE = "images/actions/execute.png";
    public static final String ACTION_EDIT = "images/actions/edit.png";
    public static final String ACTION_SAVE = "images/actions/save.png";
    public static final String ACTION_CLOSE_BRIGHT = "images/actions/close_bright.png";
    public static final String ACTION_CLOSE_DARK = "images/actions/close_dark.png";
    public static final String ACTION_CANCEL = "images/actions/cancel.png";
    public static final String ACTION_ADD = "images/actions/add.png";
    public static final String ACTION_REMOVE = "images/actions/remove.png";
    public static final String ACTION_RESET = "images/actions/reset.png";
    public static final String ACTION_RENAME = "images/actions/rename.png";
    public static final String ACTION_REFRESH = "images/actions/refresh.png";
    public static final String ACTION_PREVIEW = "images/actions/preview_data.png";
    public static final String ACTION_BROWSE = "images/actions/browse.png";
    public static final String ACTION_HELP = "images/actions/help.png";
    public static final String ACTION_BACK = "images/actions/back.png";
    public static final String ACTION_FORWARD = "images/actions/forward.png";
    public static final String ACTION_SCROLLDOWN_DARK = "images/actions/scrolldown_dark.png";
    public static final String ACTION_SCROLLDOWN_BRIGHT = "images/actions/scrolldown_bright.png";
    public static final String ACTION_STOP = "images/actions/stop.png";
    public static final String ACTION_LOG = "images/actions/log.png";
    public static final String ACTION_DRILL_TO_DETAIL = "images/actions/drill-to-detail.png";

    public static final String WEBSITE = "images/actions/website.png";

    // TODO: 'valid.png' needs a visual update
    public static final String STATUS_VALID = "images/status/valid.png";
    public static final String STATUS_INFO = "images/status/info.png";
    public static final String STATUS_WARNING = "images/status/warning.png";
    public static final String STATUS_ERROR = "images/status/error.png";

    public static final String ACTION_REORDER_COLUMNS = "images/actions/reorder-columns.png";

    public static final String CHART_BAR = "images/chart-types/bar.png";
    public static final String CHART_LINE = "images/chart-types/line.png";
    public static final String CHART_SCATTER = "images/chart-types/scatter.png";

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
    public static final String SALESFORCE_IMAGEPATH = "images/datastore-types/salesforce.png";
    public static final String SUGAR_CRM_IMAGEPATH = "images/datastore-types/sugarcrm.png";
    public static final String JSON_IMAGEPATH = "images/datastore-types/json.png";
    public static final String HBASE_IMAGEPATH = "images/datastore-types/hbase.png";
    public static final String CASSANDRA_IMAGEPATH = "images/datastore-types/cassandra.png";
    public static final String ELASTICSEARCH_IMAGEPATH = "images/datastore-types/elasticsearch.png";

    public static final String TRANSFORMER_IMAGEPATH = "images/component-types/transformer.png";
    public static final String ANALYZER_IMAGEPATH = "images/component-types/analyzer.png";
    public static final String FILTER_IMAGEPATH = "images/component-types/filter.png";
    public static final String FILTER_OUTCOME_PATH = "images/component-types/filter-outcome.png";

    public static final String FILE_FOLDER = "images/filetypes/folder.png";
    public static final String FILE_ARCHIVE = "images/filetypes/archive.png";
    public static final String FILE_FILE = "images/filetypes/file.png";
    public static final String FILE_DESKTOP_FOLDER = "images/filetypes/desktop-folder.png";
    public static final String FILE_HOME_FOLDER = "images/filetypes/home-folder.png";
    public static final String FILE_HIDDEN_FOLDER = "images/filetypes/hidden-folder.png";
    public static final String FILE_SEARCH = "images/filetypes/search-folder.png";

    public static final String COMPONENT_TYPE_WRITE_DATA = "images/component-types/type_output_writer.png";

    private static final ImageManager _imageManager = ImageManager.get();

    private IconUtils() {
        // prevent instantiation
    }

    public static Icon getDescriptorIcon(ComponentDescriptor<?> descriptor, boolean configured, int iconWidth) {
        final ImageIcon descriptorIcon = getDescriptorIcon(descriptor, iconWidth);
        if (configured) {
            return descriptorIcon;
        }

        // add a small error symbol to unconfigured components
        final int offset = 4;
        final int decorationSize = iconWidth / 2;
        final Image errorImage = _imageManager.getImage(STATUS_ERROR, decorationSize);
        final BufferedImage bufferedImage = new BufferedImage(iconWidth + offset, iconWidth + offset,
                BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getGraphics().drawImage(descriptorIcon.getImage(), offset, 0, null);
        bufferedImage.getGraphics().drawImage(errorImage, 0, iconWidth + offset - decorationSize, null);
        return new ImageIcon(bufferedImage);
    }

    public static ImageIcon getDescriptorIcon(ComponentDescriptor<?> descriptor, int newWidth) {
        final ClassLoader classLoader = descriptor.getComponentClass().getClassLoader();
        String imagePath = getDescriptorImagePath(descriptor, classLoader);
        return _imageManager.getImageIcon(imagePath, newWidth, classLoader);
    }

    public static ImageIcon getDescriptorIcon(ComponentDescriptor<?> descriptor) {
        return getDescriptorIcon(descriptor, ICON_SIZE_MEDIUM);
    }

    public static ImageIcon getDatastoreIcon(Datastore datastore, int newWidth) {
        final String imagePath = getDatastoreImagePath(datastore, true);
        return _imageManager.getImageIcon(imagePath, newWidth);
    }

    public static ImageIcon getDatastoreIcon(Datastore datastore) {
        final String imagePath = getDatastoreImagePath(datastore, true);
        return _imageManager.getImageIcon(imagePath);
    }

    public static ImageIcon getComponentSuperCategoryIcon(ComponentSuperCategory superCategory) {
        final Class<? extends ComponentSuperCategory> superCategoryClass = superCategory.getClass();
        return getCategoryIcon(superCategoryClass, false);
    }

    public static ImageIcon getComponentCategoryIcon(ComponentCategory category) {
        final Class<? extends ComponentCategory> categoryClass = category.getClass();
        return getCategoryIcon(categoryClass, true);
    }

    private static ImageIcon getCategoryIcon(Class<?> cls, boolean decorateWithFolder) {
        final String bundledIconPath = getImagePathForClass(cls);

        final int totalSize = ICON_SIZE_MEDIUM;

        if (!decorateWithFolder && bundledIconPath != null) {
            return _imageManager.getImageIcon(bundledIconPath, totalSize);
        }

        final Image decoration;
        final int decorationSize = ICON_SIZE_SMALL;
        if (bundledIconPath == null) {
            decoration = null;
        } else {
            final ClassLoader classLoader = cls.getClassLoader();
            decoration = _imageManager.getImage(bundledIconPath, decorationSize, classLoader);
        }

        final Image folderIcon = _imageManager.getImage(FILE_FOLDER, totalSize);

        if (decoration == null) {
            return new ImageIcon(folderIcon);
        }

        final BufferedImage bufferedImage = new BufferedImage(totalSize, totalSize, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getGraphics().drawImage(folderIcon, 0, 0, null);
        bufferedImage.getGraphics().drawImage(decoration, totalSize - decorationSize, totalSize - decorationSize, null);
        return new ImageIcon(bufferedImage);
    }

    public static Icon getDatastoreSpecificAnalysisJobIcon(Datastore datastore) {
        final int decorationSize = ICON_SIZE_MEDIUM;
        final int totalWidth = ICON_SIZE_LARGE;
        final int totalHeight = ICON_SIZE_LARGE + 6;

        final Image datastoreIcon;
        if (datastore == null) {
            datastoreIcon = _imageManager.getImage(IconUtils.GENERIC_DATASTORE_IMAGEPATH, decorationSize);
        } else {
            final String datastoreImagePath = getDatastoreImagePath(datastore, false);
            datastoreIcon = _imageManager.getImage(datastoreImagePath, decorationSize);
        }

        final Image jobIcon = _imageManager.getImage(MODEL_JOB, decorationSize);

        final BufferedImage bufferedImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getGraphics().drawImage(jobIcon, 0, 0, null);
        bufferedImage.getGraphics().drawImage(datastoreIcon, totalWidth - decorationSize, totalHeight - decorationSize,
                null);
        return new ImageIcon(bufferedImage);
    }

    public static String getImagePathForClass(Class<?> cls) {
        return getImagePathForClass(cls, cls.getClassLoader());
    }

    public static String getImagePathForClass(Class<?> cls, ClassLoader classLoader) {
        final String iconPath = cls.getName().replaceAll("\\.", "/") + ".png";
        final URL url = ResourceManager.get().getUrl(iconPath, classLoader);
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

        if (!descriptor.getComponentClass().getPackage().getName().startsWith("org.datacleaner")) {
            // plugins get a special icon
            return "images/component-types/plugin.png";
        }

        final ComponentDescriptor<?> beanDescriptor = (ComponentDescriptor<?>) descriptor;
        final Set<ComponentCategory> categories = beanDescriptor.getComponentCategories();
        if (categories.contains(new WriteDataCategory())) {
            return "images/component-types/type_output_writer.png";
        }

        final String displayName = beanDescriptor.getDisplayName().toLowerCase();

        String imagePath;
        if (descriptor instanceof TransformerDescriptor<?>) {
            imagePath = TRANSFORMER_IMAGEPATH;
        } else if (descriptor instanceof FilterDescriptor<?, ?>) {
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
        return _imageManager.getImageIcon(MODEL_COLUMN, IconUtils.ICON_SIZE_SMALL);
    }

    public static Icon getColumnIcon(Column column, int iconSize) {
        if (column.isPrimaryKey()) {
            return _imageManager.getImageIcon(MODEL_COLUMN_KEY, iconSize);
        }
        return _imageManager.getImageIcon(MODEL_COLUMN, iconSize);
    }

    protected static String getDatastoreImagePath(Datastore datastore, boolean considerOrderdbSpecialization) {
        String imagePath = GENERIC_DATASTORE_IMAGEPATH;
        if (datastore == null) {
            return imagePath;
        } else if (datastore instanceof JdbcDatastore) {
            JdbcDatastore jdbcDatastore = (JdbcDatastore) datastore;
            if (considerOrderdbSpecialization
                    && "jdbc:hsqldb:res:orderdb;readonly=true".equals(jdbcDatastore.getJdbcUrl())) {
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
        } else if (datastore instanceof HBaseDatastore) {
            imagePath = HBASE_IMAGEPATH;
        } else if (datastore instanceof ElasticSearchDatastore) {
            imagePath = ELASTICSEARCH_IMAGEPATH;
        } else if (datastore instanceof CassandraDatastore) {
            imagePath = CASSANDRA_IMAGEPATH;
        } else if (datastore instanceof JsonDatastore) {
            imagePath = JSON_IMAGEPATH;
        } else if (datastore instanceof CouchDbDatastore) {
            imagePath = COUCHDB_IMAGEPATH;
        } else if (datastore instanceof MongoDbDatastore) {
            imagePath = MONGODB_IMAGEPATH;
        } else if (datastore instanceof SalesforceDatastore) {
            imagePath = SALESFORCE_IMAGEPATH;
        } else if (datastore instanceof SugarCrmDatastore) {
            imagePath = SUGAR_CRM_IMAGEPATH;
        } else if (datastore instanceof CompositeDatastore) {
            imagePath = COMPOSITE_IMAGEPATH;
        }
        return imagePath;
    }
}
