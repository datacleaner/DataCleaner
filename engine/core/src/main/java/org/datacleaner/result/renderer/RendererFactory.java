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
package org.datacleaner.result.renderer;

import java.util.Collection;

import org.datacleaner.api.Renderable;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererPrecedence;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.configuration.AnalyzerBeansConfiguration;
import org.datacleaner.descriptors.DescriptorProvider;
import org.datacleaner.descriptors.RendererBeanDescriptor;
import org.datacleaner.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory that can resolve the best suited {@link Renderer} for a particular
 * {@link Renderable}. The resolving mechanism inspects the available renderers
 * in the {@link DescriptorProvider}.
 * 
 * 
 */
public final class RendererFactory {

    private static final Logger logger = LoggerFactory.getLogger(RendererFactory.class);

    /**
     * Represents a selection of a renderer. Will be used to store everything
     * related to a renderer while finding the most suited renderer.
     */
    private static class RendererSelection {
        private final Renderer<?, ?> renderer;
        private final RendererPrecedence precedence;
        private final int hierarchyDistance;

        public RendererSelection(Renderer<?, ?> renderer, RendererPrecedence precedence, int hierarchyDistance) {
            this.renderer = renderer;
            this.precedence = precedence;
            this.hierarchyDistance = hierarchyDistance;
        }

        public Renderer<?, ?> getRenderer() {
            return renderer;
        }

        public RendererPrecedence getPrecedence() {
            return precedence;
        }

        public int getHierarchyDistance() {
            return hierarchyDistance;
        }
    }

    private final DescriptorProvider _descriptorProvider;
    private final RendererInitializer _rendererInitializer;

    /**
     * Constructs a renderer factory
     * 
     * @param configuration
     * @param job
     *            optionally a job which the instantiated renderers pertain to.
     */
    public RendererFactory(AnalyzerBeansConfiguration configuration) {
        this(configuration, new DefaultRendererInitializer(configuration));
    }
    
    public RendererFactory(AnalyzerBeansConfiguration configuration, RendererInitializer rendererInitializer) {
        this(configuration.getDescriptorProvider(), rendererInitializer);
    }

    /**
     * Constructs a renderer factory
     * 
     * @param descriptorProvider
     * @param rendererInitializer
     * 
     * @deprecated use
     *             {@link #RendererFactory(AnalyzerBeansConfiguration)}
     *             instead.
     */
    @Deprecated
    public RendererFactory(DescriptorProvider descriptorProvider, RendererInitializer rendererInitializer) {
        _descriptorProvider = descriptorProvider;
        _rendererInitializer = rendererInitializer;
    }

    /**
     * Gets the best suited {@link Renderer} for the given {@link Renderable} to
     * the given {@link RenderingFormat}.
     * 
     * @param <I>
     * @param <O>
     * @param renderable
     * @param renderingFormat
     * @return
     */
    public <I extends Renderable, O> Renderer<? super I, ? extends O> getRenderer(I renderable,
            Class<? extends RenderingFormat<? extends O>> renderingFormat) {

        RendererSelection bestMatch = null;

        Collection<RendererBeanDescriptor<?>> descriptors = _descriptorProvider
                .getRendererBeanDescriptorsForRenderingFormat(renderingFormat);
        for (RendererBeanDescriptor<?> descriptor : descriptors) {
            RendererSelection rendererMatch = isRendererMatch(descriptor, renderable, bestMatch);
            if (rendererMatch != null) {
                bestMatch = rendererMatch;
            }
        }

        if (bestMatch == null) {
            logger.warn("Didn't find any matches for renderable {} (format={})", renderable, renderingFormat);
            return null;
        }

        @SuppressWarnings("unchecked")
        Renderer<? super I, ? extends O> renderer = (Renderer<? super I, ? extends O>) bestMatch.getRenderer();

        if (logger.isInfoEnabled()) {
            logger.info("Returning renderer '{}' for renderable '{}' in format '{}'", new Object[] { renderer,
                    renderable.getClass().getName(), renderingFormat.getName() });
        }

        return renderer;
    }

    /**
     * Checks if a particular renderer (descriptor) is a good match for a
     * particular renderer.
     * 
     * @param rendererDescriptor
     *            the renderer (descriptor) to check.
     * @param renderable
     *            the renderable that needs rendering.
     * @param bestMatchingDescriptor
     *            the currently "best matching" renderer (descriptor), or null
     *            if no other renderers matches yet.
     * @return a {@link RendererSelection} object if the renderer is a match, or
     *         null if not.
     */
    private RendererSelection isRendererMatch(RendererBeanDescriptor<?> rendererDescriptor, Renderable renderable,
            RendererSelection bestMatch) {
        final Class<? extends Renderable> renderableType = rendererDescriptor.getRenderableType();
        if (ReflectionUtils.is(renderable.getClass(), renderableType)) {
            if (bestMatch == null) {
                return isRendererCapable(rendererDescriptor, renderable, bestMatch);
            } else {
                int hierarchyDistance = ReflectionUtils.getHierarchyDistance(renderable.getClass(), renderableType);

                if (hierarchyDistance == 0) {
                    // no hierarchy distance
                    return isRendererCapable(rendererDescriptor, renderable, bestMatch);
                }

                if (hierarchyDistance <= bestMatch.getHierarchyDistance()) {
                    // lower hierarchy distance than best match
                    return isRendererCapable(rendererDescriptor, renderable, bestMatch);
                }
            }
        }
        return null;
    }

    private RendererSelection isRendererCapable(RendererBeanDescriptor<?> rendererDescriptor, Renderable renderable,
            RendererSelection bestMatch) {
        final Renderer<Renderable, ?> renderer = instantiate(rendererDescriptor);

        if (_rendererInitializer != null) {
            _rendererInitializer.initialize(rendererDescriptor, renderer);
        }

        RendererPrecedence precedence;
        try {
            precedence = renderer.getPrecedence(renderable);
            if (precedence == null) {
                logger.debug("Renderer precedence was null for {}, using MEDIUM", renderer);
                precedence = RendererPrecedence.MEDIUM;
            }

            if (precedence == RendererPrecedence.NOT_CAPABLE) {
                logger.debug("Renderer is not capable of rendering this renderable!");
                return null;
            }

            if (bestMatch != null) {
                final RendererPrecedence bestPrecedence = bestMatch.getPrecedence();
                if (precedence.ordinal() < bestPrecedence.ordinal()) {
                    logger.info("Precedence {} did not match or supersede best matching precedence ({}).", precedence, bestPrecedence);
                    return null;
                }
            }

        } catch (Exception e) {
            logger.error("Could not get precedence of renderer, returning null", e);
            return null;
        }

        final Class<? extends Renderable> renderableType = rendererDescriptor.getRenderableType();
        final int hierarchyDistance = ReflectionUtils.getHierarchyDistance(renderable.getClass(), renderableType);

        return new RendererSelection(renderer, precedence, hierarchyDistance);
    }

    @SuppressWarnings("unchecked")
    private static <I extends Renderable, O> Renderer<I, O> instantiate(RendererBeanDescriptor<?> descriptor) {
        final Class<? extends Renderer<?, ?>> componentClass = descriptor.getComponentClass();
        final Renderer<?, ?> renderer = ReflectionUtils.newInstance(componentClass);
        return (Renderer<I, O>) renderer;
    }
}
