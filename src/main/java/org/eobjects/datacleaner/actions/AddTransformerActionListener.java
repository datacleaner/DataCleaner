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
package org.eobjects.datacleaner.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPopupMenu;

import org.eobjects.analyzer.beans.coalesce.CoalesceDatesTransformer;
import org.eobjects.analyzer.beans.coalesce.CoalesceNumbersTransformer;
import org.eobjects.analyzer.beans.coalesce.CoalesceStringsTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToBooleanTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToDateTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.beans.convert.ConvertToStringTransformer;
import org.eobjects.analyzer.beans.script.JavaScriptTransformer;
import org.eobjects.analyzer.beans.standardize.EmailStandardizerTransformer;
import org.eobjects.analyzer.beans.standardize.NameStandardizerTransformer;
import org.eobjects.analyzer.beans.standardize.TokenizerTransformer;
import org.eobjects.analyzer.beans.standardize.UrlStandardizerTransformer;
import org.eobjects.analyzer.beans.transform.ConcatenatorTransformer;
import org.eobjects.analyzer.beans.transform.DateDiffTransformer;
import org.eobjects.analyzer.beans.transform.DateMaskMatcherTransformer;
import org.eobjects.analyzer.beans.transform.DateToAgeTransformer;
import org.eobjects.analyzer.beans.transform.ELTransformer;
import org.eobjects.analyzer.beans.transform.StringLengthTransformer;
import org.eobjects.analyzer.beans.transform.StringPatternMatcherTransformer;
import org.eobjects.analyzer.beans.transform.WhitespaceTrimmerTransformer;
import org.eobjects.analyzer.beans.transform.XmlDecoderTransformer;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.descriptors.TransformerBeanDescriptor;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.util.CollectionUtils;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.DisplayNameComparator;
import org.eobjects.datacleaner.widgets.tooltip.DescriptorMenu;
import org.eobjects.datacleaner.widgets.tooltip.DescriptorMenuItem;

public final class AddTransformerActionListener implements ActionListener {

	private final AnalyzerBeansConfiguration _configuration;
	private final AnalysisJobBuilder _analysisJobBuilder;

	public AddTransformerActionListener(AnalyzerBeansConfiguration configuration, AnalysisJobBuilder analysisJobBuilder) {
		_configuration = configuration;
		_analysisJobBuilder = analysisJobBuilder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final List<DescriptorMenu> descriptorMenus = new ArrayList<DescriptorMenu>();
		descriptorMenus.add(new DescriptorMenu("Conversion").addComponentClasses(ConvertToStringTransformer.class,
				ConvertToNumberTransformer.class, ConvertToDateTransformer.class, ConvertToBooleanTransformer.class)
				.setIconDecoration("images/component-types/type_convert.png"));
		descriptorMenus.add(new DescriptorMenu("Coalesce").addComponentClasses(CoalesceStringsTransformer.class,
				CoalesceNumbersTransformer.class, CoalesceDatesTransformer.class).setIconDecoration(
				"images/component-types/type_coalesce.png"));
		descriptorMenus.add(new DescriptorMenu("String manipulation").addComponentClasses(
				WhitespaceTrimmerTransformer.class, StringLengthTransformer.class, TokenizerTransformer.class,
				ConcatenatorTransformer.class, XmlDecoderTransformer.class, CoalesceStringsTransformer.class)
				.setIconDecoration("images/component-types/type_expression.png"));
		descriptorMenus.add(new DescriptorMenu("Matching and standardization").addComponentClasses(
				EmailStandardizerTransformer.class, UrlStandardizerTransformer.class, NameStandardizerTransformer.class,
				StringPatternMatcherTransformer.class, DateMaskMatcherTransformer.class).setIconDecoration(
				"images/component-types/type_match.png"));
		descriptorMenus.add(new DescriptorMenu("Date and time").addComponentClasses(DateDiffTransformer.class,
				DateMaskMatcherTransformer.class, DateToAgeTransformer.class, CoalesceDatesTransformer.class,
				ConvertToDateTransformer.class).setIconDecoration("images/component-types/type_date_time_analyzer.png"));
		descriptorMenus.add(new DescriptorMenu("Scripting").addComponentClasses(JavaScriptTransformer.class,
				ELTransformer.class).setIconDecoration("images/component-types/transformer.png"));

		final JPopupMenu popup = new JPopupMenu();

		for (DescriptorMenu descriptorMenu : descriptorMenus) {
			popup.add(descriptorMenu);
		}

		Collection<TransformerBeanDescriptor<?>> descriptors = _configuration.getDescriptorProvider()
				.getTransformerBeanDescriptors();
		descriptors = CollectionUtils.sorted(descriptors, new DisplayNameComparator());
		for (final TransformerBeanDescriptor<?> descriptor : descriptors) {
			boolean placedInSubmenu = false;
			Class<?> componentClass = descriptor.getComponentClass();
			for (DescriptorMenu descriptorMenu : descriptorMenus) {
				if (descriptorMenu.containsComponentClass(componentClass)) {
					descriptorMenu.add(getMenuItem(descriptor));
					placedInSubmenu = true;
				}
			}

			if (!placedInSubmenu) {
				popup.add(getMenuItem(descriptor));
			}
		}

		Component source = (Component) e.getSource();
		popup.show(source, 0, source.getHeight());
	}

	private DescriptorMenuItem getMenuItem(final TransformerBeanDescriptor<?> descriptor) {
		final DescriptorMenuItem menuItem = new DescriptorMenuItem(descriptor);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_analysisJobBuilder.addTransformer(descriptor);
				UsageLogger.getInstance().log("Add transformer: " + descriptor.getDisplayName());
			}
		});
		return menuItem;
	}
}
