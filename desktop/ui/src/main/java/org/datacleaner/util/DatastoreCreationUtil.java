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

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreCatalog;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.user.MutableDatastoreCatalog;

public class DatastoreCreationUtil {

    public enum FileDatastoreEnum {
        CSV("csv", "tsv", "txt", "dat"), EXCEL("xls", "xlsx"), ACCESS("mdb"),

        // TODO: Add .sas7bdat file support
        SAS(), DBASE("dbf"), XML("xml"), JSON("json"), OPENOFFICE("odb");

        private final List<String> _extensions;

        private FileDatastoreEnum(String... extensions) {
            _extensions = Arrays.asList(extensions);
        }

        protected static FileDatastoreEnum getDatastoreTypeFromFile(File file) {
            final String extension = FilenameUtils.getExtension(file.getName());

            for (FileDatastoreEnum datastoreType : EnumSet.allOf(FileDatastoreEnum.class)) {
                if (datastoreType._extensions.contains(extension.toLowerCase())) {
                    return datastoreType;
                }
            }

            return null;
        }
    }

    public static FileDatastoreEnum inferDatastoreTypeFromFile(File file) {
        if (file.isDirectory()) {
            return FileDatastoreEnum.SAS;
        }

        return FileDatastoreEnum.getDatastoreTypeFromFile(file);
    }

    public static Datastore createAndAddUniqueDatastoreFromFile(DatastoreCatalog catalog, File file) {
        String name = file.getName();
        if (catalog.containsDatastore(name)) {
            final String originalName = name;
            int prefix = 1;
            do {
                name = originalName + "_" + prefix++;
            } while (catalog.containsDatastore(name));
        }
        Datastore datastore = createDatastoreFromFile(file, name);
        if (catalog instanceof MutableDatastoreCatalog) {
            ((MutableDatastoreCatalog) catalog).addDatastore(datastore);
        }
        return datastore;
    }

    public static Datastore createDatastoreFromFile(File file, String datastoreName) {
        return createDatastoreFromEnum(inferDatastoreTypeFromFile(file), file, datastoreName);
    }

    public static Datastore createDatastoreFromEnum(FileDatastoreEnum fileDatastore, File file, String datastoreName) {
        final String filename = file.getAbsolutePath();
        if (fileDatastore == null) {
            throw new IllegalArgumentException("Illegal file type for: " + filename);
        }
        final FileResource resource = new FileResource(file);

        
        switch (fileDatastore) {
        case CSV:
            final CsvConfigurationDetection detection = new CsvConfigurationDetection(resource);
            final CsvConfiguration csvConfiguration = detection.suggestCsvConfiguration();
            return new CsvDatastore(datastoreName, resource, csvConfiguration);
        case EXCEL:
            return new ExcelDatastore(datastoreName, new FileResource(filename), filename);
        case ACCESS:
            return new AccessDatastore(datastoreName, filename);
        case SAS:
            return new SasDatastore(datastoreName, file);
        case DBASE:
            return new DbaseDatastore(datastoreName, filename);
        case JSON:
            return new JsonDatastore(datastoreName, resource);
        case OPENOFFICE:
            return new OdbDatastore(datastoreName, filename);
        case XML:
            return new XmlDatastore(datastoreName, filename);
        }

        throw new IllegalArgumentException("No such datastore type");
    }
}
