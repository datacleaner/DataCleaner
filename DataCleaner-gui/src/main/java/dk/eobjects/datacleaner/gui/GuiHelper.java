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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.error.ErrorLevel;

import dk.eobjects.datacleaner.data.ColumnSelection;
import dk.eobjects.datacleaner.util.ReflectionHelper;
import dk.eobjects.metamodel.schema.Column;
import dk.eobjects.metamodel.schema.Table;

public class GuiHelper {

	private static Log _log = LogFactory.getLog(GuiHelper.class);

	public static final Font FONT_HEADER = new Font("Sans", Font.BOLD, 15);
	public static final Font FONT_MONOSPACE = new Font("Monospaced",
			Font.PLAIN, 14);
	public static final Font FONT_NORMAL = new Font("Arial", Font.PLAIN, 12);
	public static final String NULL_STRING = "<null>";
	public static final Color BG_COLOR_LIGHT = new Color(243, 243, 243);
	public static final Color BG_COLOR_DARK = new Color(191, 192, 200);
	public static final Color BG_COLOR_DARKBLUE = new Color(80, 84, 104);
	public static final Border BORDER_WIDE = new LineBorder(
			GuiHelper.BG_COLOR_DARKBLUE, 4);
	public static final CompoundBorder BORDER_THIN = new CompoundBorder(
			new LineBorder(Color.DARK_GRAY), new EmptyBorder(2, 2, 2, 2));

	/**
	 * A highlighter for coloring odd/even rows in a JXTable
	 */
	public static final Highlighter LIBERELLO_HIGHLIGHTER = HighlighterFactory
			.createAlternateStriping(HighlighterFactory.BEIGE,
					HighlighterFactory.FLORAL_WHITE);

	public static void confirmExit() {
		int feedback = JOptionPane.showConfirmDialog(null,
				"Are you sure you want to exit DataCleaner?", "Exit",
				JOptionPane.YES_NO_OPTION);
		if (feedback == JOptionPane.YES_OPTION) {
			_log.info("DataCleaner-gui shutting down.");
			System.exit(DataCleanerGui.EXIT_CODE_NORMAL_EXIT);
		}
	}

	public static GuiBuilder<JPanel> createPanel() {
		return new GuiBuilder<JPanel>(new JPanel()).applyLightBackground();
	}

	public static GuiBuilder<JCheckBox> createCheckBox(String text,
			boolean selected) {
		return new GuiBuilder<JCheckBox>(new JCheckBox(text, selected))
				.applyLightBackground();
	}

	public static GuiBuilder<JRadioButton> createRadio(String text,
			ButtonGroup buttonGroup) {
		JRadioButton component = new JRadioButton(text);
		if (buttonGroup != null) {
			buttonGroup.add(component);
		}
		return new GuiBuilder<JRadioButton>(component).applyLightBackground();
	}

	public static GuiBuilder<JTextArea> createLabelTextArea() {
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		return new GuiBuilder<JTextArea>(textArea).applyLightBackground()
				.applyBorder();
	}

	public static GuiBuilder<JButton> createButton(String label,
			String iconFilename) {
		JButton button = new JButton();
		if (label != null) {
			button.setText(label);
			button.setToolTipText(label);
		}
		if (iconFilename != null) {
			button.setIcon(getImageIcon(iconFilename));
		}
		return new GuiBuilder<JButton>(button).applyLightBackground();
	}

	public static JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setRollover(true);
		toolbar.setFloatable(false);
		toolbar.setBackground(GuiHelper.BG_COLOR_LIGHT);
		return toolbar;
	}

	public static void centerOnScreen(Component component) {
		Dimension paneSize = component.getSize();
		Dimension screenSize = component.getToolkit().getScreenSize();
		component.setLocation((screenSize.width - paneSize.width) / 2,
				(screenSize.height - paneSize.height) / 2);
	}

	public static String getLicenceText() {
		try {
			return getResourceContent("licence.txt");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCreditsText() {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("version", DataCleanerGui.VERSION);
			StrSubstitutor substitutor = new StrSubstitutor(map);
			return substitutor.replace(getResourceContent("credits.txt"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getChangelogText() {
		try {
			return getResourceContent("changelog.txt");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getResourceContent(String filename)
			throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				ClassLoader.getSystemResourceAsStream(filename), "UTF8"));
		StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			sb.append(line);
			line = reader.readLine();
			if (line != null) {
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	public static void showErrorMessage(String shortMessage,
			String detailedMessage, Throwable exception) {
		JXErrorPane.setDefaultLocale(Locale.ENGLISH);
		JXErrorPane errorPane = new JXErrorPane();
		ErrorInfo info = new ErrorInfo(shortMessage, detailedMessage, null,
				"error", exception, ErrorLevel.SEVERE, null);
		errorPane.setErrorInfo(info);
		JDialog dialog = JXErrorPane.createDialog(DataCleanerGui
				.getMainWindow().getFrame(), errorPane);
		dialog.setTitle(shortMessage);
		dialog.setVisible(true);
	}

	public static ImageIcon getImageIcon(String imagePath) {
		return new ImageIcon(getImage(imagePath));
	}

	public static Image getImage(String imagePath) {
		try {
			return ImageIO.read(ClassLoader.getSystemResource(imagePath));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String getLabelForTable(Table table) {
		return table.getName() + ".*";
	}

	public static String getLabelForColumn(Column column) {
		Table table = column.getTable();
		return table.getName() + "." + column.getName();
	}

	public static Column getColumnByLabel(ColumnSelection columnSelection,
			String label) {
		List<Column> columns = columnSelection.getColumns();
		Object[] tables = ReflectionHelper.getProperties(columns, "table");
		for (int i = 0; i < tables.length; i++) {
			String tablePrefix = ((Table) tables[i]).getName() + ".";
			if (label.startsWith(tablePrefix)) {
				String columnName = label.substring(tablePrefix.length());
				Column column = columns.get(i);
				if (columnName.equals(column.getName())) {
					return column;
				}
			}
		}
		return null;
	}

	public static void addComponentAligned(Container container,
			JComponent component) {
		component.setAlignmentX(Component.LEFT_ALIGNMENT);
		component.setAlignmentY(Component.TOP_ALIGNMENT);
		container.add(component);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx,
			int gridy, int width, int height, int anchor) {
		LayoutManager layout = panel.getLayout();
		if (!(layout instanceof GridBagLayout)) {
			layout = new GridBagLayout();
			panel.setLayout(layout);
		}
		GridBagLayout gridBagLayout = (GridBagLayout) layout;
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = gridx;
		constraints.gridy = gridy;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.anchor = anchor;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.ipadx = 4;
		constraints.ipady = 4;
		gridBagLayout.addLayoutComponent(comp, constraints);
		panel.add(comp);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx,
			int gridy, int width, int height) {
		addToGridBag(comp, panel, gridx, gridy, width, height,
				GridBagConstraints.WEST);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx,
			int gridy) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1);
	}

	/**
	 * Adds a component to a panel with a grid bag layout
	 */
	public static void addToGridBag(JComponent comp, JPanel panel, int gridx,
			int gridy, int anchor) {
		addToGridBag(comp, panel, gridx, gridy, 1, 1, anchor);
	}
}