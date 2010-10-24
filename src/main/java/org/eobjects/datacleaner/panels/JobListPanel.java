package org.eobjects.datacleaner.panels;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.eobjects.datacleaner.util.ImageManager;
import org.eobjects.datacleaner.widgets.builder.WidgetFactory;

public class JobListPanel extends DCPanel {

	private static final long serialVersionUID = 1L;

	public JobListPanel() {
		super();

		setLayout(new BorderLayout());

		JToolBar toolBar = WidgetFactory.createToolBar();
		JButton addAnalysisJobItem = new JButton(ImageManager.getInstance().getImageIcon("images/actions/create_job.png"));
		addAnalysisJobItem.setToolTipText("Add analysis job");
		toolBar.add(addAnalysisJobItem);

		add(toolBar, BorderLayout.NORTH);
	}
}
