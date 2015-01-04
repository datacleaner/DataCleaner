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
package org.eobjects.analyzer.beans.stringpattern;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.Crosstab;
import org.eobjects.analyzer.result.renderer.AbstractRenderer;
import org.eobjects.analyzer.result.renderer.CrosstabTextRenderer;
import org.eobjects.analyzer.result.renderer.TextRenderingFormat;

/**
 * Text renderer for {@link PatternFinderResult}s
 * 
 * 
 */
@RendererBean(TextRenderingFormat.class)
public class PatternFinderResultTextRenderer extends AbstractRenderer<PatternFinderResult, String> {

	@Override
	public String render(PatternFinderResult result) {
		final CrosstabTextRenderer crosstabTextRenderer = new CrosstabTextRenderer();
		if (result.isGroupingEnabled()) {
			Map<String, Crosstab<?>> crosstabs = result.getGroupedCrosstabs();
			if (crosstabs.isEmpty()) {
				return "No patterns found";
			}
			Set<Entry<String, Crosstab<?>>> crosstabEntries = crosstabs.entrySet();
			StringBuilder sb = new StringBuilder();
			for (Entry<String, Crosstab<?>> entry : crosstabEntries) {
				String group = entry.getKey();
				Crosstab<?> crosstab = entry.getValue();
				if (sb.length() != 0) {
					sb.append("\n");
				}

				sb.append("Patterns for group: ");
				sb.append(group);
				sb.append('\n');
				sb.append(crosstabTextRenderer.render(crosstab));
			}
			return sb.toString();
		} else {
			Crosstab<?> crosstab = result.getSingleCrosstab();
			return crosstabTextRenderer.render(crosstab);
		}
	}

}
