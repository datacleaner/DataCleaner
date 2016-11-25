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
package org.datacleaner.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.FileHelper;
import org.datacleaner.Version;
import org.datacleaner.bootstrap.DCWindowContext;
import org.datacleaner.bootstrap.WindowContext;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.util.IconUtils;
import org.datacleaner.util.ImageManager;
import org.datacleaner.util.ResourceManager;
import org.datacleaner.util.StringUtils;
import org.datacleaner.util.WidgetFactory;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.DCLabel;
import org.datacleaner.widgets.DCListCellRenderer;
import org.datacleaner.widgets.NeopostToolbarButton;
import org.datacleaner.widgets.tabs.CloseableTabbedPane;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.action.OpenBrowserAction;

/**
 * The "About" dialog of the DataCleaner application.
 */
public class AboutDialog extends AbstractDialog {

    public static class LicensedProject {
        public String name;
        public String websiteUrl;
        public String license;
    }

    private static final long serialVersionUID = 1L;
    private static final ResourceManager resourceManager = ResourceManager.get();
    private static final ImageManager imageManager = ImageManager.get();

    public AboutDialog(final WindowContext windowContext) {
        super(windowContext);
    }

    public static List<LicensedProject> getLicensedProjects() {
        final List<LicensedProject> result = new ArrayList<>();
        final URL url = resourceManager.getUrl("licenses/dependency-licenses.csv");
        if (url == null) {
            throw new IllegalStateException("Could not find dependencies file");
        }
        try {
            final DataContext dc = DataContextFactory.createCsvDataContext(url.openStream(), ',', '"');
            final Table table = dc.getDefaultSchema().getTables()[0];
            final Column projectColumn = table.getColumnByName("Project");
            final Column websiteColumn = table.getColumnByName("Website");
            final Column licenseColumn = table.getColumnByName("License");
            final Query q = dc.query().from(table).select(table.getColumns()).orderBy(projectColumn).asc().toQuery();
            final DataSet ds = dc.executeQuery(q);
            while (ds.next()) {
                final LicensedProject licensedProject = new LicensedProject();
                final Row row = ds.getRow();
                final String licenseName = row.getValue(licenseColumn).toString();

                licensedProject.name = row.getValue(projectColumn).toString();
                licensedProject.websiteUrl = row.getValue(websiteColumn).toString();
                licensedProject.license = getLicense(licenseName);

                result.add(licensedProject);
            }

        } catch (final IOException e) {
            throw new IllegalStateException("Error occurred while reading dependencies file", e);
        }

        return result;
    }

    public static String getLicense(final String licenseName) {
        final URL url = resourceManager.getUrl("licenses/" + licenseName + ".txt");
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
        } catch (final Exception e) {
            throw new IllegalStateException("Error occurred while reading license file: " + licenseName, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // do nothing
                }
            }
        }
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
        return 650;
    }

    @Override
    protected boolean isWindowResizable() {
        return true;
    }

    @Override
    protected JComponent getDialogContent() {
        final CloseableTabbedPane tabbedPane = new CloseableTabbedPane(true);

        tabbedPane.addTab("About DataCleaner",
                imageManager.getImageIcon(IconUtils.APPLICATION_ICON, IconUtils.ICON_SIZE_LARGE), getAboutPanel(),
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
        licenseHeader.setFont(WidgetUtils.FONT_HEADER1);

        final DCLabel licenseLabel = DCLabel.darkMultiLine("");
        licenseLabel.setBackground(WidgetUtils.BG_COLOR_BRIGHTEST);
        licenseLabel.setFont(WidgetUtils.FONT_MONOSPACE);
        licenseLabel.setOpaque(true);

        final JButton dcLicenseButton = WidgetFactory.createSmallButton("images/menu/license.png");
        dcLicenseButton.setToolTipText("DataCleaner's license: GNU LGPL");
        dcLicenseButton.addActionListener(e -> {
            licenseHeader.setText("Displaying license of DataCleaner");
            licenseLabel.setText(dcLicense);
        });

        final JComboBox<Object> librariesComboBox = new JComboBox<>();
        final JButton visitProjectButton = WidgetFactory.createSmallButton(IconUtils.WEBSITE);

        librariesComboBox.setRenderer(new DCListCellRenderer() {

            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
                    final boolean isSelected, final boolean cellHasFocus) {
                if (value instanceof LicensedProject) {
                    final LicensedProject project = (LicensedProject) value;
                    final String name = project.name;
                    return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
                } else if (value instanceof String) {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
                throw new UnsupportedOperationException();
            }
        });
        librariesComboBox.addItemListener(e -> {
            final Object item = e.getItem();
            if (item instanceof LicensedProject) {
                visitProjectButton.setEnabled(true);
                final LicensedProject project = (LicensedProject) item;
                licenseLabel.setText(project.license);
                licenseHeader.setText("Displaying license of " + project.name + "");
            } else {
                visitProjectButton.setEnabled(false);
                licenseHeader.setText("Displaying license of DataCleaner");
                licenseLabel.setText(dcLicense);
            }
        });

        visitProjectButton.addActionListener(e -> {
            final Object item = librariesComboBox.getSelectedItem();
            final LicensedProject project = (LicensedProject) item;
            final String websiteUrl = project.websiteUrl;
            if (!StringUtils.isNullOrEmpty(websiteUrl)) {
                new OpenBrowserAction(websiteUrl).actionPerformed(e);
            }
        });

        librariesComboBox.addItem("- select project -");
        final List<LicensedProject> licensedProjects = getLicensedProjects();
        for (final LicensedProject licensedProject : licensedProjects) {
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

        final DCPanel panel = new DCPanel(WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        panel.setLayout(new BorderLayout());
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(licenseLabelScroll, BorderLayout.CENTER);

        return panel;
    }

    private JComponent getAboutPanel() {
        final DCLabel headerLabel =
                DCLabel.dark("DataCleaner " + Version.getEdition() + " " + Version.getDistributionVersion());
        headerLabel.setFont(WidgetUtils.FONT_HEADER1);

        final ImageManager imageManager = ImageManager.get();

        final JButton datacleanerButton = new JButton(imageManager.getImageIcon("images/links/datacleaner.png"));
        WidgetUtils.setDefaultButtonStyle(datacleanerButton);
        datacleanerButton.addActionListener(new OpenBrowserAction("https://datacleaner.org"));
        datacleanerButton.setToolTipText("Visit the DataCleaner website");

        final JButton bloggerButton = new JButton(imageManager.getImageIcon("images/links/blogger.png"));
        bloggerButton.addActionListener(new OpenBrowserAction("http://kasper.eobjects.org"));
        bloggerButton.setToolTipText("Follow along at our blog");
        WidgetUtils.setDefaultButtonStyle(bloggerButton);

        final JButton linkedInButton = new JButton(imageManager.getImageIcon("images/links/linkedin.png"));
        linkedInButton.addActionListener(new OpenBrowserAction("http://www.linkedin.com/groups?gid=3352784"));
        linkedInButton.setToolTipText("Join the DataCleaner LinkedIn group");
        WidgetUtils.setDefaultButtonStyle(linkedInButton);

        final DCPanel buttonPanel = new DCPanel();
        buttonPanel.setLayout(new HorizontalLayout());
        buttonPanel.add(datacleanerButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(bloggerButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(linkedInButton);

        final NeopostToolbarButton neopostButton =
                new NeopostToolbarButton(imageManager.getImageIcon("images/powered-by-neopost-bright.png"));

        final DCPanel contentPanel = new DCPanel();
        contentPanel.setLayout(new VerticalLayout());
        contentPanel.add(headerLabel);
        contentPanel.add(DCLabel.dark("Core version " + Version.getVersion()));
        contentPanel.add(DCLabel.dark("Copyright (C) " + Calendar.getInstance().get(Calendar.YEAR) + " Neopost"));
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(DCPanel.around(neopostButton));

        if (Version.isCommunityEdition()) {
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(DCLabel.dark("Licensed under the LGPL license"));
            contentPanel.add(DCLabel.dark("(see Licensing tab)."));
        } else {
            final String licenseKey = Version.getLicenseKey();
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(DCLabel.dark("License key: " + licenseKey));
        }

        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(DCLabel.dark("Java runtime information:"));
        contentPanel.add(DCLabel.dark("  " + System.getProperty("java.vm.name")));
        contentPanel.add(DCLabel.dark("  by " + System.getProperty("java.vm.vendor")));
        contentPanel.add(DCLabel.dark("  version " + System.getProperty("java.runtime.version")));
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(buttonPanel);

        final DCPanel mainPanel = new DCPanel(imageManager.getImage("images/window/app-icon-hires.png"), 97, 10,
                WidgetUtils.COLOR_DEFAULT_BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new VerticalLayout());
        mainPanel.add(contentPanel);

        return mainPanel;
    }

    @Override
    public String getWindowTitle() {
        return "About DataCleaner | DataCleaner";
    }

    public static void main(final String[] args) {
        new AboutDialog(new DCWindowContext(null, null, null)).setVisible(true);
    }
}
