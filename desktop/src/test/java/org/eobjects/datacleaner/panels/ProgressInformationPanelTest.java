/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.panels;

import java.sql.SQLException;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;

import org.eobjects.datacleaner.panels.result.ProgressInformationPanel;
import org.eobjects.metamodel.MetaModelException;

public class ProgressInformationPanelTest extends TestCase {

	private SQLException sql1;
	private SQLException sql2;
	private MetaModelException outerException;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sql1 = new SQLException("sql1");
		sql2 = new SQLException("sql2");
		outerException = new MetaModelException("mm", sql1);
		sql1.setNextException(sql2);
	}

	public void testPrintNextSQLExceptions() throws Exception {
		ProgressInformationPanel panel = new ProgressInformationPanel(true);
		panel.addUserLog("damn, something rotten happened!", outerException, true);

		// wait for swing dispatch
		SwingUtilities.invokeAndWait(new Thread());
		String text = panel.getTextAreaText();
		
		assertTrue(text.indexOf("ERROR: damn, something rotten happened!") != -1);
		assertTrue(text.indexOf("org.eobjects.metamodel.MetaModelException: mm") != -1);
		assertTrue(text.indexOf("Caused by: java.sql.SQLException: sql1") != -1);
		assertTrue(text.indexOf("Next exception: java.sql.SQLException: sql2") != -1);
	}

	public void testPrintSingleException() throws Exception {
		ProgressInformationPanel panel = new ProgressInformationPanel(true);
		panel.addUserLog("damn, something rotten happened!", sql2, true);

		// wait for swing dispatch
		SwingUtilities.invokeAndWait(new Thread());
		String text = panel.getTextAreaText();
		
		System.out.println(text);
		

		assertTrue(text.indexOf("ERROR: damn, something rotten happened!") != -1);
		assertFalse(text.indexOf("org.eobjects.metamodel.MetaModelException: mm") != -1);
		assertFalse(text.indexOf("Caused by: java.sql.SQLException: sql1") != -1);
		assertTrue(text.indexOf("java.sql.SQLException: sql2") != -1);
	}
}
