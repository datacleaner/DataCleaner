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
package org.datacleaner.widgets;

import javax.swing.JComponent;

import junit.framework.TestCase;

import org.datacleaner.beans.standardize.EmailStandardizerTransformer;
import org.datacleaner.descriptors.Descriptors;

public class DescriptorMenuItemTest extends TestCase {

	public void testToolTipSize() throws Exception {
		DescriptorMenuItem menuItem = new DescriptorMenuItem(
				Descriptors.ofTransformer(EmailStandardizerTransformer.class));

		JComponent toolTipPanel = menuItem.createToolTipPanel();
		assertTrue(1000 > toolTipPanel.getPreferredSize().width);
		assertTrue(1000 > toolTipPanel.getPreferredSize().height);
	}
}
