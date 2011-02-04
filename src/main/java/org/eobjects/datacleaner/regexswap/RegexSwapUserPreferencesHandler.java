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
package org.eobjects.datacleaner.regexswap;

import java.util.Collection;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.datacleaner.user.MutableReferenceDataCatalog;
import org.eobjects.datacleaner.user.UsageLogger;
import org.eobjects.datacleaner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexSwapUserPreferencesHandler {

	private static final Logger logger = LoggerFactory.getLogger(RegexSwapUserPreferencesHandler.class);

	private final RegexSwapClient _client;
	private final MutableReferenceDataCatalog _referenceDataCatalog;

	public RegexSwapUserPreferencesHandler(MutableReferenceDataCatalog referenceDataCatalog) {
		this(referenceDataCatalog, HttpUtils.getHttpClient());
	}

	public RegexSwapUserPreferencesHandler(MutableReferenceDataCatalog referenceDataCatalog, HttpClient httpClient) {
		_referenceDataCatalog = referenceDataCatalog;
		_client = new RegexSwapClient(httpClient);
	}

	public boolean isLoaded() {
		String[] stringPatternNames = _referenceDataCatalog.getStringPatternNames();
		for (String name : stringPatternNames) {
			StringPattern stringPattern = _referenceDataCatalog.getStringPattern(name);
			if (stringPattern instanceof RegexSwapStringPattern) {
				return true;
			}
		}
		return false;
	}

	public void loadInitialRegexes() {
		logger.info("Loading initial regexes from RegexSwap");
		UsageLogger.getInstance().log("RegexSwap: Initial download");
		Collection<Category> categories = _client.getCategories();
		for (Category category : categories) {
			List<Regex> regexes = _client.getRegexes(category);
			if (regexes != null && !regexes.isEmpty()) {
				logger.info("Loading {} regexes from RegexSwap category '{}'", regexes.size(), category.getName());
				for (Regex regex : regexes) {
					if (!_referenceDataCatalog.containsStringPattern(regex.getName())) {
						logger.debug("Adding regex: {}", regex);
						_referenceDataCatalog.addStringPattern(new RegexSwapStringPattern(regex));
					} else {
						logger.debug("Omitting regex: {}", regex);
					}
				}
			}
		}
	}
}
