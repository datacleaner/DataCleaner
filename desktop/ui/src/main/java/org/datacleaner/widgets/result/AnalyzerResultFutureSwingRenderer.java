package org.datacleaner.widgets.result;

import javax.inject.Inject;
import javax.swing.JComponent;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.result.AnalyzerResultFuture;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;

@RendererBean(SwingRenderingFormat.class)
public class AnalyzerResultFutureSwingRenderer implements Renderer<AnalyzerResultFuture<? extends AnalyzerResult>, JComponent> {

    @Inject
    RendererFactory _rendererFactory;
    
    @Override
    public RendererPrecedence getPrecedence(AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        return RendererPrecedence.LOW;
    }

    @Override
    public JComponent render(AnalyzerResultFuture<? extends AnalyzerResult> renderable) {
        // TODO: display a loading indicator
        
        renderable.addListener(new AnalyzerResultFuture.Listener<AnalyzerResult>() {

            @Override
            public void onSuccess(AnalyzerResult result) {
                Renderer<? super AnalyzerResult, ? extends JComponent> renderer = _rendererFactory.getRenderer(result, SwingRenderingFormat.class);
                // TODO: check if null?
                
                JComponent jComponent = renderer.render(result);
                
                // TODO: Remove loading indicator, replace with rendered result
            }

            @Override
            public void onError(RuntimeException error) {
                // TODO Auto-generated method stub
            }
            
        });
        
        // TODO: return the loading indicator in some panel
        return null;
    }

}
