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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.eobjects.analyzer.util.StringUtils;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.util.ResourceManager;
import org.eobjects.datacleaner.util.WidgetFactory;
import org.eobjects.datacleaner.util.WidgetUtils;
import org.eobjects.datacleaner.widgets.DCLabel;
import org.eobjects.datacleaner.widgets.HumanInferenceToolbarButton;
import org.eobjects.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.eobjects.metamodel.DataContext;
import org.eobjects.metamodel.DataContextFactory;
import org.eobjects.metamodel.data.DataSet;
import org.eobjects.metamodel.data.Row;
import org.eobjects.metamodel.query.Query;
import org.eobjects.metamodel.schema.Column;
import org.eobjects.metamodel.schema.Table;
import org.eobjects.metamodel.util.FileHelper;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * The "About" dialog of the DataCleaner application.
 * 
 * @author Kasper Sørensen
 */
public class AboutDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;

	public static class LicensedProject {
		public String name;
		public String websiteUrl;
		public String license;
	}

	private static final ResourceManager resourceManager = ResourceManager.getInstance();
	private static final ImageManager imageManager = ImageManager.getInstance();

	public AboutDialog(WindowContext windowContext) {
		super(windowContext);
	}

	@Override
	public void toFront() {
		super.toFront();
	}

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

		tabbedPane.addTab("About DataCleaner",
				imageManager.getImageIcon("images/window/app-icon.png", IconUtils.ICON_SIZE_LARGE), getAboutPanel(),
				"About DataCleaner");
		tabbedPane.setUnclosableTab(0);

		tabbedPane.addTab("Licensing", imageManager.getImageIcon("images/menu/license.png"), getLicensingPanel(),
				"Licensing");
		tabbedPane.setUnclosableTab(1);

		tabbedPane.setPreferredSize(new Dimension(getDialogWidth(), 500));

		return tabbedPane;
	}

	private JComponent getLicensingPanel() {
		final String dcLicense = getLicense("lgpl");

		final DCLabel licenseHeader = DCLabel.dark("");
		licenseHeader.setFont(WidgetUtils.FONT_HEADER);

		final DCLabel licenseLabel = DCLabel.darkMultiLine("");
		licenseLabel.setBackground(WidgetUtils.BG_COLOR_BRIGHTEST);
		licenseLabel.setFont(WidgetUtils.FONT_MONOSPACE);
		licenseLabel.setOpaque(true);

		final JButton dcLicenseButton = WidgetFactory.createSmallButton("images/menu/license.png");
		dcLicenseButton.setToolTipText("DataCleaner's license: GNU LGPL");
		dcLicenseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				licenseHeader.setText("Displaying license of DataCleaner");
				licenseLabel.setText(dcLicense);
			}
		});

		final JComboBox librariesComboBox = new JComboBox();
		final JButton visitProjectButton = WidgetFactory.createSmallButton("images/actions/website.png");

		librariesComboBox.setRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof LicensedProject) {
					LicensedProject project = (LicensedProject) value;
					String name = project.name;
					return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
				} else if (value instanceof String) {
					return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				}
				throw new UnsupportedOperationException();
			}
		});
		librariesComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object item = e.getItem();
				if (item instanceof LicensedProject) {
					visitProjectButton.setEnabled(true);
					LicensedProject project = (LicensedProject) item;
					licenseLabel.setText(project.license);
					licenseHeader.setText("Displaying license of " + project.name + "");
				} else {
					visitProjectButton.setEnabled(false);
					licenseHeader.setText("Displaying license of DataCleaner");
					licenseLabel.setText(dcLicense);
				}
			}
		});

		visitProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object item = librariesComboBox.getSelectedItem();
				LicensedProject project = (LicensedProject) item;
				String websiteUrl = project.websiteUrl;
				if (!StringUtils.isNullOrEmpty(websiteUrl)) {
					new OpenBrowserAction(websiteUrl).actionPerformed(e);
				}
			}
		});

		librariesComboBox.addItem("- select project -");
		final List<LicensedProject> licensedProjects = getLicensedProjects();
		for (LicensedProject licensedProject : licensedProjects) {
			librariesComboBox.addItem(licensedProject);
		}

		final JToolBar toolBar = WidgetFactory.createToolBar();
		toolBar.add(DCLabel.dark("DataCleaners license: "));
		toolBar.add(dcLicenseButton);
		toolBar.add(WidgetFactory.createToolBarSeparator());
		toolBar.add(DCLabel.dark("Included libraries: "));
		toolBar.add(librariesComboBox);
		toolBar.add(visitProjectButton);

		final JScrollPane licenseLabelScroll = WidgetUtils.scrolleable(licenseLabel);
		licenseLabelScroll.setBorder(new CompoundBorder(new EmptyBorder(10, 0, 10, 0), WidgetUtils.BORDER_THIN));

		final DCPanel headerPanel = new DCPanel();
		headerPanel.setLayout(new VerticalLayout());
		headerPanel.add(toolBar);
		headerPanel.add(Box.createVerticalStrut(20));
		headerPanel.add(licenseHeader);

		final DCPanel panel = new DCPanel(WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		panel.setBorder(new EmptyBorder(4, 4, 4, 4));
		panel.setLayout(new BorderLayout());
		panel.add(headerPanel, BorderLayout.NORTH);
		panel.add(licenseLabelScroll, BorderLayout.CENTER);

		return panel;
	}

	private JComponent getAboutPanel() {
		final DCLabel headerLabel = DCLabel.dark("DataCleaner " + Main.VERSION);
		headerLabel.setFont(WidgetUtils.FONT_HEADER);

		final ImageManager imageManager = ImageManager.getInstance();

		final JButton datacleanerButton = new JButton(imageManager.getImageIcon("images/links/datacleaner.png"));
		datacleanerButton.addActionListener(new OpenBrowserAction("http://datacleaner.eobjects.org"));
		datacleanerButton.setToolTipText("Visit the DataCleaner website");
		datacleanerButton.setBorder(null);

		final JButton bloggerButton = new JButton(imageManager.getImageIcon("images/links/blogger.png"));
		bloggerButton.addActionListener(new OpenBrowserAction("http://kasper.eobjects.org"));
		bloggerButton.setToolTipText("Follow along at our blog");
		bloggerButton.setBorder(null);

		final JButton linkedInButton = new JButton(imageManager.getImageIcon("images/links/linkedin.png"));
		linkedInButton.addActionListener(new OpenBrowserAction("http://www.linkedin.com/groups?gid=3352784"));
		linkedInButton.setToolTipText("Join the DataCleaner LinkedIn group");
		linkedInButton.setBorder(null);

		final DCPanel buttonPanel = new DCPanel();
		buttonPanel.setLayout(new HorizontalLayout());
		buttonPanel.add(datacleanerButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(bloggerButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		buttonPanel.add(linkedInButton);

		final HumanInferenceToolbarButton humanInferenceButton = new HumanInferenceToolbarButton(
				imageManager.getImageIcon("images/powered-by-human-inference-bright.png"));

		final DCPanel contentPanel = new DCPanel();
		contentPanel.setLayout(new VerticalLayout());
		contentPanel.add(headerLabel);
		contentPanel.add(DCLabel.dark("Copyright (C) 2010 eobjects.org"));
		contentPanel.add(Box.createVerticalStrut(20));
		contentPanel.add(DCPanel.around(humanInferenceButton));
		contentPanel.add(Box.createVerticalStrut(20));
		contentPanel.add(DCLabel.dark("Licensed under the LGPL license"));
		contentPanel.add(DCLabel.dark("(see Licensing tab)."));
		contentPanel.add(Box.createVerticalStrut(50));
		contentPanel.add(buttonPanel);

		final DCPanel mainPanel = new DCPanel(imageManager.getImage("images/window/app-icon-hires.png"), 97, 10,
				WidgetUtils.BG_COLOR_BRIGHT, WidgetUtils.BG_COLOR_BRIGHTEST);
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainPanel.setLayout(new VerticalLayout());
		mainPanel.add(contentPanel);

		return mainPanel;
	}

	@Override
	public String getWindowTitle() {
		return "About DataCleaner | DataCleaner";
	}

	public static List<LicensedProject> getLicensedProjects() {
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

	public static String getLicense(final String licenseName) {
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

			return sb.toString();
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
	}
}
