/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.pentaho;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eobjects.analyzer.beans.api.Description;
import org.eobjects.analyzer.beans.api.ParameterizableMetric;
import org.eobjects.analyzer.beans.convert.ConvertToNumberTransformer;
import org.eobjects.analyzer.result.AnalyzerResult;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.CrosstabNavigator;
import org.eobjects.analyzer.result.CrosstabResult;
import org.eobjects.analyzer.result.Metric;
import org.eobjects.metamodel.util.CollectionUtils;
import org.eobjects.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Result of running a Pentaho job
 */
@Description("Pentaho job result")
public class PentahoJobResult extends CrosstabResult implements AnalyzerResult {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(PentahoJobResult.class);

    private final String _documentString;
    private transient Document _document;

    public PentahoJobResult(String documentString) {
        super(null);
        _documentString = documentString;
    }

    @Override
    public String toString(int maxEntries) {
        return toString();
    }

    @Override
    public String toString() {
        return "PentahoJobResult";
    }

    @Metric("Lines input")
    public ParameterizableMetric getLinesInput() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                Element stepElement = getStepStatusElement(parameter);
                return getMeasure(stepElement, "linesInput");
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getStepNames();
            }
        };
    }

    @Metric("Lines output")
    public ParameterizableMetric getLinesOutput() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                Element stepElement = getStepStatusElement(parameter);
                return getMeasure(stepElement, "linesOutput");
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getStepNames();
            }
        };
    }

    @Metric("Lines written")
    public ParameterizableMetric getLinesWritten() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                Element stepElement = getStepStatusElement(parameter);
                return getMeasure(stepElement, "linesWritten");
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getStepNames();
            }
        };
    }

    @Metric("Lines updated")
    public ParameterizableMetric getLinesUpdated() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                Element stepElement = getStepStatusElement(parameter);
                return getMeasure(stepElement, "linesUpdated");
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getStepNames();
            }
        };
    }

    @Metric("Lines rejected")
    public ParameterizableMetric getLinesRejected() {
        return new ParameterizableMetric() {
            @Override
            public Number getValue(String parameter) {
                Element stepElement = getStepStatusElement(parameter);
                return getMeasure(stepElement, "linesRejected");
            }

            @Override
            public Collection<String> getParameterSuggestions() {
                return getStepNames();
            }
        };
    }

    protected Number getMeasure(Element stepElement, String measureKey) {
        if (stepElement == null) {
            return null;
        }
        final String measure = DomUtils.getChildElementValueByTagName(stepElement, measureKey);
        return ConvertToNumberTransformer.transformValue(measure);
    }

    protected Collection<String> getStepNames() {
        List<Element> elements = getStepStatusElements();
        return CollectionUtils.map(elements, new Func<Element, String>() {
            @Override
            public String eval(Element element) {
                final String stepName = DomUtils.getChildElementValueByTagName(element, "stepname");
                return stepName;
            }
        });
    }

    private List<Element> getStepStatusElements() {
        final Document document = getDocument();

        final Element transstatusElement = document.getDocumentElement();
        final Element stepstatuslistElement = DomUtils.getChildElementByTagName(transstatusElement, "stepstatuslist");
        final List<Element> stepstatusElements = DomUtils.getChildElements(stepstatuslistElement);
        return stepstatusElements;
    }

    private Element getStepStatusElement(String name) {
        if (name == null) {
            return null;
        }
        List<Element> elements = getStepStatusElements();
        for (Element element : elements) {
            final String stepName = DomUtils.getChildElementValueByTagName(element, "stepname");
            if (name.equals(stepName)) {
                return element;
            }
        }
        return null;
    }

    @Override
    public Crosstab<?> getCrosstab() {
        // create the crosstab dynamically based on the document, it's more
        // flexible
        final Crosstab<Serializable> crosstab = new Crosstab<Serializable>(Serializable.class, "Step", "Measure");

        for (Element stepstatusElement : getStepStatusElements()) {
            final String stepName = DomUtils.getChildElementValueByTagName(stepstatusElement, "stepname");

            final CrosstabNavigator<Serializable> nav = crosstab.where("Step", stepName);
            addCrosstabMeasure(nav, stepstatusElement, "copy", "Copy");
            addCrosstabMeasure(nav, stepstatusElement, "linesWritten", "Lines written");
            addCrosstabMeasure(nav, stepstatusElement, "linesInput", "Lines input");
            addCrosstabMeasure(nav, stepstatusElement, "linesOutput", "Lines output");
            addCrosstabMeasure(nav, stepstatusElement, "linesUpdated", "Lines updated");
            addCrosstabMeasure(nav, stepstatusElement, "linesRejected", "Lines rejected");
            addCrosstabMeasure(nav, stepstatusElement, "errors", "Errors");
            addCrosstabMeasure(nav, stepstatusElement, "statusDescription", "Status description");
            addCrosstabMeasure(nav, stepstatusElement, "seconds", "Seconds");
            addCrosstabMeasure(nav, stepstatusElement, "speed", "Speed");
            addCrosstabMeasure(nav, stepstatusElement, "priority", "Priority");
            addCrosstabMeasure(nav, stepstatusElement, "stopped", "Stopped");
            addCrosstabMeasure(nav, stepstatusElement, "paused", "Paused");
        }

        return crosstab;
    }

    private void addCrosstabMeasure(CrosstabNavigator<Serializable> nav, Element stepstatusElement, String measureKey,
            String measureName) {
        final String measure = DomUtils.getChildElementValueByTagName(stepstatusElement, measureKey);
        nav.where("Measure", measureName).put(measure, true);
    }

    private Document getDocument() {
        if (_document == null) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                _document = documentBuilder.parse(new InputSource(new StringReader(_documentString)));
            } catch (Exception e) {
                logger.error("Failed to parse document XML: {}", _documentString);
                throw new IllegalStateException(e);
            }
        }
        return _document;
    }

    public String getDocumentString() {
        return _documentString;
    }

}
