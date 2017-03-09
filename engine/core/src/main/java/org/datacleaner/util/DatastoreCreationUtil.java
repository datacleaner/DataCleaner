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
import org.apache.metamodel.util.Resource;
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

public class DatastoreCreationUtil {

    public enum FileDatastoreEnum {
        CSV("csv", "tsv", "txt", "dat"), EXCEL("xls", "xlsx"), ACCESS("mdb"),

        // TODO: Add .sas7bdat file support
        SAS(), DBASE("dbf"), XML("xml"), JSON("json"), OPENOFFICE("odb");

        private final List<String> _extensions;

        FileDatastoreEnum(final String... extensions) {
            _extensions = Arrays.asList(extensions);
        }

        protected static FileDatastoreEnum getDatastoreTypeFromResource(final Resource resource) {
            final String extension = FilenameUtils.getExtension(resource.getName());

            for (final FileDatastoreEnum datastoreType : EnumSet.allOf(FileDatastoreEnum.class)) {
                if (datastoreType._extensions.contains(extension.toLowerCase())) {
                    return datastoreType;
                }
            }

            return null;
        }
    }

    public static FileDatastoreEnum inferDatastoreTypeFromResource(final Resource resource) {
        if (resource instanceof FileResource) {
            final FileResource fileResource = (FileResource) resource;
            final File file = fileResource.getFile();
            if (file.isDirectory()) {
                return FileDatastoreEnum.SAS;
            }
        }

        return FileDatastoreEnum.getDatastoreTypeFromResource(resource);
    }

    public static Datastore createUniqueDatastoreFromResource(final DatastoreCatalog catalog,
            final Resource resource) {
        String name = resource.getName();
        if (catalog.containsDatastore(name)) {
            final String originalName = name;
            int prefix = 1;
            do {
                name = originalName + "_" + prefix++;
            } while (catalog.containsDatastore(name));
        }
        return createDatastoreFromResource(resource, name);
    }

    public static Datastore createDatastoreFromResource(final Resource resource, final String datastoreName) {
        return createDatastoreFromEnum(inferDatastoreTypeFromResource(resource), resource, datastoreName);
    }

    public static Datastore createDatastoreFromEnum(final FileDatastoreEnum fileDatastore, final Resource resource,
            final String datastoreName) {
        if (fileDatastore == null) {
            throw new IllegalArgumentException("Unrecognized file type for: " + resource.getQualifiedPath());
        }

        switch (fileDatastore) {
        case CSV:
            final CsvConfigurationDetection detection = new CsvConfigurationDetection(resource);
            final CsvConfiguration csvConfiguration = detection.suggestCsvConfiguration();
            return new CsvDatastore(datastoreName, resource, csvConfiguration);
        case EXCEL:
            return new ExcelDatastore(datastoreName, resource, resource.getQualifiedPath());
        case ACCESS:
            return new AccessDatastore(datastoreName, resource.getQualifiedPath());
        case SAS:
            final FileResource fileResource = (FileResource) resource;
            return new SasDatastore(datastoreName, fileResource.getFile());
        case DBASE:
            return new DbaseDatastore(datastoreName, resource.getQualifiedPath());
        case JSON:
            return new JsonDatastore(datastoreName, resource);
        case OPENOFFICE:
            return new OdbDatastore(datastoreName, resource.getQualifiedPath());
        case XML:
            return new XmlDatastore(datastoreName, resource.getQualifiedPath());
        default:
            throw new IllegalArgumentException("No such datastore type");
        }
    }
}
