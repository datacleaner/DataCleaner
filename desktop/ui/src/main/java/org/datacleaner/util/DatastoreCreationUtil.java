package org.datacleaner.util;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.metamodel.util.FileResource;
import org.datacleaner.connection.AccessDatastore;
import org.datacleaner.connection.CsvDatastore;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DbaseDatastore;
import org.datacleaner.connection.ExcelDatastore;
import org.datacleaner.connection.JsonDatastore;
import org.datacleaner.connection.OdbDatastore;
import org.datacleaner.connection.SasDatastore;
import org.datacleaner.connection.XmlDatastore;
import org.datacleaner.user.MutableDatastoreCatalog;

public class DatastoreCreationUtil {
    public enum FileDatastoreEnum {
        CSV("csv", "tsv", "txt", "dat"), EXCEL("xls", "xlsx"), ACCESS("mdb"), SAS(), DBASE("dbf"), XML("xml"), JSON(
                "json"), OPENOFFICE("odb");

        private List<String> _extensions;

        private FileDatastoreEnum(String... extensions) {
            _extensions = Arrays.asList(extensions);
        }

        protected static FileDatastoreEnum getDatastoreTypeFromFile(File file) {
            final String extension = FilenameUtils.getExtension(file.getName());

            for (FileDatastoreEnum datastoreType : EnumSet.allOf(FileDatastoreEnum.class)) {
                if (datastoreType._extensions.contains(extension)) {
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

    public static Datastore createAndAddUniqueDatastoreFromFile(MutableDatastoreCatalog catalog, File file) {
        String name = file.getName();
        if (catalog.containsDatastore(name)) {
            final String originalName = name;
            int prefix = 1;
            do {
                name = originalName + "_" + prefix++;
            } while (catalog.containsDatastore(name));
        }
        Datastore datastore = createDatastoreFromFile(file, name);
        catalog.addDatastore(datastore);
        return datastore;
    }

    public static Datastore createDatastoreFromFile(File file, String datastoreName) {
        return createDatastoreFromEnum(inferDatastoreTypeFromFile(file), file, datastoreName);
    }

    public static Datastore createDatastoreFromEnum(FileDatastoreEnum fileDatastore, File file, String datastoreName) {
        String filename = file.getAbsolutePath();

        switch (fileDatastore) {
        case CSV:
            return new CsvDatastore(datastoreName, new FileResource(file));
        case EXCEL:
            return new ExcelDatastore(datastoreName, new FileResource(filename), filename);
        case ACCESS:
            return new AccessDatastore(datastoreName, filename);
        case SAS:
            return new SasDatastore(datastoreName, file);
        case DBASE:
            return new DbaseDatastore(datastoreName, filename);
        case JSON:
            return new JsonDatastore(datastoreName, new FileResource(file));
        case OPENOFFICE:
            return new OdbDatastore(datastoreName, filename);
        case XML:
            return new XmlDatastore(datastoreName, filename);
        }

        throw new IllegalArgumentException("No such datastore type");
    }
}
