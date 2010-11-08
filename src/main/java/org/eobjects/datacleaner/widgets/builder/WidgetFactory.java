package org.eobjects.datacleaner.widgets.builder;

import java.awt.Image;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.eobjects.datacleaner.util.ImageManager;
import org.jdesktop.swingx.JXTaskPaneContainer;

public final class WidgetFactory {

	public static <E extends JComponent> WidgetBuilder<E> create(Class<E> componentClazz) {
		try {
			E component = componentClazz.newInstance();
			WidgetBuilder<E> widgetBuilder = new WidgetBuilder<E>(component);
			return widgetBuilder;
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not instantiate: " + componentClazz);
		}
	}

	public static MenuBuilder<JMenu> createMenu(String text, char mnemonic) {
		WidgetBuilder<JMenu> wb = create(JMenu.class);
		JMenu component = wb.toComponent();
		wb.applyTooltip(text);
		component.setText(text);
		component.setMnemonic(mnemonic);
		return new MenuBuilder<JMenu>(wb.toComponent());
	}

	public static MenuBuilder<JMenuItem> createMenuItem(String text, String iconPath) {
		WidgetBuilder<JMenuItem> wb = create(JMenuItem.class);
		JMenuItem component = wb.toComponent();
		wb.applyTooltip(text);
		component.setText(text);
		if (iconPath != null) {
			component.setIcon(ImageManager.getInstance().getImageIcon(iconPath));
		}
		return new MenuBuilder<JMenuItem>(wb.toComponent());
	}

	public static WidgetBuilder<JButton> createButton(String text, String imagePath) {
		WidgetBuilder<JButton> bb = create(JButton.class);
		bb.toComponent().setText(text);
		if (imagePath != null) {
			bb.toComponent().setIcon(ImageManager.getInstance().getImageIcon(imagePath));
		}
		return bb;
	}

	public static JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setRollover(true);
		toolbar.setFloatable(false);
		toolbar.setAlignmentY(JToolBar.LEFT_ALIGNMENT);
		return toolbar;
	}

	public static WidgetBuilder<JMenuBar> createMenuBar() {
		WidgetBuilder<JMenuBar> wb = new WidgetBuilder<JMenuBar>(new JMenuBar());
		return wb;
	}

	public static WidgetBuilder<JLabel> createLabel(Icon icon) {
		JLabel label = new JLabel(icon);
		WidgetBuilder<JLabel> wb = new WidgetBuilder<JLabel>(label);
		return wb;
	}

	public static WidgetBuilder<JLabel> createLabel(String text) {
		JLabel label = new JLabel(text);
		WidgetBuilder<JLabel> wb = new WidgetBuilder<JLabel>(label);
		return wb;
	}

	public static WidgetBuilder<JButton> createSmallButton(String imagePath) {
		WidgetBuilder<JButton> wb = create(JButton.class);
		Image image = ImageManager.getInstance().getImage(imagePath, 16);
		ImageIcon imageIcon = new ImageIcon(image);
		JButton button = wb.toComponent();
		button.setIcon(imageIcon);
		button.setMargin(new Insets(0, 0, 0, 0));
		return wb;
	}

	public static WidgetBuilder<JPopupMenu> createPopupMenu() {
		return create(JPopupMenu.class);
	}

	public static JXTaskPaneContainer createTaskPaneContainer() {
		JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();
		taskPaneContainer.setOpaque(false);
		taskPaneContainer.setBackgroundPainter(null);
		return taskPaneContainer;
	}
}
