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
import org.jdesktop.swingx.JXBusyLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RendererBean(SwingRenderingFormat.class)
public class AnalyzerResultFutureSwingRenderer implements Renderer<AnalyzerResultFuture<? extends AnalyzerResult>, JComponent> {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyzerResultFutureSwingRenderer.class);

    @Inject
    RendererFactory _rendererFactory;
    
    @Override
    public RendererPrecedence getPrecedence(AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        return RendererPrecedence.LOW;
    }

    @Override
    public JComponent render(AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        final JXBusyLabel busyLabel = new JXBusyLabel();
        busyLabel.setBusy(true);
        
        final DCPanel resultPanel = new DCPanel();
        resultPanel.add(busyLabel);
        resultPanel.updateUI();
        
        
        renderable.addListener(new AnalyzerResultFuture.Listener<AnalyzerResult>() {

            @Override
            public void onSuccess(AnalyzerResult result) {
                Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(result, SwingRenderingFormat.class);
                if (renderer != null) {
                    JComponent jComponent = renderer.render(result);
                    resultPanel.add(jComponent);
                } else {
                    logger.error("No renderer found for: " + result);
                }
                
                resultPanel.remove(busyLabel);
                resultPanel.updateUI();
                
            }

            @Override
            public void onError(RuntimeException error) {
                logger.error("Error occured while retrieving AnalyzerResult from AnalyzerResultFuture" + error);
            }
            
        });
        
        return resultPanel;
    }
    
}
