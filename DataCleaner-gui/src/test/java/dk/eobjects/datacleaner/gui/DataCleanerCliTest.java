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

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

public class DataCleanerCliTest extends TestCase {

	public void testPrintHelp() throws Exception {
		StringWriter stringWriter = new StringWriter();
		DataCleanerCli cli = new DataCleanerCli();
		cli.setConsoleWriter(new PrintWriter(stringWriter));
		cli.printHelp();
		assertEquals(
				"usage: runjob\n"
						+ "Use this command line tool to execute DataCleaner jobs (.dcp or .dcv files)\n"
						+ " -?,--help                show this help message\n"
						+ " -f,--file <arg>          input file path (.dcp or .dcv file)\n"
						+ " -o,--output-type <arg>   output type (csv|xml)\n"
						+ "Please visit http://eobjects.org/datacleaner for more information",
				stringWriter.toString().trim());
	}
}
