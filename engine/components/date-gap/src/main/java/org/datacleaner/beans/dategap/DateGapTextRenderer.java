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
package org.datacleaner.beans.dategap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.result.renderer.AbstractRenderer;
import org.datacleaner.result.renderer.TextRenderingFormat;
import org.datacleaner.util.StringUtils;

@RendererBean(TextRenderingFormat.class)
public class DateGapTextRenderer extends AbstractRenderer<DateGapAnalyzerResult, String> {

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String render(DateGapAnalyzerResult result) {
		Set<String> names = result.getGroupNames();
		if (names.isEmpty()) {
			return "No timelines recorded";
		}

		StringBuilder sb = new StringBuilder();
		for (String name : names) {
			if (!StringUtils.isNullOrEmpty(name)) {
				sb.append("Timeline recorded for '");
				sb.append(name);
				sb.append('\'');
				sb.append('\n');
			}

			SortedSet<TimeInterval> gaps = result.getGaps(name);
			if (gaps.isEmpty()) {
				sb.append(" - no time gaps!\n");
			} else {
				for (TimeInterval timeInterval : gaps) {
					sb.append(" - time gap: ");
					sb.append(format(timeInterval));
					sb.append('\n');
				}
			}

			SortedSet<TimeInterval> overlaps = result.getOverlaps(name);
			if (overlaps.isEmpty()) {
				sb.append(" - no time overlaps!\n");
			} else {
				for (TimeInterval timeInterval : overlaps) {
					sb.append(" - time overlap: ");
					sb.append(format(timeInterval));
					sb.append('\n');
				}
			}
		}
		return sb.toString();
	}

	private String format(TimeInterval interval) {
		Date from = new Date(interval.getFrom());
		Date to = new Date(interval.getTo());
		return df.format(from) + " to " + df.format(to);
	}
}
