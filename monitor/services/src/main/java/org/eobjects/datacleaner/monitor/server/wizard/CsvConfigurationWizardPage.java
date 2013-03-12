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
package org.eobjects.datacleaner.monitor.server.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.common.AbstractFreemarkerWizardPage;
import org.eobjects.datacleaner.util.CsvConfigurationDetection;
import org.eobjects.metamodel.csv.CsvConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WizardPage for configuring separator char, quote char, escape char, header
 * line number and file encoding of a CSV file.
 */
public abstract class CsvConfigurationWizardPage extends AbstractFreemarkerWizardPage {

    private static final Logger logger = LoggerFactory.getLogger(CsvConfigurationWizardPage.class);

    private final File _file;

    public CsvConfigurationWizardPage(File file) {
        _file = file;
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
        final CsvConfiguration detectedConfiguration = autoDetectConfiguration(_file);
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("separator", detectedConfiguration.getSeparatorChar());
        map.put("quote", detectedConfiguration.getQuoteChar());
        map.put("escape", detectedConfiguration.getEscapeChar());
        map.put("headerLineNumber", detectedConfiguration.getColumnNameLineNumber());
        map.put("encoding", detectedConfiguration.getEncoding());
        return map;
    };

    private CsvConfiguration autoDetectConfiguration(File file) {
        try {
            final CsvConfigurationDetection detection = new CsvConfigurationDetection(file);
            return detection.suggestCsvConfiguration();
        } catch (Exception e) {
            logger.warn("Failed to detect CSV configuration for file: " + file, e);
            return new CsvConfiguration();
        }
    }

    @Override
    public WizardPageController nextPageController(Map<String, List<String>> formParameters)
            throws DCUserInputException {
        final char separator = getChar(formParameters, "separator");
        final char quote = formParameters.get("quote").get(0).charAt(0);
        final char escape = formParameters.get("escape").get(0).charAt(0);

        final int headerLineNumber;
        final String headerLineNumberStr = formParameters.get("headerLineNumber").get(0);
        try {
            headerLineNumber = Integer.parseInt(headerLineNumberStr);
        } catch (NumberFormatException e) {
            throw new DCUserInputException("Not a valid header line number: " + headerLineNumberStr);
        }

        final String encoding = formParameters.get("encoding").get(0);

        final CsvConfiguration configuration = new CsvConfiguration(headerLineNumber, encoding, separator, quote,
                escape, true);

        return nextPageController(configuration);
    }

    private char getChar(Map<String, List<String>> formParameters, String charType) {
        try {
            String value = formParameters.get(charType).get(0);
            if (value.length() == 0) {
                return CsvConfiguration.NOT_A_CHAR;
            }
            return value.charAt(0);
        } catch (Exception e) {
            throw new DCUserInputException("Please fill a " + charType + " character");
        }
    }

    protected abstract WizardPageController nextPageController(CsvConfiguration configuration);
}
