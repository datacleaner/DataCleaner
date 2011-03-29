package org.eobjects.datacleaner.widgets;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.eobjects.datacleaner.util.ImageManager;
import org.jdesktop.swingx.action.OpenBrowserAction;

public class HumanInferenceToolbarButton extends JButton {

	private static final long serialVersionUID = 1L;

	public HumanInferenceToolbarButton() {
		this(ImageManager.getInstance().getImageIcon("images/powered-by-human-inference-dark.png"));
	}

	public HumanInferenceToolbarButton(Icon icon) {
		super(icon);
		addActionListener(new OpenBrowserAction("http://www.humaninference.com"));
		setOpaque(false);
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setToolTipText("Powered by Human Inference");
	}
}
