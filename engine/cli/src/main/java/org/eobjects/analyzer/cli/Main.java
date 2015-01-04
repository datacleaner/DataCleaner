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
package org.eobjects.analyzer.cli;

import java.io.PrintWriter;

import org.apache.metamodel.util.FileHelper;

/**
 * Main class for the AnalyzerBeans Command-line interface (CLI).
 */
public final class Main {

	/**
	 * Main method of the Command-line interface (CLI)
	 * 
	 * @param args
	 *            command-line arguments
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		CliArguments arguments = CliArguments.parse(args);
		if (arguments.isSet() && !arguments.isUsageMode()) {
			CliRunner runner = new CliRunner(arguments);
			try {
				runner.run();
			} finally {
				runner.close();
			}
		} else {
			PrintWriter out = new PrintWriter(System.out);
			printUsage(out);
			FileHelper.safeClose(out);
		}
	}

	private static void printUsage(PrintWriter out) {
		CliArguments.printUsage(out);
	}
}
