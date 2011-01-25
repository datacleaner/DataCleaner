/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;

public class AboutDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	public static class LicensedProject {
		public String name;
		public String websiteUrl;
		public String license;
	}

	private final ResourceManager resourceManager = ResourceManager.getInstance();
	private final ImageManager imageManager = ImageManager.getInstance();
	private final Map<String, String> _licenses = new HashMap<String, String>();

	@Override
	protected String getBannerTitle() {
		return "About DataCleaner";
	}

	@Override
	protected int getDialogWidth() {
		return 600;
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected JComponent getDialogContent() {
		CloseableTabbedPane tabbedPane = new CloseableTabbedPane();

		tabbedPane.addTab("", imageManager.getImageIcon("images/window/app-icon.png", IconUtils.ICON_SIZE_LARGE),
				getAboutPanel(), "About DataCleaner");
		tabbedPane.setUnclosableTab(0);

		tabbedPane.addTab("", imageManager.getImageIcon("images/menu/license.png"), getLicensePanel(), "License");
		tabbedPane.setUnclosableTab(1);

		tabbedPane.addTab("", imageManager.getImageIcon("images/menu/users.png"), getCommunityPanel(), "Community");
		tabbedPane.setUnclosableTab(2);

		tabbedPane.setPreferredSize(new Dimension(getDialogWidth(), 500));

		return tabbedPane;
	}

	private JComponent getCommunityPanel() {
		return DCLabel.dark("TODO");
	}

	private JComponent getLicensePanel() {
		DCPanel panel = new DCPanel();
		
		WidgetUtils.addToGridBag(DCLabel.dark("DataCleaners license:"), panel, 0, 0);

		WidgetUtils.addToGridBag(DCLabel.dark("LGPL"), panel, 1, 0, 1.0, 0.0);
		String license = getLicense("lgpl");
		DCLabel licenseLabel = DCLabel.darkMultiLine(license);
		WidgetUtils.addToGridBag(licenseLabel, panel, 0, 1, 2, 1);

		return WidgetUtils.scrolleable(panel);
	}

	private JComponent getAboutPanel() {
		return DCLabel.dark("TODO");
	}

	@Override
	protected String getWindowTitle() {
		return "About DataCleaner | DataCleaner";
	}

	public List<LicensedProject> getLicensedProjects() {
		final List<LicensedProject> result = new ArrayList<AboutDialog.LicensedProject>();
		final URL url = resourceManager.getUrl("licenses/dependency-licenses.csv");
		if (url == null) {
			throw new IllegalStateException("Could not find dependencies file");
		}
		try {
			DataContext dc = DataContextFactory.createCsvDataContext(url.openStream(), ',', '"', false);
			Table table = dc.getDefaultSchema().getTables()[0];
			Column projectColumn = table.getColumnByName("Project");
			Column websiteColumn = table.getColumnByName("Website");
			Column licenseColumn = table.getColumnByName("License");
			Query q = dc.query().from(table).select(table.getColumns()).orderBy(projectColumn).asc().toQuery();
			DataSet ds = dc.executeQuery(q);
			while (ds.next()) {
				final LicensedProject licensedProject = new LicensedProject();
				final Row row = ds.getRow();
				final String licenseName = row.getValue(licenseColumn).toString();

				licensedProject.name = row.getValue(projectColumn).toString();
				licensedProject.websiteUrl = row.getValue(websiteColumn).toString();
				licensedProject.license = getLicense(licenseName);

				result.add(licensedProject);
			}

		} catch (IOException e) {
			throw new IllegalStateException("Error occurred while reading dependencies file", e);
		}

		return result;
	}

	public String getLicense(final String licenseName) {
		String license = _licenses.get(licenseName);
		if (license == null) {
			URL url = resourceManager.getUrl("licenses/" + licenseName + ".txt");
			if (url == null) {
				throw new IllegalArgumentException("Could not find license file for license: " + licenseName);
			}
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(url.openStream(), FileHelper.UTF_8_ENCODING));
				final StringBuilder sb = new StringBuilder();
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					if (sb.length() != 0) {
						sb.append('\n');
					}
					sb.append(line);
				}

				license = sb.toString();
			} catch (Exception e) {
				throw new IllegalStateException("Error occurred while reading license file: " + licenseName, e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// do nothing
					}
				}
			}
			_licenses.put(licenseName, license);
		}
		return license;

	}
}
