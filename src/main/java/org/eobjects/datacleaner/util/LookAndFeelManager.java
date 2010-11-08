package org.eobjects.datacleaner.util;

import java.util.Set;

import javax.swing.LookAndFeel;
import javax.swing.PopupFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.eobjects.datacleaner.widgets.tooltip.DCPopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * Class that encapsulates all central configuration of look and feel and
 * similar Swing constructs.
 * 
 * @author Kasper Sørensen
 * 
 */
public final class LookAndFeelManager {

	private static final Logger logger = LoggerFactory.getLogger(LookAndFeelManager.class);
	private static final LookAndFeelManager instance = new LookAndFeelManager();

	public static LookAndFeelManager getInstance() {
		return instance;
	}

	private LookAndFeelManager() {
	}

	public void init() {

		try {
			LookAndFeel laf = new PlasticXPLookAndFeel();
			UIManager.setLookAndFeel(laf);
			logger.info("Look and feel set to: {}", UIManager.getLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			throw new IllegalStateException(e);
		}

		Set<Object> propertyKeys = UIManager.getLookAndFeelDefaults().keySet();

		for (Object propertyKey : propertyKeys) {
			if (propertyKey instanceof String) {
				String str = (String) propertyKey;

				if (str.endsWith(".font")) {
					// set default font
					UIManager.put(propertyKey, WidgetUtils.FONT_NORMAL);
				} else if (str.endsWith(".background")) {
					// set default background color
					UIManager.put(propertyKey, WidgetUtils.BG_COLOR_BRIGHT);
				}
			}
		}

		ToolTipManager.sharedInstance().setInitialDelay(500);
		PopupFactory.setSharedInstance(new DCPopupFactory());

		UIManager.put("ScrollPane.border", new EmptyBorder(0, 0, 0, 0));

		// white background for input components
		UIManager.put("Tree.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("TextArea.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("PasswordField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("FormattedTextField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("EditorPane.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("ComboBox.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("TextField.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("Spinner.background", WidgetUtils.BG_COLOR_BRIGHTEST);

		// table header styling
		UIManager.put("TableHeader.background", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("TableHeader.focusCellBackground", WidgetUtils.BG_COLOR_LESS_DARK);
		UIManager.put("TableHeader.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("TableHeader.cellBorder", new LineBorder(WidgetUtils.BG_COLOR_LESS_DARK));

		// tool tip colors
		UIManager.put("ToolTip.background", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("ToolTip.foreground", WidgetUtils.BG_COLOR_BRIGHTEST);

		// task pane colors
		UIManager.put("TaskPaneContainer.background", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("TaskPane.titleForeground", WidgetUtils.BG_COLOR_BRIGHTEST);
		UIManager.put("TaskPane.titleBackgroundGradientStart", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("TaskPane.titleBackgroundGradientEnd", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("TaskPane.borderColor", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("TaskPane.background", WidgetUtils.BG_COLOR_BRIGHT);

		// scrollbar color
		UIManager.put("ScrollBar.thumb", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("ScrollBar.thumbHighlight", WidgetUtils.BG_COLOR_DARK);
		UIManager.put("ScrollBar.thumbShadow", WidgetUtils.BG_COLOR_DARK);

		// progressbar color
		UIManager.put("ProgressBar.foreground", WidgetUtils.BG_COLOR_DARK);
	}
}
