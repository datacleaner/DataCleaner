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
package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Modifier;

import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.result.renderer.Renderable;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AnnotationBasedRendererBeanDescriptor<R extends Renderer<?, ?>> extends SimpleComponentDescriptor<R>
        implements RendererBeanDescriptor<R> {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AnnotationBasedRendererBeanDescriptor.class);

    private final Class<? extends RenderingFormat<?>> _renderingFormat;
    private final Class<?> _formatOutputType;
    private final Class<? extends Renderable> _rendererInputType;
    private final Class<?> _rendererOutputType;

    protected AnnotationBasedRendererBeanDescriptor(Class<R> rendererClass) throws DescriptorException {
        super(rendererClass, true);

        RendererBean rendererBeanAnnotation = ReflectionUtils.getAnnotation(rendererClass, RendererBean.class);
        if (rendererBeanAnnotation == null) {
            throw new DescriptorException(rendererClass + " doesn't implement the RendererBean annotation");
        }

        _renderingFormat = rendererBeanAnnotation.value();
        if (_renderingFormat == null || _renderingFormat.isInterface()
                || Modifier.isAbstract(_renderingFormat.getModifiers())) {
            throw new DescriptorException("Rendering format (" + _renderingFormat + ") is not a non-abstract class");
        }

        _formatOutputType = ReflectionUtils.getTypeParameter(_renderingFormat, RenderingFormat.class, 0);
        logger.debug("Found format output type: {}", _formatOutputType);

        if (_formatOutputType == null) {
            throw new DescriptorException("Could not determine output type of rendering format: " + _renderingFormat);
        }

        @SuppressWarnings("unchecked")
        Class<? extends Renderable> rendererInputType = (Class<? extends Renderable>) ReflectionUtils.getTypeParameter(
                rendererClass, Renderer.class, 0);
        _rendererInputType = rendererInputType;

        logger.debug("Found renderer input type: {}", _rendererInputType);
        _rendererOutputType = ReflectionUtils.getTypeParameter(rendererClass, Renderer.class, 1);
        logger.debug("Found renderer output type: {}", _rendererOutputType);

        if (_rendererOutputType == null) {
            throw new DescriptorException("Could not determine output type of renderer: " + rendererClass);
        }

        if (!ReflectionUtils.is(_rendererOutputType, _formatOutputType)) {
            throw new DescriptorException("The renderer output type (" + _rendererOutputType
                    + ") is not a valid instance or sub-class of format output type (" + _formatOutputType + ")");
        }
    }

    @Override
    public Class<? extends RenderingFormat<?>> getRenderingFormat() {
        return _renderingFormat;
    }

    @Override
    public boolean isOutputApplicableFor(Class<?> requiredClass) {
        if (!ReflectionUtils.is(requiredClass, _formatOutputType)) {
            logger.debug("{} is not applicable to the format output type: {}", requiredClass, _formatOutputType);
            return false;
        }

        boolean result = ReflectionUtils.is(_rendererOutputType, requiredClass);

        if (!result) {
            logger.debug("{} is not applicable to the renderer output type: {}", requiredClass, _rendererOutputType);
        }

        return result;
    }

    @Override
    public Class<? extends Renderable> getRenderableType() {
        return _rendererInputType;
    }
}
