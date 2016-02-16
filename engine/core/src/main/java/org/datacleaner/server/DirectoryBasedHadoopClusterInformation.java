package org.datacleaner.server;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import com.google.common.base.Joiner;

/**
 * Environment based configuration
 */
public class DirectoryBasedHadoopClusterInformation extends AbstractServerInformation
        implements HadoopClusterInformation {
    private final String[] _paths;

    public DirectoryBasedHadoopClusterInformation(final String name, final String description, String ... paths) {
        super(name, description);
        _paths = paths;
    }

    @Override
    public Configuration getConfiguration() {
        final Configuration configuration = new Configuration(true);
        final Map<String, File> configurationFiles = new HashMap<>();

        Arrays.stream(_paths).map(File::new).filter(File::isDirectory).forEach(c -> {
            final File[] array = c.listFiles();
            assert (array != null);
            Arrays.stream(array).filter(File::isFile).filter(f -> !configurationFiles.containsKey(f.getName()))
                    .forEach(f -> configurationFiles.put(f.getName(), f));
        });

        if (configurationFiles.size() == 0) {
            throw new IllegalStateException(
                    "Specified directories down not contain any Hadoop configuration files");
        }

        for (File file : configurationFiles.values()) {
            configuration.addResource(new Path(file.toURI()));
        }

        configuration.reloadConfiguration();

        return configuration;
    }
}
