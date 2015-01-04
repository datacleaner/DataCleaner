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
package org.datacleaner.testtools.ui;

import javax.inject.Inject;

import org.datacleaner.beans.api.Analyzer;
import org.datacleaner.beans.api.Renderer;
import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.beans.api.RendererPrecedence;
import org.datacleaner.job.builder.AnalyzerJobBuilder;
import org.datacleaner.util.ReflectionUtils;
import org.datacleaner.guice.InjectorBuilder;
import org.datacleaner.panels.AnalyzerJobBuilderPresenter;
import org.datacleaner.panels.ComponentJobBuilderRenderingFormat;
import org.datacleaner.testtools.TestToolAnalyzer;
import org.datacleaner.widgets.properties.PropertyWidgetFactory;

@RendererBean(ComponentJobBuilderRenderingFormat.class)
public class TestToolConfigurationPanelRenderer implements
		Renderer<AnalyzerJobBuilder<Analyzer<?>>, AnalyzerJobBuilderPresenter> {

	@Inject
	InjectorBuilder injectorBuilder;

	@Override
	public RendererPrecedence getPrecedence(AnalyzerJobBuilder<Analyzer<?>> ajb) {
		Class<Analyzer<?>> cls = ajb.getDescriptor().getComponentClass();
		if (ReflectionUtils.is(cls, TestToolAnalyzer.class)) {
			return RendererPrecedence.HIGH;
		}
		return RendererPrecedence.NOT_CAPABLE;
	}

	@Override
	public AnalyzerJobBuilderPresenter render(
			AnalyzerJobBuilder<Analyzer<?>> ajb) {
		final PropertyWidgetFactory propertyWidgetFactory = injectorBuilder
				.with(PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, ajb)
				.getInstance(PropertyWidgetFactory.class);
		return new TestToolConfigurationPanel(ajb, propertyWidgetFactory);
	}

}
