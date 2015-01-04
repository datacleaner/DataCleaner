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
package org.datacleaner.widgets.result;

import org.datacleaner.beans.api.RendererBean;
import org.datacleaner.result.CrosstabResult;
import org.datacleaner.result.renderer.RendererFactory;
import org.datacleaner.result.renderer.SwingRenderingFormat;
import org.datacleaner.bootstrap.WindowContext;

@RendererBean(SwingRenderingFormat.class)
public class DefaultCrosstabResultSwingRenderer extends AbstractCrosstabResultSwingRenderer<CrosstabResult> {

	/**
	 * Constructor used for programmatic composition
	 * 
	 * @param windowContext
	 * @param rendererFactory 
	 */
	public DefaultCrosstabResultSwingRenderer(WindowContext windowContext, RendererFactory rendererFactory) {
		super(windowContext, rendererFactory);
	}

	/**
	 * Default constructor, used by {@link RendererFactory}.
	 */
	public DefaultCrosstabResultSwingRenderer() {
		super();
	}

}
