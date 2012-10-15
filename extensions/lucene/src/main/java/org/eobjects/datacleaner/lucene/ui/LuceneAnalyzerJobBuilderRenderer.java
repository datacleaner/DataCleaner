/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
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
package org.eobjects.datacleaner.lucene.ui;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RendererPrecedence;
import org.eobjects.analyzer.job.builder.AnalyzerJobBuilder;
import org.eobjects.datacleaner.bootstrap.WindowContext;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.lucene.SearchIndexCatalog;
import org.eobjects.datacleaner.lucene.WriteSearchIndexAnalyzer;
import org.eobjects.datacleaner.panels.AnalyzerJobBuilderPresenter;
import org.eobjects.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.eobjects.datacleaner.user.UserPreferences;
import org.eobjects.datacleaner.widgets.properties.PropertyWidgetFactory;

@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class LuceneAnalyzerJobBuilderRenderer implements
        Renderer<AnalyzerJobBuilder<WriteSearchIndexAnalyzer>, AnalyzerJobBuilderPresenter> {

    @Inject
    InjectorBuilder injectorBuilder;

    @Inject
    WindowContext windowContext;

    @Inject
    UserPreferences userPreferences;

    @Override
    public RendererPrecedence getPrecedence(AnalyzerJobBuilder<WriteSearchIndexAnalyzer> ajb) {
        if (ajb.getDescriptor().getComponentClass() == WriteSearchIndexAnalyzer.class) {
            return RendererPrecedence.HIGH;
        }
        return RendererPrecedence.NOT_CAPABLE;
    }

    @Override
    public AnalyzerJobBuilderPresenter render(AnalyzerJobBuilder<WriteSearchIndexAnalyzer> ajb) {
        final SearchIndexCatalog catalog = SearchIndexCatalogFactory.getInstance(userPreferences);

        final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder.with(
                PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, ajb).getInstance(PropertyWidgetFactory.class);
        return new WriteSearchIndexAnalyzerJobBuilderPanel(ajb, propertyWidgetFactory, catalog, windowContext, userPreferences);
    }

}
