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
package org.datacleaner.beans;

import java.util.ArrayList;
import java.util.List;

import org.datacleaner.result.Crosstab;
import org.datacleaner.result.html.BodyElement;
import org.datacleaner.result.html.CompositeBodyElement;
import org.datacleaner.result.html.HeadElement;
import org.datacleaner.result.html.HtmlFragment;
import org.datacleaner.result.html.HtmlRenderingContext;
import org.datacleaner.result.html.SimpleHtmlFragment;
import org.datacleaner.result.renderer.CrosstabHtmlRenderer;
import org.datacleaner.result.renderer.RendererFactory;

public class BooleanAnalyzerHtmlFragment implements HtmlFragment {

    private final RendererFactory rendererFactory;
    private final BooleanAnalyzerResult result;
    private final SimpleHtmlFragment frag = new SimpleHtmlFragment();

    public BooleanAnalyzerHtmlFragment(RendererFactory rendererFactory, BooleanAnalyzerResult result) {
        this.rendererFactory = rendererFactory;
        this.result = result;
    }

    @Override
    public void initialize(HtmlRenderingContext context) {
        // render the two crosstabs in this result
        final CrosstabHtmlRenderer crosstabRenderer = new CrosstabHtmlRenderer(rendererFactory);

        final Crosstab<Number> columnStatisticsCrosstab = result.getColumnStatisticsCrosstab();
        final HtmlFragment columnStatisticsHtmlFragment =
                columnStatisticsCrosstab == null ? null : crosstabRenderer.render(columnStatisticsCrosstab);

        final Crosstab<Number> valueCombinationCrosstab = result.getValueCombinationCrosstab();
        final HtmlFragment valueCombinationHtmlFragment =
                valueCombinationCrosstab == null ? null : crosstabRenderer.render(valueCombinationCrosstab);

        // add all head elements to the html fragment
        if (columnStatisticsHtmlFragment != null) {
            columnStatisticsHtmlFragment.initialize(context);
            columnStatisticsHtmlFragment.getHeadElements().forEach(e -> frag.addHeadElement(e));
        }
        if (valueCombinationHtmlFragment != null) {
            valueCombinationHtmlFragment.initialize(context);
            valueCombinationHtmlFragment.getHeadElements().forEach(e -> frag.addHeadElement(e));
        }

        // make a composite body element
        final List<BodyElement> bodyElements = new ArrayList<BodyElement>();
        if (columnStatisticsHtmlFragment != null) {
            bodyElements.addAll(columnStatisticsHtmlFragment.getBodyElements());
        }
        if (valueCombinationHtmlFragment != null) {
            bodyElements.addAll(valueCombinationHtmlFragment.getBodyElements());
        }
        final CompositeBodyElement composite = new CompositeBodyElement("booleanAnalyzerResult", bodyElements);
        frag.addBodyElement(composite);
    }

    @Override
    public List<BodyElement> getBodyElements() {
        return frag.getBodyElements();
    }

    @Override
    public List<HeadElement> getHeadElements() {
        return frag.getHeadElements();
    }
}
