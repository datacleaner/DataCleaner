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
package org.datacleaner.descriptors;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

import javax.inject.Named;

import org.apache.metamodel.util.TruePredicate;
import org.datacleaner.api.Analyzer;
import org.datacleaner.api.Filter;
import org.datacleaner.api.Renderer;
import org.datacleaner.api.RendererBean;
import org.datacleaner.api.RenderingFormat;
import org.datacleaner.api.Transformer;
import org.datacleaner.util.ReflectionUtils;
import org.kohsuke.asm5.AnnotationVisitor;
import org.kohsuke.asm5.Attribute;
import org.kohsuke.asm5.ClassVisitor;
import org.kohsuke.asm5.FieldVisitor;
import org.kohsuke.asm5.MethodVisitor;
import org.kohsuke.asm5.Opcodes;
import org.kohsuke.asm5.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ASM class visitor for DataCleaner components.
 */
final class DCClassVisitor extends ClassVisitor {

    private static final int API_VERSION = Opcodes.ASM4;

    private static final Logger logger = LoggerFactory.getLogger(DCClassVisitor.class);
    private final ClassLoader _classLoader;
    private final Predicate<Class<? extends RenderingFormat<?>>> _renderingFormatPredicate;
    private Class<?> _beanClazz;
    private String _name;

    DCClassVisitor(final ClassLoader classLoader,
            final Predicate<Class<? extends RenderingFormat<?>>> renderingFormatPredicate) {
        super(API_VERSION);
        _classLoader = classLoader;
        _renderingFormatPredicate = renderingFormatPredicate;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        _name = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (isAnnotation(desc, RendererBean.class)) {
            if (_renderingFormatPredicate == null || _renderingFormatPredicate instanceof TruePredicate) {
                initializeClass();
                return null;
            }
            return new AnnotationVisitor(API_VERSION) {
                @Override
                public void visit(final String name, final Object value) {
                    final Type valueType = (Type) value;
                    final String renderingFormatClassName = valueType.getClassName();
                    final Class<? extends RenderingFormat<?>> renderingFormatClass;
                    try {
                        @SuppressWarnings("unchecked") final Class<? extends RenderingFormat<?>> cls =
                                (Class<? extends RenderingFormat<?>>) Class.forName(renderingFormatClassName, false,
                                        _classLoader);
                        renderingFormatClass = cls;
                    } catch (final Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Failed to read rendering format of renderer class '" + renderingFormatClassName
                                    + "', ignoring: " + _name, e);
                        }
                        return;
                    }

                    final Boolean proceed = _renderingFormatPredicate.test(renderingFormatClass);
                    if (proceed == null || !proceed.booleanValue()) {
                        logger.info("Skipping renderer because it's format was not accepted by predicate: {}", _name);
                        return;
                    }
                    initializeClass();
                }
            };
        }

        if (isAnnotation(desc, Named.class)) {
            initializeClass();
        }
        return null;
    }

    private boolean isAnnotation(final String annotationDesc, final Class<? extends Annotation> annotationClass) {
        return annotationDesc.indexOf(annotationClass.getName().replace('.', '/')) != -1;
    }

    private Class<?> initializeClass() {
        if (_beanClazz == null) {
            final String javaName = _name.replace('/', '.');
            try {
                _beanClazz = Class.forName(javaName, true, _classLoader);
            } catch (final ClassNotFoundException e) {
                // This happens when the class itself does not exist
                logger.error("Could not find class to be loaded: " + javaName, e);
            } catch (final NoClassDefFoundError e) {
                // This happens if the class depends on a unsatisfied
                // dependency. For instance when it is a renderer bean that
                // depends on a particular rendering format. We will gracefully
                // recover from this scenario with just a warning.

                logger.error("Failed to load class {} because of unsatisfied class dependency: {}", javaName,
                        e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to load class: " + javaName, e);
                }
            } catch (final UnsupportedClassVersionError e) {
                logger.error("Failed to load class {} because of unsupported class version: {}", javaName,
                        e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to load class: " + javaName, e);
                }
            }
        }
        return _beanClazz;
    }

    public boolean isAnalyzer() {
        if (_beanClazz != null) {
            return ReflectionUtils.is(_beanClazz, Analyzer.class);
        }
        return false;
    }

    public boolean isTransformer() {
        if (_beanClazz != null) {
            return ReflectionUtils.is(_beanClazz, Transformer.class);
        }
        return false;
    }

    public boolean isRenderer() {
        if (_beanClazz != null) {
            return ReflectionUtils.isAnnotationPresent(_beanClazz, RendererBean.class)
                    && ReflectionUtils.is(_beanClazz, Renderer.class);
        }
        return false;
    }

    public boolean isFilter() {
        if (_beanClazz != null) {
            return ReflectionUtils.is(_beanClazz, Filter.class);
        }
        return false;
    }

    public Class<?> getBeanClass() {
        return _beanClazz;
    }

    @Override
    public void visitAttribute(final Attribute arg0) {
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public FieldVisitor visitField(final int arg0, final String arg1, final String arg2, final String arg3,
            final Object arg4) {
        return null;
    }

    @Override
    public void visitInnerClass(final String arg0, final String arg1, final String arg2, final int arg3) {
    }

    @Override
    public MethodVisitor visitMethod(final int arg0, final String arg1, final String arg2, final String arg3,
            final String[] arg4) {
        return null;
    }

    @Override
    public void visitOuterClass(final String arg0, final String arg1, final String arg2) {
    }

    @Override
    public void visitSource(final String arg0, final String arg1) {
    }
}
