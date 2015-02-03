package org.datacleaner.util;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

public class FiletypeUtil {
    public enum FileDatastoreEnum {
        CSV("csv", "tsv", "txt", "dat"), EXCEL("xls", "xlsx"), ACCESS("mdb"), SAS(), DBASE("dbf"), XML("xml"), JSON(
                "json"), OPENOFFICE("odb");

        private List<String> _extensions;

        private FileDatastoreEnum(String... extensions) {
            _extensions = Arrays.asList(extensions);
        }

        public static Set<FileDatastoreEnum> getResolversFromFile(File file) {
            final String extension = FilenameUtils.getExtension(file.getName());
            final Set<FileDatastoreEnum> resolvers = EnumSet.noneOf(FileDatastoreEnum.class);

            for (FileDatastoreEnum resolver : EnumSet.allOf(FileDatastoreEnum.class)) {
                if (resolver._extensions.contains(extension)) {
                    resolvers.add(resolver);
                }
            }

            return resolvers;
        }
    }

    public Set<FileDatastoreEnum> inferDatastoreTypeFromFile(File file) {
        if (file.isDirectory()) {
            return EnumSet.of(FileDatastoreEnum.SAS);
        }

        return FileDatastoreEnum.getResolversFromFile(file);
    }

    public Datastore createDatastoreFromEnum(FileDatastoreEnum fileDatastore, File file) {
        String filename = file.getName();
        switch (fileDatastore) {
        case CSV:
            return new CsvDatastore(filename, new FileResource(file));
        case EXCEL:
            return new ExcelDatastore(filename, new FileResource(filename), filename);
        case ACCESS:
            return new AccessDatastore(filename, filename);
        case SAS:
            return new SasDatastore(filename, file);
        case DBASE:
            return new DbaseDatastore(filename, filename);
        case JSON:
            return new JsonDatastore(filename, new FileResource(file));
        case OPENOFFICE:
            return new OdbDatastore(filename, filename);
        case XML:
            return new XmlDatastore(filename, filename); 
        }
        
        throw new IllegalArgumentException("No such datastore type");
        
    }
}
