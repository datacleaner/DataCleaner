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
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import dk.eobjects.datacleaner.data.DataContextSelection;
import dk.eobjects.datacleaner.execution.ProfileRunner;
import dk.eobjects.datacleaner.execution.ValidationRuleRunner;
import dk.eobjects.datacleaner.gui.model.DatabaseDriver;
import dk.eobjects.datacleaner.gui.setup.GuiConfiguration;
import dk.eobjects.datacleaner.gui.setup.GuiSettings;
import dk.eobjects.datacleaner.gui.windows.ProfilerWindow;
import dk.eobjects.datacleaner.gui.windows.ValidatorWindow;
import dk.eobjects.datacleaner.profiler.ProfileConfiguration;
import dk.eobjects.datacleaner.util.DomHelper;
import dk.eobjects.datacleaner.validator.ValidationRuleConfiguration;
import dk.eobjects.metamodel.DataContext;

/**
 * This is the class with the main method for the DataCleaner Command-Line
 * Interface (CLI)
 * 
 * The class contains a main method as well as instance methods to make use of
 * it programmatically (use the setters, the initialize and the run methods).
 */
public class DataCleanerCli {

	public enum OutputType {
		CSV, XML
	}

	public static final Options OPTIONS = new Options().addOption("f", "file",
			true, "input file path (.dcp or .dcv file)").addOption("o",
			"output-type", true, "output type (csv|xml)").addOption("?",
			"help", false, "show this help message");

	private PrintWriter _consoleWriter = new PrintWriter(System.out);
	private OutputType _outputType = OutputType.XML;
	private DataContext _dataContext;
	private List<ProfileConfiguration> _profileConfigurations;
	private List<ValidationRuleConfiguration> _validationRuleConfigurations;
	private File _inputFile;

	public void setDataContext(DataContext dataContext) {
		_dataContext = dataContext;
	}

	public void setProfileConfigurations(
			List<ProfileConfiguration> profileConfigurations) {
		_profileConfigurations = profileConfigurations;
	}

	public void setValidationRuleConfigurations(
			List<ValidationRuleConfiguration> validationRuleConfigurations) {
		_validationRuleConfigurations = validationRuleConfigurations;
	}

	public void setConsoleWriter(PrintWriter writer) {
		_consoleWriter = writer;
	}

	public void setInputFile(File inputFile) {
		_inputFile = inputFile;
	}

	public void setOutputType(OutputType outputType) {
		if (outputType != null) {
			_outputType = outputType;
		}
	}

	public void printHelp() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter
				.printHelp(
						_consoleWriter,
						100,
						"runjob",
						"Use this command line tool to execute DataCleaner jobs (.dcp or .dcv files)",
						OPTIONS, HelpFormatter.DEFAULT_LEFT_PAD,
						HelpFormatter.DEFAULT_DESC_PAD,
						"Please visit http://eobjects.org/datacleaner for more information");

		_consoleWriter.flush();
	}

	private void printVersionInfo(String[] args) {
		_consoleWriter.println("Running DataCleaner version "
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
	}

	public static void main(String[] args) throws Exception {
		DataCleanerCli cli = new DataCleanerCli();

		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(OPTIONS, args);

		if (commandLine.getOptions().length == 0) {
			cli.printHelp();
		} else if (commandLine.hasOption("?")) {
			cli.printHelp();
		} else {
			cli.printVersionInfo(args);

			String outputType = commandLine.getOptionValue("o");
			if (outputType != null) {
				OutputType type = OutputType.valueOf(outputType.toUpperCase());
				cli.setOutputType(type);
			}

			String filePath = commandLine.getOptionValue("f");
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
		_consoleWriter.println("Using input file: " + _inputFile);

		DocumentBuilder documentBuilder = DomHelper.getDocumentBuilder();
		Document document = documentBuilder.parse(_inputFile);
		Node node = document.getDocumentElement();

		// Some common parsing
		Node dataContextSelectionNode = DomHelper.getChildNodesByName(node,
				DataContextSelection.NODE_NAME).get(0);
		DataContextSelection dataContextSelection = DataContextSelection
				.deserialize(dataContextSelectionNode);
		DataContext dc = dataContextSelection.getDataContext();
		setDataContext(dc);

		if (ProfilerWindow.NODE_NAME.equals(node.getNodeName())) {
			// Profiler specific parsing and execution
			List<Node> configurationNodes = DomHelper.getChildNodesByName(node,
					ProfileConfiguration.NODE_NAME);
			List<ProfileConfiguration> configurations = new ArrayList<ProfileConfiguration>();
			for (Node configurationNode : configurationNodes) {
				ProfileConfiguration configuration = ProfileConfiguration
						.deserialize(configurationNode, dc);
				configurations.add(configuration);
			}

			setProfileConfigurations(configurations);
			runProfiler();

		} else if (ValidatorWindow.NODE_NAME.equals(node.getNodeName())) {
			// Validator specific parsing and execution
			List<Node> configurationNodes = DomHelper.getChildNodesByName(node,
					ValidationRuleConfiguration.NODE_NAME);
			List<ValidationRuleConfiguration> configurations = new ArrayList<ValidationRuleConfiguration>();
			for (Node configurationNode : configurationNodes) {
				ValidationRuleConfiguration configuration = ValidationRuleConfiguration
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

	/**
	 * Runs a profiler job. Requires that the DataContext and
	 * ProfilerConfigurations have been set and that the DataCleaner-core
	 * framework have been initialized.
	 */
	public void runProfiler() {
		_consoleWriter.println("Executing profiler");

		ProfileRunner runner = new ProfileRunner();
		runner.setDetailsEnabled(false);
		for (ProfileConfiguration configuration : _profileConfigurations) {
			runner.addConfiguration(configuration);
		}
		runner.execute(_dataContext);

		// TODO: Handle result
		_consoleWriter.println("TODO: Print to " + _outputType + " source");

		_consoleWriter.flush();
	}

	/**
	 * Runs a validator job. Requires that the DataContext and
	 * ValidationRuleConfigurations have been set and that the DataCleaner-core
	 * framework have been initialized.
	 */
	public void runValidator() {
		_consoleWriter.println("Executing validator");

		ValidationRuleRunner runner = new ValidationRuleRunner();
		for (ValidationRuleConfiguration configuration : _validationRuleConfigurations) {
			runner.addConfiguration(configuration);
		}

		runner.execute(_dataContext);

		// TODO: Handle result
		_consoleWriter.println("TODO: Print to " + _outputType + " source");

		_consoleWriter.flush();
	}
}
