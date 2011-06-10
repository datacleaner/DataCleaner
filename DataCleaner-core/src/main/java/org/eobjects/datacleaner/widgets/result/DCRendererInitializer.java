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

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.result.renderer.RendererInitializer;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.eobjects.datacleaner.bootstrap.WindowManager;

public class DCRendererInitializer implements RendererInitializer {

	private final WindowManager _windowManager;

	public DCRendererInitializer(WindowManager windowManager) {
		_windowManager = windowManager;
	}

	@Override
	public void initialize(Renderer<?, ?> renderer) {
		Field[] injectFields = ReflectionUtils.getFields(renderer.getClass(), Inject.class);
		for (Field field : injectFields) {
			if (field.getType() == WindowManager.class) {
				try {
					field.setAccessible(true);
					field.set(renderer, _windowManager);
				} catch (Exception e) {
					throw new IllegalStateException("Could not assign " + WindowManager.class.getSimpleName() + " to "
							+ field, e);
				}
			}
		}
	}

}
