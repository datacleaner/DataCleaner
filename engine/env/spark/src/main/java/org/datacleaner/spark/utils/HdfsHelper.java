package org.datacleaner.spark.utils;

import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.FileResource;
import org.apache.metamodel.util.Func;
import org.apache.metamodel.util.HdfsResource;
import org.apache.metamodel.util.Resource;
import org.apache.spark.api.java.JavaSparkContext;
import org.datacleaner.util.HadoopResource;

import com.google.common.base.Strings;

public class HdfsHelper {

    private final Configuration _hadoopConfiguration;

    public HdfsHelper(JavaSparkContext sparkContext) {
        this(sparkContext.hadoopConfiguration());
    }

    public HdfsHelper(Configuration configuration) {
        _hadoopConfiguration = configuration;
    }

    public String readFile(String filepath) {
        final Resource resourceInUse = getResourceToUse(filepath);
        return readResource(resourceInUse);
    }

    public String readResource(Resource resource) {
        final Resource resourceInUse = getResourceToUse(resource);
        if (resourceInUse == null) {
            return null;
        }
        return resourceInUse.read(new Func<InputStream, String>() {
            @Override
            public String eval(InputStream in) {
                return FileHelper.readInputStreamAsString(in, FileHelper.DEFAULT_ENCODING);
            }
        });
    }

    public Resource getResourceToUse(Resource resource) {
        if (resource == null) {
            return null;
        }
        if (_hadoopConfiguration == null || resource instanceof HadoopResource) {
            return resource;
        }
        if (resource instanceof HdfsResource) {
            // wrap the resource with our known configuration
            return new HadoopResource(resource.getQualifiedPath(), _hadoopConfiguration);
        }
        if (resource instanceof FileResource) {
            // this may very well be a path that was mis-interpreted as a local
            // file because no scheme was defined
            if (resource.getQualifiedPath().startsWith("/")) {
                return new HadoopResource(resource.getQualifiedPath(), _hadoopConfiguration);
            }
        }

        return resource;
    }

    public Resource getResourceToUse(String path) {
        if (Strings.isNullOrEmpty(path)) {
            return null;
        }
        if (_hadoopConfiguration == null) {
            if (path.toLowerCase().startsWith("hdfs:")) {
                return new HdfsResource(path);
            }
            return new FileResource(path);
        }
        return new HadoopResource(path, _hadoopConfiguration);
    }
}
