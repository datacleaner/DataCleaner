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

import java.io.PrintWriter;
import java.util.Map;

import org.datacleaner.Version;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.base.Strings;

/**
 * Defines the Command-line arguments. These are populated by the CLI parser.
 */
public class CliArguments {

    private static final String[] USAGE_TOKENS = new String[] { "-usage", "--usage", "-help", "--help", "-?", "/?",
            "/help", "/usage" };

    private static final String[] VERSION_TOKENS = new String[] { "-version", "--version", "/version" };

    /**
     * Parses the CLI arguments and creates a CliArguments instance
     * 
     * @param args
     *            the arguments as a string array
     * @return
     * @throws CmdLineException
     */
    public static CliArguments parse(String[] args) {
        CliArguments arguments = new CliArguments();
        if (args != null) {
            CmdLineParser parser = new CmdLineParser(arguments);
            try {
                parser.parseArgument(args);
            } catch (CmdLineException e) {
                // ignore
            }

            arguments.usageMode = false;
            arguments.versionMode = false;

            for (String arg : args) {
                for (int i = 0; i < USAGE_TOKENS.length; i++) {
                    final String usageToken = USAGE_TOKENS[i];
                    if (usageToken.equalsIgnoreCase(arg)) {
                        arguments.usageMode = true;
                        break;
                    }
                }

                for (int i = 0; i < VERSION_TOKENS.length; i++) {
                    final String versionToken = VERSION_TOKENS[i];
                    if (versionToken.equalsIgnoreCase(arg)) {
                        arguments.versionMode = true;
                        break;
                    }
                }

            }
        }
        return arguments;
    }

    /**
     * Prints the usage information for the CLI
     * 
     * @param out
     */
    public static void printUsage(PrintWriter out) {
        CliArguments arguments = new CliArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        parser.setUsageWidth(120);
        parser.printUsage(out, null);
    }

    public static void printVersion(PrintWriter out) {
        final String distributionVersion = Version.getDistributionVersion();
        if (!Strings.isNullOrEmpty(distributionVersion)) {
            out.println("DataCleaner " + Version.getEdition() + " " + distributionVersion);
        } else {
            out.println("DataCleaner " + Version.getEdition());
        }

        out.println("Core version " + Version.getVersion());

        final String licenseKey = Version.getLicenseKey();
        if (!Strings.isNullOrEmpty(licenseKey)) {
            out.println("License no. " + licenseKey);
        }
    }

    @Option(name = "-conf", aliases = { "-configuration", "--configuration-file" }, metaVar = "PATH", usage = "Path to an XML file describing the configuration of DataCleaner")
    private String configurationFile;

    @Option(name = "-job", aliases = { "--job-file" }, metaVar = "PATH", usage = "Path to an analysis job XML file to execute")
    private String jobFile;

    @Option(name = "-properties", aliases = { "--properties-file" }, metaVar = "PATH", usage = "Path to a custom properties file")
    private String propertiesFile;

    @Option(name = "-list", usage = "Used to print a list of various elements available in the configuration")
    private CliListType listType;

    @Option(name = "-ds", aliases = { "-datastore", "--datastore-name" }, usage = "Name of datastore when printing a list of schemas, tables or columns")
    private String datastoreName;

    @Option(name = "-s", aliases = { "-schema", "--schema-name" }, usage = "Name of schema when printing a list of tables or columns")
    private String schemaName;

    @Option(name = "-t", aliases = { "-table", "--table-name" }, usage = "Name of table when printing a list of columns")
    private String tableName;

    @Option(name = "-ot", aliases = { "--output-type" }, usage = "How to represent the result of the job")
    private CliOutputType outputType;

    @Option(name = "-runtype", aliases = { "--runtype" }, usage = "How/where to run the job")
    private CliRunType runType;

    @Option(name = "-of", aliases = { "--output-file" }, metaVar = "PATH", usage = "Path to file in which to save the result of the job", required = false)
    private String outputFile;

    @Option(name = "-v", aliases = { "-var", "--variable" }, multiValued = true)
    private Map<String, String> variableOverrides;

    private boolean usageMode;
    private boolean versionMode;

    private CliArguments() {
        // instantiation only allowed by factory (parse(...)) method.
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public String getJobFile() {
        return jobFile;
    }

    public CliListType getListType() {
        return listType;
    }

    public String getDatastoreName() {
        return datastoreName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isUsageMode() {
        return usageMode;
    }

    public Map<String, String> getVariableOverrides() {
        return variableOverrides;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isVersionMode() {
        return versionMode;
    }
    
    public String getPropertiesFile() {
        return propertiesFile;
    }

    public CliOutputType getOutputType() {
        if (outputType == null) {
            return CliOutputType.TEXT;
        }
        return outputType;
    }

    public CliRunType getRunType() {
        if (runType == null){
            return CliRunType.LOCAL;
        }
        return runType;
    }

    /**
     * Gets whether the arguments have been sufficiently set to execute a CLI
     * task.
     * 
     * @return true if the CLI arguments have been sufficiently set.
     */
    public boolean isSet() {
        if (isUsageMode()) {
            return true;
        }
        if (isVersionMode()) {
            return true;
        }
        if (getJobFile() != null) {
            return true;
        }
        if (getListType() != null) {
            return true;
        }
        return false;
    }
}
