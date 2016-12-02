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
package org.datacleaner.spark.utils;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.deploy.SparkHadoopUtil;
import org.datacleaner.util.HadoopResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * Helper class for interacting with HDFS.
 */
public class HdfsHelper {

    private static final Logger logger = LoggerFactory.getLogger(HdfsHelper.class);

    private static Configuration _lastKnownConfiguration;
    private final Configuration _hadoopConfiguration;

    public HdfsHelper(final JavaSparkContext sparkContext) {
        this(getHadoopConfigurationIfYarnMode(sparkContext));
    }

    public HdfsHelper(final Configuration configuration) {
        if (configuration == null) {
            logger.warn("Hadoop Configuration is null!", new Throwable());
        } else {
            _lastKnownConfiguration = configuration;
        }
        _hadoopConfiguration = configuration;
    }

    /**
     * Creates a {@link HdfsHelper} without any configuration or context
     * available. This is normally not the recommended way to obtain a
     * {@link HdfsHelper} but may be necessary in executor functions where the
     * {@link JavaSparkContext} is not in scope and not made available by Spark
     * (at least spark's end-user API).
     *
     * @return
     */
    public static HdfsHelper createHelper() {
        Configuration configuration = _lastKnownConfiguration;
        if (configuration == null) {
            try {
                final SparkHadoopUtil sparkHadoopUtil = SparkHadoopUtil.get();
                if (sparkHadoopUtil.isYarnMode()) {
                    configuration = sparkHadoopUtil.conf();
                }
            } catch (final Exception e) {
                // the above is developer API so we don't consider it very
                // stable.
            }
        }
        return new HdfsHelper(configuration);
    }

    private static Configuration getHadoopConfigurationIfYarnMode(final JavaSparkContext sparkContext) {
        final String sparkMaster = sparkContext.getConf().get("spark.master");
        if (Strings.isNullOrEmpty(sparkMaster) || "local".equals(sparkMaster)) {
            return null;
        }
        return sparkContext.hadoopConfiguration();
    }

    /**
     * Clears up the statically cached reference to a {@link Configuration} object, which is used an
     * HdfsHelper is instantiated without an explicit {@link Configuration} object.
     *
     * Note: Only use if you want to start fresh and make sure no lingering objects are used.
     */
    public static void clear() {
        _lastKnownConfiguration = null;
    }

    public String readFile(final URI filepath) {
        return readFile(filepath, false);
    }

    public String readFile(final URI filepath, final boolean failOnNoData) {
        final Resource resourceInUse = getResourceToUse(filepath);
        if (failOnNoData && resourceInUse == null) {
            throw new IllegalArgumentException("Could not resolve resource: " + filepath);
        }
        return readResource(resourceInUse);
    }

    public String readResource(final Resource resource) {
        final Resource resourceInUse = getResourceToUse(resource);
        if (resourceInUse == null) {
            return null;
        }
        return resourceInUse.read(in -> {
            return FileHelper.readInputStreamAsString(in, FileHelper.DEFAULT_ENCODING);
        });
    }

    public Resource getResourceToUse(final Resource resource) {
        if (resource == null) {
            return null;
        }
        if (_hadoopConfiguration == null || resource instanceof HadoopResource) {
            return resource;
        }
        if (resource instanceof HdfsResource) {
            // wrap the resource with our known configuration
            return new HadoopResource(resource, _hadoopConfiguration, HadoopResource.DEFAULT_CLUSTERREFERENCE);
        }
        if (resource instanceof FileResource) {
            // this may very well be a path that was mis-interpreted as a local
            // file because no scheme was defined
            if (resource.getQualifiedPath().startsWith("/")) {
                return new HadoopResource(resource, _hadoopConfiguration, HadoopResource.DEFAULT_CLUSTERREFERENCE);
            }
        }

        return resource;
    }

    public Resource getResourceToUse(final URI path) {
        if (path == null) {
            return null;
        }
        if (_hadoopConfiguration == null) {
            if ("hdfs".equals(path.getScheme())) {
                return new HdfsResource(path.toString());
            }
            return new FileResource(path.toString());
        }
        return new HadoopResource(path, _hadoopConfiguration, HadoopResource.DEFAULT_CLUSTERREFERENCE);
    }

    public boolean isDirectory(final URI path) {
        final Resource resource = getResourceToUse(path);
        if (!resource.isExists()) {
            return false;
        }
        if (resource instanceof FileResource) {
            return ((FileResource) resource).getFile().isDirectory();
        }
        if (resource instanceof HdfsResource) {
            final FileSystem fileSystem = ((HdfsResource) resource).getHadoopFileSystem();
            final Path hadoopPath = ((HdfsResource) resource).getHadoopPath();
            try {
                return fileSystem.isDirectory(hadoopPath);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        // actually we don't know, but most likely it's not a directory
        return false;
    }
}
