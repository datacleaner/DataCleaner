package org.eobjects.datacleaner.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.analyzer.job.builder.FilterJobBuilder;
import org.eobjects.analyzer.job.builder.TransformerJobBuilder;
import org.eobjects.datacleaner.util.IconUtils;
import org.eobjects.datacleaner.util.ImageManager;

/**
 * menu item and action listener for removing a component from a job.
 */
public class RemoveComponentMenuItem extends JMenuItem implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final AnalysisJobBuilder _analysisJobBuilder;
    private final AbstractBeanJobBuilder<?, ?, ?> _componentBuilder;

    public RemoveComponentMenuItem(AnalysisJobBuilder analysisJobBuilder, AbstractBeanJobBuilder<?, ?, ?> componentBuilder) {
        super("Remove component", ImageManager.get().getImageIcon(IconUtils.ACTION_REMOVE));
        _analysisJobBuilder = analysisJobBuilder;
        _componentBuilder = componentBuilder;
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (_componentBuilder instanceof AnalyzerJobBuilder) {
            _analysisJobBuilder.removeAnalyzer((AnalyzerJobBuilder<?>) _componentBuilder);
        } else if (_componentBuilder instanceof TransformerJobBuilder) {
            _analysisJobBuilder.removeTransformer((TransformerJobBuilder<?>) _componentBuilder);
        } else if (_componentBuilder instanceof FilterJobBuilder) {
            _analysisJobBuilder.removeFilter((FilterJobBuilder<?, ?>) _componentBuilder);
        } else {
            throw new IllegalStateException("Unexpected component type: " + _componentBuilder);
        }
        onRemoved();
    }

    protected void onRemoved() {
    }
}
