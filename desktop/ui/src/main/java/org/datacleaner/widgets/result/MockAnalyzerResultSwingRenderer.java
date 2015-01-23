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

import java.awt.Color;

import javax.swing.JComponent;

import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.beans.MockAnalyzerResult;
import org.datacleaner.panels.DCPanel;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.widgets.DCLabel;

@RendererBean(SwingRenderingFormat.class)
public class MockAnalyzerResultSwingRenderer implements Renderer<MockAnalyzerResult, JComponent> {
    
    @Override
    public RendererPrecedence getPrecedence(MockAnalyzerResult renderable) {
        return RendererPrecedence.MEDIUM;
    }

    @Override
    public JComponent render(MockAnalyzerResult renderable) {
        DCLabel resultLabel = new DCLabel(false, renderable.getMockMessage(), Color.GREEN, null);
        
        final DCPanel resultPanel = new DCPanel();
        resultPanel.add(resultLabel);
        
        return resultPanel;
    }

}
