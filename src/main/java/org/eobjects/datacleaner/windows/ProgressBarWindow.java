package org.eobjects.datacleaner.windows;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.eobjects.analyzer.job.AnalysisJob;
import org.eobjects.analyzer.job.AnalyzerJob;
import org.eobjects.analyzer.job.FilterJob;
import org.eobjects.analyzer.job.TransformerJob;
import org.eobjects.analyzer.job.runner.AnalysisListener;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.datacleaner.panels.DCPanel;
import org.eobjects.datacleaner.util.ImageManager;
import org.jdesktop.swingx.VerticalLayout;

import dk.eobjects.metamodel.schema.Table;

public class ProgressBarWindow extends AbstractWindow implements AnalysisListener {

	private static final long serialVersionUID = 1L;

	private final DCPanel panel = new DCPanel();
	private final Map<Table, JProgressBar> progressBars = new HashMap<Table, JProgressBar>();

	public ProgressBarWindow() {
		super();
	}

	@Override
	protected boolean isWindowResizable() {
		return true;
	}

	@Override
	protected boolean isCentered() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return "Progress...";
	}

	@Override
	protected Image getWindowIcon() {
		return ImageManager.getInstance().getImage("images/actions/execute.png");
	}

	@Override
	protected JComponent getWindowContent() {
		panel.setLayout(new VerticalLayout(4));
		panel.setPreferredSize(200, 200);
		return panel;
	}

	@Override
	public void jobBegin(AnalysisJob job) {
	}

	@Override
	public void jobSuccess(AnalysisJob job) {
	}

	@Override
	public void rowProcessingBegin(AnalysisJob job, Table table, int expectedRows) {
		panel.add(new JLabel(table.getName() + " (" + expectedRows + " rows):"));
		JProgressBar progressBar = new JProgressBar();
		progressBar.setMaximum(expectedRows);
		progressBars.put(table, progressBar);
		panel.add(progressBar);
		panel.updateUI();
	}

	@Override
	public void rowProcessingProgress(AnalysisJob job, Table table, int currentRow) {
		JProgressBar progressBar = progressBars.get(table);
		if (currentRow > progressBar.getValue()) {
			progressBar.setValue(currentRow);
		}
	}

	@Override
	public void rowProcessingSuccess(AnalysisJob job, Table table) {
		JProgressBar progressBar = progressBars.get(table);
		progressBar.setValue(progressBar.getMaximum());
	}

	@Override
	public void analyzerBegin(AnalysisJob job, AnalyzerJob analyzerJob) {
	}

	@Override
	public void analyzerSuccess(AnalysisJob job, AnalyzerJob analyzerJob, AnalyzerResult result) {
	}

	@Override
	public void errorInFilter(AnalysisJob job, FilterJob filterJob, Throwable throwable) {
	}

	@Override
	public void errorInTransformer(AnalysisJob job, TransformerJob transformerJob, Throwable throwable) {
	}

	@Override
	public void errorInAnalyzer(AnalysisJob job, AnalyzerJob analyzerJob, Throwable throwable) {
	}

	@Override
	public void errorUknown(AnalysisJob job, Throwable throwable) {
	}

}
