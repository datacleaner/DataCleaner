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
package org.eobjects.datacleaner.widgets.result;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.result.ValidationResult;
import org.eobjects.analyzer.result.renderer.Renderer;
import org.eobjects.analyzer.result.renderer.SwingRenderingFormat;

@RendererBean(SwingRenderingFormat.class)
public class ValidationResultSwingRenderer implements Renderer<ValidationResult, JPanel> {

	@Override
	public JPanel render(ValidationResult result) {
		JPanel panel = new AnnotatedRowsResultSwingRenderer().render(result);
		panel.add(new JLabel("Invalid rows:"), 0);
		return panel;
	}
}
