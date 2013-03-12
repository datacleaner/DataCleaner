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
package org.eobjects.datacleaner.windows;

import java.util.List;

import org.eobjects.datacleaner.windows.AboutDialog.LicensedProject;

import junit.framework.TestCase;

public class AboutDialogTest extends TestCase {

	public void testGetLicense() throws Exception {
		String mit = AboutDialog.getLicense("mit");
		assertNotNull(mit);
		assertTrue(mit.startsWith("Copyright (C)"));
		assertTrue(mit.endsWith(" OTHER DEALINGS IN\nTHE SOFTWARE."));
	}

	public void testGetLicensedProects() throws Exception {
		List<LicensedProject> lp = AboutDialog.getLicensedProjects();
		for (LicensedProject licensedProject : lp) {
			assertNotNull(licensedProject.name);
			assertNotNull(licensedProject.websiteUrl);
			assertNotNull(licensedProject.license);
		}
	}
}
