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

import java.util.TreeMap;

import junit.framework.TestCase;

public class CliArgumentsTest extends TestCase {

	public void testIsSetFalse() throws Exception {
		CliArguments args;
		args = CliArguments.parse(new String[0]);
		assertFalse(args.isSet());

		args = CliArguments.parse(null);
		assertFalse(args.isSet());

		args = CliArguments.parse(new String[] { "-hello", "world" });
		assertFalse(args.isSet());
	}

	public void testIsSetTrue() throws Exception {
		CliArguments args;
		args = CliArguments.parse(new String[] { "-list", "TABLES", "-ds", "mrrh" });
		assertTrue(args.isSet());

		args = CliArguments.parse(new String[] { "-usage" });
		assertTrue(args.isSet());

		args = CliArguments.parse(new String[] { "-ds", "foo" });
		assertFalse(args.isSet());
		assertEquals("foo", args.getDatastoreName());
	}

	public void testVariableOverrides() throws Exception {
		CliArguments args;
		args = CliArguments.parse("-job myjob.xml -conf conf.xml -var foo=bar -v bar=c:\\foo\bar\baz.csv".split(" "));
		assertEquals("{bar=c:\\foo\bar\baz.csv, foo=bar}",
				new TreeMap<String, String>(args.getVariableOverrides()).toString());
	}
}
