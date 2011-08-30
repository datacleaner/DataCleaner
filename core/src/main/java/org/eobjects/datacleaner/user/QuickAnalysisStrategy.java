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
package org.eobjects.datacleaner.user;

import java.io.Serializable;

import org.eobjects.datacleaner.actions.QuickAnalysisActionListener;

/**
 * Defines the strategy and rules for doing quick analysis.
 * 
 * @see QuickAnalysisActionListener
 * 
 * @author Kasper Sørensen
 */
public class QuickAnalysisStrategy implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int columnsPerAnalyzer;
	private final boolean includeValueDistribution;
	private final boolean includePatternFinder;

	public QuickAnalysisStrategy() {
		this(5, false, false);
	}

	public QuickAnalysisStrategy(int columnsPerAnalyzer, boolean includeValueDistribution, boolean includePatternFinder) {
		this.columnsPerAnalyzer = columnsPerAnalyzer;
		this.includeValueDistribution = includeValueDistribution;
		this.includePatternFinder = includePatternFinder;
	}

	public boolean isIncludePatternFinder() {
		return includePatternFinder;
	}

	public boolean isIncludeValueDistribution() {
		return includeValueDistribution;
	}

	public int getColumnsPerAnalyzer() {
		return columnsPerAnalyzer;
	}
}
