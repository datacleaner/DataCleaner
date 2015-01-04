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
package org.eobjects.analyzer.cli;

import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.result.AnalysisResultWriter;
import org.eobjects.analyzer.result.html.HtmlAnalysisResultWriter;
import org.eobjects.analyzer.result.renderer.HtmlRenderingFormat;
import org.eobjects.analyzer.result.renderer.TextRenderingFormat;

/**
 * Represents the different types of output types that can be selected in the
 * CLI.
 */
public enum CliOutputType {

	TEXT(TextRenderingFormat.class), HTML(HtmlRenderingFormat.class), SERIALIZED(null);

	private final Class<? extends RenderingFormat<?>> _renderingFormat;

	private CliOutputType(Class<? extends RenderingFormat<?>> renderingFormat) {
		_renderingFormat = renderingFormat;
	}

	public Class<? extends RenderingFormat<?>> getRenderingFormat() {
		return _renderingFormat;
	}
	
	public AnalysisResultWriter createWriter() {
	    if (this == TEXT ){ 
	        return new TextAnalysisResultWriter();
	    } else if (this == HTML) {
	        return new HtmlAnalysisResultWriter();
	    } else {
	        return new SerializedAnalysisResultWriter();
	    }
	}
}
