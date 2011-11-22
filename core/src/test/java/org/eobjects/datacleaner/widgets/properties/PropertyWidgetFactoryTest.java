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
package org.eobjects.datacleaner.widgets.properties;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.lang.ArrayUtils;
import org.eobjects.analyzer.beans.filter.ValidationCategory;
import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.analyzer.connection.CsvDatastore;
import org.eobjects.analyzer.connection.ExcelDatastore;
import org.eobjects.analyzer.data.InputColumn;
import org.eobjects.analyzer.data.MockInputColumn;
import org.eobjects.analyzer.descriptors.AnalyzerBeanDescriptor;
import org.eobjects.analyzer.descriptors.Descriptors;
import org.eobjects.analyzer.job.builder.AbstractBeanJobBuilder;
import org.eobjects.analyzer.job.builder.AnalysisJobBuilder;
import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.SimpleDictionary;
import org.eobjects.analyzer.reference.SimpleStringPattern;
import org.eobjects.analyzer.reference.SimpleSynonymCatalog;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.reference.TextFileSynonymCatalog;
import org.eobjects.datacleaner.guice.DCModule;
import org.eobjects.datacleaner.guice.InjectorBuilder;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.metamodel.util.EqualsBuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PropertyWidgetFactoryTest extends TestCase {

	private SimpleDictionary dict1 = new SimpleDictionary("dict1");
	private SimpleDictionary dict2 = new SimpleDictionary("dict2");
	private SimpleDictionary dict3 = new SimpleDictionary("dict3");
	private SimpleSynonymCatalog syn1 = new SimpleSynonymCatalog("syn1");
	private SimpleSynonymCatalog syn2 = new SimpleSynonymCatalog("syn2");
	private SimpleSynonymCatalog syn3 = new SimpleSynonymCatalog("syn3");
	private SimpleStringPattern stringPattern1 = new SimpleStringPattern("sp1", "");
	private SimpleStringPattern stringPattern2 = new SimpleStringPattern("sp2", "");
	private SimpleStringPattern stringPattern3 = new SimpleStringPattern("sp3", "");

	public void testCreateAllPropertyTypes() throws Exception {
		Injector injector = Guice.createInjector(new DCModule(new File(".")));
		AnalyzerBeansConfiguration configuration = injector.getInstance(AnalyzerBeansConfiguration.class);
		MutableReferenceDataCatalog referenceDataCatalog = (MutableReferenceDataCatalog) configuration
				.getReferenceDataCatalog();
		referenceDataCatalog.addDictionary(dict1);
		referenceDataCatalog.addDictionary(dict2);
		referenceDataCatalog.addDictionary(dict3);
		referenceDataCatalog.addStringPattern(stringPattern1);
		referenceDataCatalog.addStringPattern(stringPattern2);
		referenceDataCatalog.addStringPattern(stringPattern3);
		referenceDataCatalog.addSynonymCatalog(syn1);
		referenceDataCatalog.addSynonymCatalog(syn2);
		referenceDataCatalog.addSynonymCatalog(syn3);

		injector = injector.getInstance(InjectorBuilder.class).with(AnalyzerBeansConfiguration.class, configuration)
				.createInjector();

		AnalysisJobBuilder ajb = injector.getInstance(AnalysisJobBuilder.class);

		AnalyzerBeanDescriptor<ManyPropertiesAnalyzer> descriptor = Descriptors.ofAnalyzer(ManyPropertiesAnalyzer.class);

		assertEquals(28, descriptor.getConfiguredProperties().size());

		AbstractBeanJobBuilder<?, ?, ?> beanJobBuilder = ajb.addAnalyzer(descriptor);

		PropertyWidgetFactory propertyWidgetFactory = injector.getInstance(InjectorBuilder.class)
				.with(PropertyWidgetFactory.TYPELITERAL_BEAN_JOB_BUILDER, beanJobBuilder).createInjector()
				.getInstance(PropertyWidgetFactory.class);
		assertNotNull(propertyWidgetFactory);

		performAssertions(propertyWidgetFactory, "Int property", SingleNumberPropertyWidget.class, 0, 2);

		performAssertions(propertyWidgetFactory, "Int array property", MultipleNumberPropertyWidget.class, new int[2],
				new int[] { 2, 3 });

		performAssertions(propertyWidgetFactory, "Number property", SingleNumberPropertyWidget.class, null, 2l);

		performAssertions(propertyWidgetFactory, "Number array property", MultipleNumberPropertyWidget.class, new Number[] {
				null, null }, new Number[] { 2l, 3l });

		performAssertions(propertyWidgetFactory, "Double property", SingleNumberPropertyWidget.class, 0d, 2d);

		performAssertions(propertyWidgetFactory, "Double array property", MultipleNumberPropertyWidget.class, new double[2],
				new double[] { 2d, 3d }); // TODO

		performAssertions(propertyWidgetFactory, "Bool property", SingleBooleanPropertyWidget.class, false, true);

		performAssertions(propertyWidgetFactory, "Bool array property", DummyPropertyWidget.class, null, new boolean[] {
				true, false }); // TODO

		performAssertions(propertyWidgetFactory, "String property", SingleStringPropertyWidget.class, "", "foo");

		performAssertions(propertyWidgetFactory, "String array property", MultipleStringPropertyWidget.class, new String[] {
				"", "" }, new String[] { "foo", "bar" });

		performAssertions(propertyWidgetFactory, "Char property", SingleCharacterPropertyWidget.class, (char) 0, 'b');

		performAssertions(propertyWidgetFactory, "Char array property", MultipleCharPropertyWidget.class, new char[1],
				new char[] { 'b', 'a', 'r' });

		performAssertions(propertyWidgetFactory, "Enum property", SingleEnumPropertyWidget.class, null,
				ValidationCategory.VALID);

		performAssertions(propertyWidgetFactory, "Enum array property", MultipleEnumPropertyWidget.class,
				new ValidationCategory[0], new ValidationCategory[] { ValidationCategory.VALID, ValidationCategory.INVALID });

		File fooFile = new File("foo").getAbsoluteFile();
		performAssertions(propertyWidgetFactory, "File property", SingleFilePropertyWidget.class, null, fooFile);

		performAssertions(propertyWidgetFactory, "File array property", DummyPropertyWidget.class, null, new File[] {
				fooFile, new File("bar") });

		// TODO: Disabled because pattern.equals only works by identity!
		// performAssertions(propertyWidgetFactory, "Pattern property",
		// SinglePatternPropertyWidget.class, Pattern.compile(""),
		// Pattern.compile("foo"));
		//
		// performAssertions(propertyWidgetFactory, "Pattern array property",
		// DummyPropertyWidget.class, null, new Pattern[] {
		// Pattern.compile("foo"), Pattern.compile("bar") });

		performAssertions(propertyWidgetFactory, "Input column property", SingleInputColumnRadioButtonPropertyWidget.class,
				null, new MockInputColumn<String>("foo", String.class));

		performAssertions(propertyWidgetFactory, "Input column array property", MultipleInputColumnsPropertyWidget.class,
				new InputColumn[0], new InputColumn[] { new MockInputColumn<String>("foo", String.class),
						new MockInputColumn<String>("bar", String.class) });

		performAssertions(propertyWidgetFactory, "Dictionary property", SingleDictionaryPropertyWidget.class, null,
				new SimpleDictionary("foo", "foobar"));

		performAssertions(propertyWidgetFactory, "Dictionary array property", MultipleDictionariesPropertyWidget.class,
				new Dictionary[0], new Dictionary[] { dict1, dict3 });

		performAssertions(propertyWidgetFactory, "String pattern property", SingleStringPatternPropertyWidget.class, null,
				new SimpleStringPattern("foo", "aaa"));

		performAssertions(propertyWidgetFactory, "String pattern array property", MultipleStringPatternPropertyWidget.class,
				new StringPattern[0], new StringPattern[] { stringPattern1, stringPattern3 });

		performAssertions(propertyWidgetFactory, "Synonym catalog property", SingleSynonymCatalogPropertyWidget.class, null,
				new TextFileSynonymCatalog("foo", new File("foobar"), true, "UTF8"));

		performAssertions(propertyWidgetFactory, "Synonym catalog array property",
				MultipleSynonymCatalogsPropertyWidget.class, new SynonymCatalog[0], new SynonymCatalog[] { syn1, syn3 });

		performAssertions(propertyWidgetFactory, "Datastore property", SingleDatastorePropertyWidget.class, null,
				new ExcelDatastore("my ds", "target/foobar.xlsx"));

		performAssertions(propertyWidgetFactory, "Updateable datastore property", SingleDatastorePropertyWidget.class, null,
				new CsvDatastore("foo", "foo.csv"));
	}

	private void performAssertions(PropertyWidgetFactory propertyWidgetFactory, String propertyName,
			Class<? extends PropertyWidget<?>> widgetClass, Object initialValue, final Object setValue) {
		@SuppressWarnings("unchecked")
		PropertyWidget<Object> widget = (PropertyWidget<Object>) propertyWidgetFactory.create(propertyName);
		assertNotNull(widget);
		assertEquals(widgetClass, widget.getClass());

		assertEquals(propertyName, widget.getPropertyDescriptor().getName());

		assertTrue("Expected: " + initialValue + ", actual: " + widget.getValue(),
				EqualsBuilder.equals(initialValue, widget.getValue()));
		widget.onValueTouched(setValue);
		assertTrue(widget.isSet());
		assertTrue("Expected: " + ArrayUtils.toString(setValue) + ", actual: " + ArrayUtils.toString(widget.getValue()),
				EqualsBuilder.equals(setValue, widget.getValue()));
	}
}
