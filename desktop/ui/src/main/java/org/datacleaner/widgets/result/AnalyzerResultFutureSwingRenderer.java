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
package org.datacleaner.widgets.result;

import javax.inject.Inject;
import javax.swing.JComponent;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.AnalyzerResultFuture;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.util.WidgetUtils;
import org.datacleaner.widgets.LoadingIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RendererBean(SwingRenderingFormat.class)
public class AnalyzerResultFutureSwingRenderer
        implements Renderer<AnalyzerResultFuture<? extends AnalyzerResult>, JComponent> {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultFutureSwingRenderer.class);

    @Inject
    RendererFactory _rendererFactory;

    @Override
    public RendererPrecedence getPrecedence(final AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        return RendererPrecedence.LOW;
    }

    @Override
    public JComponent render(final AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        final LoadingIcon loadingIcon = new LoadingIcon();

        final DCPanel resultPanel = new DCPanel();
        resultPanel.add(loadingIcon);
        resultPanel.updateUI();

        renderable.addListener(new AnalyzerResultFuture.Listener<AnalyzerResult>() {

            @Override
            public void onSuccess(final AnalyzerResult result) {
                try {
                    final Renderer<? super AnalyzerResult, ? extends JComponent> renderer =
                            _rendererFactory.getRenderer(result, SwingRenderingFormat.class);
                    if (renderer != null) {
                        logger.debug("renderer.render({})", result);
                        final JComponent component = renderer.render(result);
                        resultPanel.add(component);
                    } else {
                        final String message = "No renderer found for result type " + result.getClass().getName();
                        logger.error(message);
                        throw new IllegalStateException(message);
                    }
                } finally {
                    resultPanel.remove(loadingIcon);
                    resultPanel.updateUI();
                }
            }

            @Override
            public void onError(final RuntimeException error) {
                WidgetUtils.showErrorMessage("Unable to fetch result", error);
            }

        });

        return resultPanel;
    }

}
