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
package org.datacleaner.monitor.server.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.Resource;
import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.wizard.WizardPageController;
import org.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.datacleaner.util.CsvConfigurationDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WizardPage for configuring separator char, quote char, escape char, header
 * line number and file encoding of a CSV file.
 */
public abstract class CsvConfigurationWizardPage extends AbstractFreemarkerWizardPage {

    private static final Logger logger = LoggerFactory.getLogger(CsvConfigurationWizardPage.class);

    private final Resource _resource;

    public CsvConfigurationWizardPage(final Resource resource) {
        _resource = resource;
    }

    @Override
    public Integer getPageIndex() {
        return 2;
    }

    @Override
    protected String getTemplateFilename() {
        return "CsvConfigurationWizardPage.html";
    }

    protected Map<String, Object> getFormModel() {
        final CsvConfiguration detectedConfiguration = autoDetectConfiguration(_resource);
        final Map<String, Object> map = new HashMap<>();
        map.put("separator", detectedConfiguration.getSeparatorChar());
        map.put("quote", detectedConfiguration.getQuoteChar());
        map.put("escape", detectedConfiguration.getEscapeChar());
        map.put("headerLineNumber", detectedConfiguration.getColumnNameLineNumber());
        map.put("encoding", detectedConfiguration.getEncoding());
        map.put("multilinesValues", detectedConfiguration.isMultilineValues());
        return map;
    }

    private CsvConfiguration autoDetectConfiguration(final Resource resource) {
        try {
            final CsvConfigurationDetection detection = new CsvConfigurationDetection(resource);
            return detection.suggestCsvConfiguration();
        } catch (final Exception e) {
            logger.warn("Failed to detect CSV configuration for file: " + resource, e);
            return new CsvConfiguration();
        }
    }

    @Override
    public WizardPageController nextPageController(final Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final char separator = getChar(formParameters, "separator");
        final char quote = getChar(formParameters, "quote");
        final char escape = getChar(formParameters, "escape");

        final int headerLineNumber;

        final String headerLineNumberStr = getString(formParameters, "headerLineNumber");
        try {
            headerLineNumber = Integer.parseInt(headerLineNumberStr);
        } catch (final NumberFormatException e) {
            throw new DCUserInputException("Not a valid header line number: " + headerLineNumberStr);
        }

        final String encoding = getString(formParameters, "encoding");
        final boolean multilines = getBoolean(formParameters, "multilinesValues");
        final CsvConfiguration configuration =
                new CsvConfiguration(headerLineNumber, encoding, separator, quote, escape, true, multilines);

        return nextPageController(configuration);
    }

    private char getChar(final Map<String, List<String>> formParameters, final String charType) {
        try {
            final String value = getString(formParameters, charType);
            if (value == null || value.length() == 0) {
                return CsvConfiguration.NOT_A_CHAR;
            }
            return value.charAt(0);
        } catch (final Exception e) {
            throw new DCUserInputException("Please fill a " + charType + " character");
        }
    }

    protected abstract WizardPageController nextPageController(CsvConfiguration configuration);
}
