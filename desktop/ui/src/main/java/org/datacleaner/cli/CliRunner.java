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
package org.datacleaner.cli;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Resource;
import org.datacleaner.configuration.ConfigurationReaderInterceptor;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.configuration.DataCleanerConfigurationImpl;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.descriptors.AnalyzerDescriptor;
import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.FilterDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnalysisResultWriter;
import org.datacleaner.spark.SparkRunner;
import org.datacleaner.user.DesktopConfigurationReaderInterceptor;
import org.datacleaner.util.VFSUtils;
import org.datacleaner.util.convert.ResourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the execution logic to run a job from the command line.
 */
public final class CliRunner implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);

    private final CliArguments _arguments;
    private final Supplier<OutputStream> _outputStreamRef;
    private final Supplier<Writer> _writerRef;
    private final boolean _closeOut;

    /**
     * Alternative constructor that will specifically specifies the output
     * writer. Should be used only for testing, since normally the CliArguments
     * should be used to decide which outputwriter to use
     *
     * @param arguments
     * @param writer
     * @param outputStream
     */
    protected CliRunner(final CliArguments arguments, final Writer writer, final OutputStream outputStream) {
        _arguments = arguments;
        if (outputStream == null) {
            final String outputFilePath = arguments.getOutputFile();
            if (outputFilePath == null) {
                _outputStreamRef = null;
                _writerRef = new LazyRef<Writer>() {
                    @Override
                    protected Writer fetch() {
                        return new PrintWriter(System.out);
                    }
                };
            } else {
                if (_arguments.getRunType() == CliRunType.SPARK) {
                    _writerRef = null;
                    _outputStreamRef = null;
                } else {
                    final FileObject outputFile;
                    try {
                        outputFile = VFSUtils.getFileSystemManager().resolveFile(outputFilePath);
                    } catch (final FileSystemException e) {
                        throw new IllegalStateException(e);
                    }

                    _writerRef = new LazyRef<Writer>() {
                        @Override
                        protected Writer fetch() {
                            try {
                                final OutputStream out = outputFile.getContent().getOutputStream();
                                return new OutputStreamWriter(out, FileHelper.DEFAULT_ENCODING);
                            } catch (UnsupportedEncodingException | FileSystemException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    };
                    _outputStreamRef = new LazyRef<OutputStream>() {
                        @Override
                        protected OutputStream fetch() {
                            try {
                                return outputFile.getContent().getOutputStream();
                            } catch (final FileSystemException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    };
                }
            }
            _closeOut = true;
        } else {
            _writerRef = new ImmutableRef<>(writer);
            _outputStreamRef = new ImmutableRef<>(outputStream);
            _closeOut = false;
        }
    }

    public CliRunner(final CliArguments arguments) {
        this(arguments, null, null);
    }

    public void run() throws Throwable {
        final String configurationFilePath = _arguments.getConfigurationFile();
        final Resource configurationFile = resolveResource(configurationFilePath);
        final Resource propertiesResource;
        if (_arguments.getPropertiesFile() != null) {
            propertiesResource = resolveResource(_arguments.getPropertiesFile());
        } else {
            propertiesResource = null;
        }

        final ConfigurationReaderInterceptor configurationReaderInterceptor =
                new DesktopConfigurationReaderInterceptor(new File("."), propertiesResource);

        final InputStream inputStream = configurationFile.read();
        try {
            run(new JaxbConfigurationReader(configurationReaderInterceptor).create(inputStream));
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    private Resource resolveResource(final String path) {
        return new ResourceConverter(new DataCleanerConfigurationImpl()).fromString(Resource.class, path);
    }

    public void run(final DataCleanerConfiguration configuration) throws Throwable {
        final String jobFilePath = _arguments.getJobFile();
        final CliListType listType = _arguments.getListType();
        try {
            if (jobFilePath != null) {
                runJob(configuration);
            } else if (listType != null) {
                switch (listType) {
                case ANALYZERS:
                    printAnalyzers(configuration);
                    break;
                case TRANSFORMERS:
                    printTransformers(configuration);
                    break;
                case FILTERS:
                    printFilters(configuration);
                    break;
                case DATASTORES:
                    printDatastores(configuration);
                    break;
                case SCHEMAS:
                    printSchemas(configuration);
                    break;
                case TABLES:
                    printTables(configuration);
                    break;
                case COLUMNS:
                    printColumns(configuration);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown list type: " + listType);
                }
            } else {
                throw new IllegalArgumentException(
                        "Neither --job-file nor --list-type is specified. Try running with -usage to see usage help.");
            }
        } catch (final Exception e) {
            logger.error("Exception thrown in {}", e, this);
            System.err.println("Error:");
            e.printStackTrace(System.err);
        } finally {
            if (configuration != null) {
                configuration.getEnvironment().getTaskRunner().shutdown();
            }
        }
    }

    private void printColumns(final DataCleanerConfiguration configuration) {
        final String datastoreName = _arguments.getDatastoreName();
        final String tableName = _arguments.getTableName();
        final String schemaName = _arguments.getSchemaName();

        if (datastoreName == null) {
            System.err.println("You need to specify the datastore name!");
        } else if (tableName == null) {
            System.err.println("You need to specify a table name!");
        } else {
            final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            if (ds == null) {
                System.err.println("No such datastore: " + datastoreName);
            } else {
                final DatastoreConnection con = ds.openConnection();
                final DataContext dc = con.getDataContext();
                final Schema schema;
                if (schemaName == null) {
                    schema = dc.getDefaultSchema();
                } else {
                    schema = dc.getSchemaByName(schemaName);
                }
                if (schema == null) {
                    System.err.println("No such schema: " + schemaName);
                } else {
                    final Table table = schema.getTableByName(tableName);
                    if (table == null) {
                        write("No such table: " + tableName);
                    } else {
                        final List<String> columnNames = table.getColumnNames();
                        write("Columns:");
                        write("--------");
                        for (final String columnName : columnNames) {
                            write(columnName);
                        }
                    }
                }
                con.close();
            }
        }
    }

    private void printTables(final DataCleanerConfiguration configuration) {
        final String datastoreName = _arguments.getDatastoreName();
        final String schemaName = _arguments.getSchemaName();

        if (datastoreName == null) {
            System.err.println("You need to specify the datastore name!");
        } else {
            final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            if (ds == null) {
                System.err.println("No such datastore: " + datastoreName);
            } else {
                final DatastoreConnection con = ds.openConnection();
                final DataContext dc = con.getDataContext();
                final Schema schema;
                if (schemaName == null) {
                    schema = dc.getDefaultSchema();
                } else {
                    schema = dc.getSchemaByName(schemaName);
                }
                if (schema == null) {
                    System.err.println("No such schema: " + schemaName);
                } else {
                    final List<String> tableNames = schema.getTableNames();
                    if (tableNames == null || tableNames.isEmpty()) {
                        System.err.println("No tables in schema!");
                    } else {
                        write("Tables:");
                        write("-------");
                        for (final String tableName : tableNames) {
                            write(tableName);
                        }
                    }
                }
                con.close();
            }
        }
    }

    private void printSchemas(final DataCleanerConfiguration configuration) {
        final String datastoreName = _arguments.getDatastoreName();

        if (datastoreName == null) {
            System.err.println("You need to specify the datastore name!");
        } else {
            final Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            if (ds == null) {
                System.err.println("No such datastore: " + datastoreName);
            } else {
                final DatastoreConnection con = ds.openConnection();
                final List<String> schemaNames = con.getDataContext().getSchemaNames();
                if (schemaNames == null || schemaNames.isEmpty()) {
                    write("No schemas in datastore!");
                } else {
                    write("Schemas:");
                    write("--------");
                    for (final String schemaName : schemaNames) {
                        write(schemaName);
                    }
                }
                con.close();
            }
        }
    }

    private void printDatastores(final DataCleanerConfiguration configuration) {
        final String[] datastoreNames = configuration.getDatastoreCatalog().getDatastoreNames();
        if (datastoreNames == null || datastoreNames.length == 0) {
            write("No datastores configured!");
        } else {
            write("Datastores:");
            write("-----------");
            for (final String datastoreName : datastoreNames) {
                write(datastoreName);
            }
        }
    }

    protected void runJob(final DataCleanerConfiguration configuration) throws Throwable {
        if (_arguments.getRunType() == CliRunType.SPARK) {
            final SparkRunner sparkRunner = new SparkRunner(_arguments.getConfigurationFile(), _arguments.getJobFile(),
                    _arguments.getOutputFile());
            sparkRunner.runJob();
        } else {
            final JaxbJobReader jobReader = new JaxbJobReader(configuration);

            final String jobFilePath = _arguments.getJobFile();
            final Resource jobResource = resolveResource(jobFilePath);
            final Map<String, String> variableOverrides = _arguments.getVariableOverrides();

            final AnalysisJobBuilder analysisJobBuilder;
            final InputStream inputStream = jobResource.read();
            try {
                if (_arguments.getDatastoreName() != null) {
                    final Datastore datastore =
                            configuration.getDatastoreCatalog().getDatastore(_arguments.getDatastoreName());
                    if (datastore == null) {
                        throw new IllegalArgumentException("No such datastore: " + _arguments.getDatastoreName());
                    }
                    analysisJobBuilder = jobReader.create(inputStream, variableOverrides, datastore);
                } else {
                    analysisJobBuilder = jobReader.create(inputStream, variableOverrides);
                }
            } finally {
                FileHelper.safeClose(inputStream);
            }

            final AnalysisRunner runner = new AnalysisRunnerImpl(configuration, new CliProgressAnalysisListener());
            final AnalysisResultFuture resultFuture = runner.run(analysisJobBuilder.toAnalysisJob());

            resultFuture.await();

            if (resultFuture.isSuccessful()) {
                final CliOutputType outputType = _arguments.getOutputType();
                final AnalysisResultWriter writer = outputType.createWriter();
                writer.write(resultFuture, configuration, _writerRef, _outputStreamRef);
            } else {
                write("ERROR!");
                write("------");

                final List<Throwable> errors = resultFuture.getErrors();
                write(errors.size() + " error(s) occurred while executing the job:");

                for (final Throwable throwable : errors) {
                    write("------");
                    final StringWriter stringWriter = new StringWriter();
                    throwable.printStackTrace(new PrintWriter(stringWriter));
                    write(stringWriter.toString());
                }

                throw errors.get(0);
            }
        }
    }

    protected void printAnalyzers(final DataCleanerConfiguration configuration) {
        final Collection<AnalyzerDescriptor<?>> descriptors =
                configuration.getEnvironment().getDescriptorProvider().getAnalyzerDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            write("No analyzers configured!");
        } else {
            write("Analyzers:");
            write("----------");
            printBeanDescriptors(descriptors);
        }
    }

    private void printTransformers(final DataCleanerConfiguration configuration) {
        final Collection<TransformerDescriptor<?>> descriptors =
                configuration.getEnvironment().getDescriptorProvider().getTransformerDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            write("No transformers configured!");
        } else {
            write("Transformers:");
            write("-------------");
            printBeanDescriptors(descriptors);
        }
    }

    private void printFilters(final DataCleanerConfiguration configuration) {
        final Collection<FilterDescriptor<?, ?>> descriptors =
                configuration.getEnvironment().getDescriptorProvider().getFilterDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            write("No filters configured!");
        } else {
            write("Filters:");
            write("--------");
            printBeanDescriptors(descriptors);
        }
    }

    protected void printBeanDescriptors(final Collection<? extends ComponentDescriptor<?>> descriptors) {
        logger.debug("Printing {} descriptors", descriptors.size());
        for (final ComponentDescriptor<?> descriptor : descriptors) {
            write("name: " + descriptor.getDisplayName());

            final Set<ConfiguredPropertyDescriptor> propertiesForInput = descriptor.getConfiguredPropertiesForInput();
            if (propertiesForInput.size() == 1) {
                final ConfiguredPropertyDescriptor propertyForInput = propertiesForInput.iterator().next();
                if (propertyForInput != null) {
                    if (propertyForInput.isArray()) {
                        write(" - Consumes multiple input columns (type: " + propertyForInput.getTypeArgument(0)
                                .getSimpleName() + ")");
                    } else {
                        write(" - Consumes a single input column (type: " + propertyForInput.getTypeArgument(0)
                                .getSimpleName() + ")");
                    }
                }
            } else {
                write(" - Consumes " + propertiesForInput.size() + " named inputs");
                for (final ConfiguredPropertyDescriptor propertyForInput : propertiesForInput) {
                    if (propertyForInput.isArray()) {
                        write("   Input columns: " + propertyForInput.getName() + " (type: " + propertyForInput
                                .getTypeArgument(0).getSimpleName() + ")");
                    } else {
                        write("   Input column: " + propertyForInput.getName() + " (type: " + propertyForInput
                                .getTypeArgument(0).getSimpleName() + ")");
                    }
                }
            }

            final Set<ConfiguredPropertyDescriptor> properties = descriptor.getConfiguredProperties();
            for (final ConfiguredPropertyDescriptor property : properties) {
                if (!property.isInputColumn()) {
                    write(" - Property: name=" + property.getName() + ", type=" + property.getBaseType().getSimpleName()
                            + ", required=" + property.isRequired());
                }
            }

            if (descriptor instanceof FilterDescriptor<?, ?>) {
                final Set<String> categoryNames = ((FilterDescriptor<?, ?>) descriptor).getOutcomeCategoryNames();
                for (final String categoryName : categoryNames) {
                    write(" - Outcome: " + categoryName);
                }
            }
        }
    }

    private void write(final String str) {
        try {
            _writerRef.get().write(str + "\n");
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        if (_closeOut) {
            close(_writerRef);
            close(_outputStreamRef);
        }
    }

    private void close(final Supplier<?> ref) {
        if (ref != null) {
            if (ref instanceof LazyRef) {
                final LazyRef<?> lazyRef = (LazyRef<?>) ref;
                if (lazyRef.isFetched()) {
                    FileHelper.safeClose(ref.get());
                }
            } else {
                FileHelper.safeClose(ref.get());
            }
        }
    }
}
