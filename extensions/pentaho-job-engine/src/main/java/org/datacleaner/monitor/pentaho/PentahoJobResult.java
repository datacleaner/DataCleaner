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
package org.datacleaner.monitor.pentaho;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.datacleaner.api.AnalyzerResult;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.Metric;
import org.datacleaner.components.convert.ConvertToNumberTransformer;
import org.datacleaner.result.Crosstab;
import org.datacleaner.result.CrosstabNavigator;
import org.datacleaner.result.CrosstabResult;
import org.apache.metamodel.util.CollectionUtils;
import org.apache.metamodel.util.Func;
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

    @Metric(order = 101, value = "Lines input")
    public Number getLinesInput(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "linesInput");
    }

    @Metric(order = 102, value = "Lines output")
    public Number getLinesOutput(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "linesOutput");
    }

    @Metric(order = 103, value = "Lines written")
    public Number getLinesWritten(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "linesWritten");
    }

    @Metric(order = 104, value = "Lines updated")
    public Number getLinesUpdated(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "linesUpdated");
    }

    @Metric(order = 105, value = "Lines rejected")
    public Number getLinesRejected(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "linesRejected");
    }

    @Metric(order = 106, value = "Seconds")
    public Number getSeconds(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "seconds");
    }

    @Metric(order = 107, value = "Speed")
    public Number getSpeed(InputColumn<?> step) {
        Element stepElement = getStepStatusElement(step.getName());
        return getMeasure(stepElement, "speed");
    }
    
    @Metric(order = 201, value = "First log line no.")
    public Number getFirstLogLine() {
        Element transStatusElement = getTransStatusElement();
        return getMeasure(transStatusElement, "first_log_line_nr");
    }
    
    @Metric(order = 202, value = "Last log line no.")
    public Number getLastLogLine() {
        Element transStatusElement = getTransStatusElement();
        return getMeasure(transStatusElement, "last_log_line_nr");
    }
    
    @Metric(order = 203, value = "Error count")
    public Number getErrorCount() {
        Element resultStatusElement = getResultStatusElement();
        return getMeasure(resultStatusElement, "nr_errors");
    }
    
    @Metric(order = 204, value = "Files retrieved")
    public Number getFilesRetrieved() {
        Element resultStatusElement = getResultStatusElement();
        return getMeasure(resultStatusElement, "nr_files_retrieved");
    }

    private Element getResultStatusElement() {
        final Element transstatusElement = getTransStatusElement();
        final Element resultStatusElement = DomUtils.getChildElementByTagName(transstatusElement, "result");
        return resultStatusElement;
    }

    protected Number getMeasure(Element parentElement, String measureKey) {
        if (parentElement == null) {
            return null;
        }
        final String measure = DomUtils.getChildElementValueByTagName(parentElement, measureKey);
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
        final Element transstatusElement = getTransStatusElement();
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

    public Element getTransStatusElement() {
        final Document document = getDocument();
        final Element transstatusElement = document.getDocumentElement();
        return transstatusElement;
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
