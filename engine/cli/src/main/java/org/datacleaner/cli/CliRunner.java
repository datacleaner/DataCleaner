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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.configuration.JaxbConfigurationReader;
import org.datacleaner.connection.Datastore;
import org.datacleaner.connection.DatastoreConnection;
import org.datacleaner.descriptors.AnalyzerBeanDescriptor;
import org.datacleaner.descriptors.BeanDescriptor;
import org.datacleaner.descriptors.ConfiguredPropertyDescriptor;
import org.datacleaner.descriptors.FilterBeanDescriptor;
import org.datacleaner.descriptors.TransformerBeanDescriptor;
import org.datacleaner.job.JaxbJobReader;
import org.datacleaner.job.builder.AnalysisJobBuilder;
import org.datacleaner.job.runner.AnalysisResultFuture;
import org.datacleaner.job.runner.AnalysisRunner;
import org.datacleaner.job.runner.AnalysisRunnerImpl;
import org.datacleaner.result.AnalysisResultWriter;
import org.datacleaner.util.VFSUtils;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.apache.metamodel.util.ImmutableRef;
import org.apache.metamodel.util.LazyRef;
import org.apache.metamodel.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the execution logic to run a job from the command line.
 */
public final class CliRunner implements Closeable {

    private final static Logger logger = LoggerFactory.getLogger(CliRunner.class);

    private final CliArguments _arguments;
    private final Ref<OutputStream> _outputStreamRef;
    private final Ref<Writer> _writerRef;
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
    protected CliRunner(CliArguments arguments, Writer writer, OutputStream outputStream) {
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

                final FileObject outputFile;
                try {
                    outputFile = VFSUtils.getFileSystemManager().resolveFile(outputFilePath);
                } catch (FileSystemException e) {
                    throw new IllegalStateException(e);
                }

                _writerRef = new LazyRef<Writer>() {
                    @Override
                    protected Writer fetch() {
                        try {
                            OutputStream out = outputFile.getContent().getOutputStream();
                            return new OutputStreamWriter(out, FileHelper.DEFAULT_ENCODING);
                        } catch (Exception e) {
                            if (e instanceof RuntimeException) {
                                throw (RuntimeException) e;
                            }
                            throw new IllegalStateException(e);
                        }
                    }
                };
                _outputStreamRef = new LazyRef<OutputStream>() {
                    @Override
                    protected OutputStream fetch() {
                        try {
                            return outputFile.getContent().getOutputStream();
                        } catch (FileSystemException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                };
            }
            _closeOut = true;
        } else {
            _writerRef = new ImmutableRef<Writer>(writer);
            _outputStreamRef = new ImmutableRef<OutputStream>(outputStream);
            _closeOut = false;
        }
    }

    public CliRunner(CliArguments arguments) {
        this(arguments, null, null);
    }

    public void run() throws Throwable {
        final String configurationFilePath = _arguments.getConfigurationFile();
        final FileObject configurationFile = VFSUtils.getFileSystemManager().resolveFile(configurationFilePath);
        final InputStream inputStream = configurationFile.getContent().getInputStream();
        try {
            run(new JaxbConfigurationReader().create(inputStream));
        } finally {
            FileHelper.safeClose(inputStream);
        }
    }

    public void run(AnalyzerBeansConfiguration configuration) throws Throwable {
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
        } catch (Exception e) {
            logger.error("Exception thrown in {}", e, this);
            System.err.println("Error:");
            e.printStackTrace(System.err);
        } finally {
            configuration.getTaskRunner().shutdown();
        }
    }

    private void printColumns(AnalyzerBeansConfiguration configuration) {
        String datastoreName = _arguments.getDatastoreName();
        String tableName = _arguments.getTableName();
        String schemaName = _arguments.getSchemaName();

        if (datastoreName == null) {
            System.err.println("You need to specify the datastore name!");
        } else if (tableName == null) {
            System.err.println("You need to specify a table name!");
        } else {
            Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            if (ds == null) {
                System.err.println("No such datastore: " + datastoreName);
            } else {
                DatastoreConnection con = ds.openConnection();
                DataContext dc = con.getDataContext();
                Schema schema;
                if (schemaName == null) {
                    schema = dc.getDefaultSchema();
                } else {
                    schema = dc.getSchemaByName(schemaName);
                }
                if (schema == null) {
                    System.err.println("No such schema: " + schemaName);
                } else {
                    Table table = schema.getTableByName(tableName);
                    if (table == null) {
                        write("No such table: " + tableName);
                    } else {
                        String[] columnNames = table.getColumnNames();
                        write("Columns:");
                        write("--------");
                        for (String columnName : columnNames) {
                            write(columnName);
                        }
                    }
                }
                con.close();
            }
        }
    }

    private void printTables(AnalyzerBeansConfiguration configuration) {
        String datastoreName = _arguments.getDatastoreName();
        String schemaName = _arguments.getSchemaName();

        if (datastoreName == null) {
            System.err.println("You need to specify the datastore name!");
        } else {
            Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            if (ds == null) {
                System.err.println("No such datastore: " + datastoreName);
            } else {
                DatastoreConnection con = ds.openConnection();
                DataContext dc = con.getDataContext();
                Schema schema;
                if (schemaName == null) {
                    schema = dc.getDefaultSchema();
                } else {
                    schema = dc.getSchemaByName(schemaName);
                }
                if (schema == null) {
                    System.err.println("No such schema: " + schemaName);
                } else {
                    String[] tableNames = schema.getTableNames();
                    if (tableNames == null || tableNames.length == 0) {
                        System.err.println("No tables in schema!");
                    } else {
                        write("Tables:");
                        write("-------");
                        for (String tableName : tableNames) {
                            write(tableName);
                        }
                    }
                }
                con.close();
            }
        }
    }

    private void printSchemas(AnalyzerBeansConfiguration configuration) {
        String datastoreName = _arguments.getDatastoreName();

        if (datastoreName == null) {
            System.err.println("You need to specify the datastore name!");
        } else {
            Datastore ds = configuration.getDatastoreCatalog().getDatastore(datastoreName);
            if (ds == null) {
                System.err.println("No such datastore: " + datastoreName);
            } else {
                DatastoreConnection con = ds.openConnection();
                String[] schemaNames = con.getDataContext().getSchemaNames();
                if (schemaNames == null || schemaNames.length == 0) {
                    write("No schemas in datastore!");
                } else {
                    write("Schemas:");
                    write("--------");
                    for (String schemaName : schemaNames) {
                        write(schemaName);
                    }
                }
                con.close();
            }
        }
    }

    private void printDatastores(AnalyzerBeansConfiguration configuration) {
        String[] datastoreNames = configuration.getDatastoreCatalog().getDatastoreNames();
        if (datastoreNames == null || datastoreNames.length == 0) {
            write("No datastores configured!");
        } else {
            write("Datastores:");
            write("-----------");
            for (String datastoreName : datastoreNames) {
                write(datastoreName);
            }
        }
    }

    protected void runJob(AnalyzerBeansConfiguration configuration) throws Throwable {
        final JaxbJobReader jobReader = new JaxbJobReader(configuration);

        final String jobFilePath = _arguments.getJobFile();
        final FileObject jobFile = VFS.getManager().resolveFile(jobFilePath);
        final Map<String, String> variableOverrides = _arguments.getVariableOverrides();

        final InputStream inputStream = jobFile.getContent().getInputStream();

        final AnalysisJobBuilder analysisJobBuilder;
        try {
            analysisJobBuilder = jobReader.create(inputStream, variableOverrides);
        } finally {
            FileHelper.safeClose(inputStream);
        }

        final AnalysisRunner runner = new AnalysisRunnerImpl(configuration, new CliProgressAnalysisListener());
        final AnalysisResultFuture resultFuture = runner.run(analysisJobBuilder.toAnalysisJob());

        resultFuture.await();

        if (resultFuture.isSuccessful()) {
            final CliOutputType outputType = _arguments.getOutputType();
            AnalysisResultWriter writer = outputType.createWriter();
            writer.write(resultFuture, configuration, _writerRef, _outputStreamRef);
        } else {
            write("ERROR!");
            write("------");

            List<Throwable> errors = resultFuture.getErrors();
            write(errors.size() + " error(s) occurred while executing the job:");

            for (Throwable throwable : errors) {
                write("------");
                StringWriter stringWriter = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stringWriter));
                write(stringWriter.toString());
            }

            throw errors.get(0);
        }
    }

    protected void printAnalyzers(AnalyzerBeansConfiguration configuration) {
        Collection<AnalyzerBeanDescriptor<?>> descriptors = configuration.getDescriptorProvider()
                .getAnalyzerBeanDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            write("No analyzers configured!");
        } else {
            write("Analyzers:");
            write("----------");
            printBeanDescriptors(descriptors);
        }
    }

    private void printTransformers(AnalyzerBeansConfiguration configuration) {
        Collection<TransformerBeanDescriptor<?>> descriptors = configuration.getDescriptorProvider()
                .getTransformerBeanDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            write("No transformers configured!");
        } else {
            write("Transformers:");
            write("-------------");
            printBeanDescriptors(descriptors);
        }
    }

    private void printFilters(AnalyzerBeansConfiguration configuration) {
        Collection<FilterBeanDescriptor<?, ?>> descriptors = configuration.getDescriptorProvider()
                .getFilterBeanDescriptors();
        if (descriptors == null || descriptors.isEmpty()) {
            write("No filters configured!");
        } else {
            write("Filters:");
            write("--------");
            printBeanDescriptors(descriptors);
        }
    }

    protected void printBeanDescriptors(Collection<? extends BeanDescriptor<?>> descriptors) {
        logger.debug("Printing {} descriptors", descriptors.size());
        for (BeanDescriptor<?> descriptor : descriptors) {
            write("name: " + descriptor.getDisplayName());

            Set<ConfiguredPropertyDescriptor> propertiesForInput = descriptor.getConfiguredPropertiesForInput();
            if (propertiesForInput.size() == 1) {
                ConfiguredPropertyDescriptor propertyForInput = propertiesForInput.iterator().next();
                if (propertyForInput != null) {
                    if (propertyForInput.isArray()) {
                        write(" - Consumes multiple input columns (type: "
                                + propertyForInput.getTypeArgument(0).getSimpleName() + ")");
                    } else {
                        write(" - Consumes a single input column (type: "
                                + propertyForInput.getTypeArgument(0).getSimpleName() + ")");
                    }
                }
            } else {
                write(" - Consumes " + propertiesForInput.size() + " named inputs");
                for (ConfiguredPropertyDescriptor propertyForInput : propertiesForInput) {
                    if (propertyForInput.isArray()) {
                        write("   Input columns: " + propertyForInput.getName() + " (type: "
                                + propertyForInput.getTypeArgument(0).getSimpleName() + ")");
                    } else {
                        write("   Input column: " + propertyForInput.getName() + " (type: "
                                + propertyForInput.getTypeArgument(0).getSimpleName() + ")");
                    }
                }
            }

            Set<ConfiguredPropertyDescriptor> properties = descriptor.getConfiguredProperties();
            for (ConfiguredPropertyDescriptor property : properties) {
                if (!property.isInputColumn()) {
                    write(" - Property: name=" + property.getName() + ", type="
                            + property.getBaseType().getSimpleName() + ", required=" + property.isRequired());
                }
            }

            if (descriptor instanceof TransformerBeanDescriptor<?>) {
                Class<?> dataType = ((TransformerBeanDescriptor<?>) descriptor).getOutputDataType();
                write(" - Output type is: " + dataType.getSimpleName());
            }

            if (descriptor instanceof FilterBeanDescriptor<?, ?>) {
                Set<String> categoryNames = ((FilterBeanDescriptor<?, ?>) descriptor).getOutcomeCategoryNames();
                for (String categoryName : categoryNames) {
                    write(" - Outcome category: " + categoryName);
                }
            }
        }
    }

    private void write(String str) {
        try {
            _writerRef.get().write(str + "\n");
        } catch (IOException e) {
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

    private void close(Ref<?> ref) {
        if (ref != null) {
            if (ref instanceof LazyRef) {
                LazyRef<?> lazyRef = (LazyRef<?>) ref;
                if (lazyRef.isFetched()) {
                    FileHelper.safeClose(ref.get());
                }
            } else {
                FileHelper.safeClose(ref.get());
            }
        }
    }
}
