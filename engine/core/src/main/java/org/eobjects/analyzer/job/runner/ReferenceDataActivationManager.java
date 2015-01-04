/**
 * AnalyzerBeans
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
package org.eobjects.analyzer.job.runner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.reference.Dictionary;
import org.eobjects.analyzer.reference.StringPattern;
import org.eobjects.analyzer.reference.SynonymCatalog;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that manages the reference data that is being used within the execution
 * of a job. It will make sure that any @Initialize or @Close methods are called
 * before and after the job executes.
 * 
 * 
 */
public final class ReferenceDataActivationManager {

	private static final Logger logger = LoggerFactory.getLogger(ReferenceDataActivationManager.class);

	private final Map<String, Dictionary> _dictionaries = new HashMap<String, Dictionary>();
	private final Map<String, SynonymCatalog> _synonymCatalogs = new HashMap<String, SynonymCatalog>();
	private final Map<String, StringPattern> _stringPatterns = new HashMap<String, StringPattern>();

	public Collection<Dictionary> getDictionaries() {
		return _dictionaries.values();
	}

	public Collection<SynonymCatalog> getSynonymCatalogs() {
		return _synonymCatalogs.values();
	}

	public Collection<StringPattern> getStringPatterns() {
		return _stringPatterns.values();
	}

	public Collection<Object> getAllReferenceData() {
		Collection<Object> result = new ArrayList<Object>();
		result.addAll(getDictionaries());
		result.addAll(getSynonymCatalogs());
		result.addAll(getStringPatterns());
		return result;
	}

	public void clearReferenceData() {
		_dictionaries.clear();
		_synonymCatalogs.clear();
		_stringPatterns.clear();
	}

	public boolean accepts(Object obj) {
		if (obj == null) {
			return false;
		}
		Class<?> c = obj.getClass();
		return ReflectionUtils.is(c, Dictionary.class) || ReflectionUtils.is(c, SynonymCatalog.class)
				|| ReflectionUtils.is(c, StringPattern.class);
	}

	public void register(Object obj) {
		Class<?> c = obj.getClass();

		if (ReflectionUtils.is(c, Dictionary.class)) {
			Dictionary dictionaries[];
			if (c.isArray()) {
				dictionaries = (Dictionary[]) obj;
			} else {
				dictionaries = new Dictionary[] { (Dictionary) obj };
			}
			for (Dictionary dict : dictionaries) {
				_dictionaries.put(dict.getName(), dict);
			}
		} else if (ReflectionUtils.is(c, SynonymCatalog.class)) {
			SynonymCatalog synonymCatalogs[];
			if (c.isArray()) {
				synonymCatalogs = (SynonymCatalog[]) obj;
			} else {
				synonymCatalogs = new SynonymCatalog[] { (SynonymCatalog) obj };
			}
			for (SynonymCatalog sc : synonymCatalogs) {
				_synonymCatalogs.put(sc.getName(), sc);
			}
		} else if (ReflectionUtils.is(c, StringPattern.class)) {
			StringPattern stringPatterns[];
			if (c.isArray()) {
				stringPatterns = (StringPattern[]) obj;
			} else {
				stringPatterns = new StringPattern[] { (StringPattern) obj };
			}
			for (StringPattern sp : stringPatterns) {
				_stringPatterns.put(sp.getName(), sp);
			}
		} else {
			logger.warn("Could not register unsupport object: {}", obj);
		}
	}
}
