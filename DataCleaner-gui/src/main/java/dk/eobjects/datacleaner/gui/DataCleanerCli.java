/**
 *  This file is part of DataCleaner.
 *
 *  DataCleaner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DataCleaner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DataCleaner.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.eobjects.datacleaner.gui;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.DataCleanerExecutor;
import dk.eobjects.datacleaner.execution.ExecutionConfiguration;
import dk.eobjects.datacleaner.export.IResultExporter;
import dk.eobjects.datacleaner.export.XmlResultExporter;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.windows.ProfilerWindow;
import dk.eobjects.datacleaner.gui.windows.ValidatorWindow;
import dk.eobjects.datacleaner.profiler.IProfile;
import dk.eobjects.datacleaner.profiler.IProfileResult;
import dk.eobjects.datacleaner.profiler.ProfilerExecutorCallback;
import dk.eobjects.datacleaner.profiler.ProfilerJobConfiguration;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.datacleaner.validator.IValidationRule;
import dk.eobjects.datacleaner.validator.IValidationRuleResult;
import dk.eobjects.datacleaner.validator.ValidatorExecutorCallback;
import dk.eobjects.datacleaner.validator.ValidatorJobConfiguration;
import dk.eobjects.metamodel.DataContext;
import dk.eobjects.metamodel.schema.Table;
import dk.eobjects.metamodel.util.FileHelper;

/**
 * This is the class with the main method for the DataCleaner Command-Line
 * Interface (CLI)
 * 
 * The class contains a main method as well as instance methods to make use of
 * it programmatically (use the setters, the initialize and the run methods).
 */
public class DataCleanerCli {

	private static final String OPTION_HELP = "?";
	private static final String OPTION_INPUT_FILE = "i";
	private static final String OPTION_OVERWRITE = "ow";
	private static final String OPTION_OUTPUT_TYPE = "t";
	private static final String OPTION_OUTPUT_FILE = "o";

	private static final Log _log = LogFactory.getLog(DataCleanerCli.class);

	public static final Options OPTIONS = new Options().addOption(
			OPTION_INPUT_FILE, "input-file", true,
			"input file path (.dcp or .dcv file)").addOption(
			OPTION_OUTPUT_TYPE, "output-type", true,
			"output type (xml|customClassName)").addOption(OPTION_OUTPUT_FILE,
			"output-file", true, "output file path").addOption(
			OPTION_OVERWRITE, "overwrite", false,
			"overwrite output-file if it already exists").addOption(
			OPTION_HELP, "help", false, "show this help message");

	private IResultExporter _resultExporter;
	private PrintWriter _resultWriter = new PrintWriter(System.out);
	private DataContextSelection _dataContextSelection;
	private List<ProfilerJobConfiguration> _profileConfigurations;
	private List<ValidatorJobConfiguration> _validationRuleConfigurations;
	private File _inputFile;

	private ExecutionConfiguration _executionConfiguration;

	public void setDataContextSelection(
			DataContextSelection dataContextSelection) {
		_dataContextSelection = dataContextSelection;
	}

	public void setProfileConfigurations(
			List<ProfilerJobConfiguration> profileConfigurations) {
		_profileConfigurations = profileConfigurations;
	}

	public void setValidationRuleConfigurations(
			List<ValidatorJobConfiguration> validationRuleConfigurations) {
		_validationRuleConfigurations = validationRuleConfigurations;
	}

	public void setResultWriter(PrintWriter writer) {
		_resultWriter = writer;
	}

	public void setInputFile(File inputFile) {
		_inputFile = inputFile;
	}

	private void setResultExporterClass(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Class<?> resultExporterClass = Class.forName(className);
		_resultExporter = (IResultExporter) resultExporterClass.newInstance();
	}

	public void setResultExporter(IResultExporter resultExporter) {
		_resultExporter = resultExporter;
	}

	public void printHelp() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter
				.printHelp(
						_resultWriter,
						100,
						"runjob",
						"Use this command line tool to execute DataCleaner jobs (.dcp or .dcv files)",
						OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD,
						HelpFormatter.DEFAULT_DESC_PAD,
						"Please visit http://datacleaner.eobjects.org/ for more information");

		_resultWriter.flush();
	}

	private void printVersionInfo(String[] args) {
		_resultWriter.println("Running DataCleaner version "
				+ DataCleanerGui.VERSION);
	}

	/**
	 * Initializes the DataCleaner-core framework, ie. sets up Profile and
	 * ValidationRule descriptors and Database drivers
	 * 
	 * @throws Exception
	 *             if a database driver could not be loaded
	 */
	public void initialize() throws Exception {
		GuiConfiguration.initialize();
		GuiSettings.initialize();

		List<DatabaseDriver> databaseDrivers = new LinkedList<DatabaseDriver>();
		databaseDrivers.addAll(GuiConfiguration
				.getBeansOfClass(DatabaseDriver.class));
		databaseDrivers.addAll(GuiSettings.getSettings().getDatabaseDrivers());

		for (DatabaseDriver databaseDriver : databaseDrivers) {
			databaseDriver.loadDriver();
		}

		GuiHelper.silentNotification("CLI: " + DataCleanerGui.VERSION);
	}

	public static void main(String[] args) throws Exception {
		DataCleanerCli cli = new DataCleanerCli();

		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(OPTIONS, args);

		if (commandLine.getOptions().length == 0) {
			cli.printHelp();
		} else if (commandLine.hasOption(OPTION_HELP)) {
			cli.printHelp();
		} else {
			cli.printVersionInfo(args);

			boolean overwrite = commandLine.hasOption(OPTION_OVERWRITE);

			String outputType = commandLine.getOptionValue(OPTION_OUTPUT_TYPE);
			if (outputType == null) {
				// Default output type is xml
				outputType = "xml";
			}

			// Shortcuts for xml output type
			if ("xml".equalsIgnoreCase(outputType)) {
				outputType = XmlResultExporter.class.getName();
			}

			// Leave this open for people to develop their own IResultExporter
			// interfaces
			cli.setResultExporterClass(outputType);

			String filePath = commandLine.getOptionValue(OPTION_OUTPUT_FILE);
			if (filePath != null) {
				File file = new File(filePath);
				if (file.exists() && !overwrite) {
					throw new IllegalArgumentException(
							"The output file '"
									+ filePath
									+ "' already exists. Turn on the --overwrite option to allow writing to existing files.");
				}
				cli.setResultWriter(new PrintWriter(FileHelper
						.getBufferedWriter(file)));
			}

			filePath = commandLine.getOptionValue(OPTION_INPUT_FILE);
			if (filePath == null) {
				// Test if unnamed arguments match the file option
				StringBuilder sb = new StringBuilder();
				String[] remainingArgs = commandLine.getArgs();
				if (remainingArgs.length > 0) {
					for (int i = 0; i < remainingArgs.length; i++) {
						if (i != 0) {
							sb.append(' ');
						}
						String arg = remainingArgs[i];
						sb.append(arg);
					}
					filePath = sb.toString();
				}
			}
			if (filePath != null) {
				if (!filePath.endsWith(".dcp") && !filePath.endsWith(".dcv")) {
					throw new IllegalArgumentException(
							"The input file '"
									+ filePath
									+ "' is not a valid DataCleaner (.dcp or .dcv) file");
				}

				File file = new File(filePath);
				if (!file.exists()) {
					throw new IllegalArgumentException("The input file '"
							+ filePath + "' does not exist");
				}
				cli.setInputFile(file);

				cli.initialize();
				cli.runInputFile();

				// Run garbage collection and finalization in order to close any
				// DataContext objects properly
				System.gc();
				System.runFinalization();
			}
		}
	}

	/**
	 * Runs a job based on an input file. Requires that the InputFile have been
	 * set and that the DataCleaner-core framework have been initialized.
	 * 
	 * @throws Exception
	 *             if the InputFile is not a parsable DataCleaner file
	 */
	public void runInputFile() throws Exception {
		DocumentBuilder documentBuilder = DomHelper.getDocumentBuilder();
		Document document = documentBuilder.parse(_inputFile);
		Node node = document.getDocumentElement();

		// Some common parsing
		Node dataContextSelectionNode = DomHelper.getChildNodesByName(node,
				DataContextSelection.NODE_NAME).get(0);
		DataContextSelection dataContextSelection = DataContextSelection
				.deserialize(dataContextSelectionNode);
		List<Node> executionConfigurationNodes = DomHelper.getChildNodesByName(
				node, ExecutionConfiguration.NODE_NAME);
		ExecutionConfiguration executionConfiguration;
		if (executionConfigurationNodes == null
				|| executionConfigurationNodes.isEmpty()) {
			executionConfiguration = new ExecutionConfiguration();
		} else {
			executionConfiguration = ExecutionConfiguration
					.deserialize(executionConfigurationNodes.get(0));
		}

		// Always disable drill-to-detail in profiler results when running in
		// batch-mode
		executionConfiguration.setDrillToDetailEnabled(false);
		setExecutionConfiguration(executionConfiguration);

		setDataContextSelection(dataContextSelection);
		DataContext dc = dataContextSelection.getDataContext();

		if (ProfilerWindow.NODE_NAME.equals(node.getNodeName())) {
			// Profiler specific parsing and execution
			List<Node> configurationNodes = DomHelper.getChildNodesByName(node,
					ProfilerJobConfiguration.NODE_NAME);
			List<ProfilerJobConfiguration> configurations = new ArrayList<ProfilerJobConfiguration>();
			for (Node configurationNode : configurationNodes) {
				ProfilerJobConfiguration configuration = ProfilerJobConfiguration
						.deserialize(configurationNode, dc);
				configurations.add(configuration);
			}

			setProfileConfigurations(configurations);
			runProfiler();

		} else if (ValidatorWindow.NODE_NAME.equals(node.getNodeName())) {
			// Validator specific parsing and execution
			List<Node> configurationNodes = DomHelper.getChildNodesByName(node,
					ValidatorJobConfiguration.NODE_NAME);
			List<ValidatorJobConfiguration> configurations = new ArrayList<ValidatorJobConfiguration>();
			for (Node configurationNode : configurationNodes) {
				ValidatorJobConfiguration configuration = ValidatorJobConfiguration
						.deserialize(configurationNode, dc);
				configurations.add(configuration);
			}

			setValidationRuleConfigurations(configurations);
			runValidator();

		} else {
			throw new Exception("Could not deserialize node with name '"
					+ node.getNodeName() + "'");
		}
	}

	private void setExecutionConfiguration(
			ExecutionConfiguration executionConfiguration) {
		_executionConfiguration = executionConfiguration;
	}

	/**
	 * Runs a profiler job. Requires that the DataContext and
	 * ProfilerConfigurations have been set and that the DataCleaner-core
	 * framework have been initialized.
	 */
	public void runProfiler() {
		_log.info("--- executing profiler ---");

		DataCleanerExecutor<ProfilerJobConfiguration, IProfileResult, IProfile> executor = ProfilerExecutorCallback
				.createExecutor();
		for (ProfilerJobConfiguration configuration : _profileConfigurations) {
			executor.addJobConfiguration(configuration);
		}
		executor.setExecutionConfiguration(_executionConfiguration);
		executor.execute(_dataContextSelection);

		Table[] tables = executor.getResultTables();
		boolean collectiveResults = _resultExporter
				.isCollectiveResultsCapable();

		if (collectiveResults) {
			_resultExporter.writeProfileResultHeader(_resultWriter);
		}
		for (Table table : tables) {
			List<IProfileResult> results = executor.getResultsForTable(table);
			for (IProfileResult result : results) {
				if (!collectiveResults) {
					// TODO: init new resultWriter
					_resultExporter.writeProfileResultHeader(_resultWriter);
				}
				_resultExporter
						.writeProfileResult(table, result, _resultWriter);
				if (!collectiveResults) {
					_resultExporter.writeProfileResultFooter(_resultWriter);
				}
			}
		}
		if (collectiveResults) {
			_resultExporter.writeProfileResultFooter(_resultWriter);
		}

		_resultWriter.close();
	}

	/**
	 * Runs a validator job. Requires that the DataContext and
	 * ValidationRuleConfigurations have been set and that the DataCleaner-core
	 * framework have been initialized.
	 */
	public void runValidator() {
		_log.info("--- executing validator ---");

		DataCleanerExecutor<ValidatorJobConfiguration, IValidationRuleResult, IValidationRule> executor = ValidatorExecutorCallback
				.createExecutor();

		for (ValidatorJobConfiguration configuration : _validationRuleConfigurations) {
			executor.addJobConfiguration(configuration);
		}
		executor.setExecutionConfiguration(_executionConfiguration);
		executor.execute(_dataContextSelection);

		Table[] tables = executor.getResultTables();
		boolean collectiveResults = _resultExporter
				.isCollectiveResultsCapable();

		if (collectiveResults) {
			_resultExporter.writeValidationRuleResultHeader(_resultWriter);
		}
		for (Table table : tables) {
			List<IValidationRuleResult> results = executor
					.getResultsForTable(table);
			for (IValidationRuleResult result : results) {
				if (!collectiveResults) {
					// TODO: init new resultWriter
					_resultExporter
							.writeValidationRuleResultHeader(_resultWriter);
				}
				_resultExporter.writeValidationRuleResult(table, result,
						_resultWriter);
				if (!collectiveResults) {
					_resultExporter
							.writeValidationRuleResultFooter(_resultWriter);
				}
			}
		}
		if (collectiveResults) {
			_resultExporter.writeValidationRuleResultFooter(_resultWriter);
		}

		_resultWriter.close();
	}
}
