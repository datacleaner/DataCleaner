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
package org.eobjects.analyzer.descriptors;

import java.lang.annotation.Annotation;

import org.eobjects.analyzer.beans.api.Analyzer;
import org.eobjects.analyzer.beans.api.AnalyzerBean;
import org.eobjects.analyzer.beans.api.Filter;
import org.eobjects.analyzer.beans.api.FilterBean;
import org.eobjects.analyzer.beans.api.Renderer;
import org.eobjects.analyzer.beans.api.RendererBean;
import org.eobjects.analyzer.beans.api.RenderingFormat;
import org.eobjects.analyzer.beans.api.Transformer;
import org.eobjects.analyzer.beans.api.TransformerBean;
import org.eobjects.analyzer.util.ReflectionUtils;
import org.apache.metamodel.util.Predicate;
import org.apache.metamodel.util.TruePredicate;
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
 * ASM class visitor for analyzer beans components.
 */
final class BeanClassVisitor extends ClassVisitor {

    private static final int API_VERSION = Opcodes.ASM4;

    private final static Logger _logger = LoggerFactory.getLogger(BeanClassVisitor.class);
    private final ClassLoader _classLoader;
    private final Predicate<Class<? extends RenderingFormat<?>>> _renderingFormatPredicate;
    private Class<?> _beanClazz;
    private String _name;

    public BeanClassVisitor(ClassLoader classLoader,
            Predicate<Class<? extends RenderingFormat<?>>> renderingFormatPredicate) {
        super(API_VERSION);
        _classLoader = classLoader;
        _renderingFormatPredicate = renderingFormatPredicate;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        _name = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (isAnnotation(desc, RendererBean.class)) {
            if (_renderingFormatPredicate == null || _renderingFormatPredicate instanceof TruePredicate) {
                initializeClass();
                return null;
            }
            return new AnnotationVisitor(API_VERSION) {
                @Override
                public void visit(String name, Object value) {
                    final Type valueType = (Type) value;
                    final String renderingFormatClassName = valueType.getClassName();
                    final Class<? extends RenderingFormat<?>> renderingFormatClass;
                    try {
                        @SuppressWarnings("unchecked")
                        final Class<? extends RenderingFormat<?>> cls = (Class<? extends RenderingFormat<?>>) Class
                                .forName(renderingFormatClassName, false, _classLoader);
                        renderingFormatClass = cls;
                    } catch (Exception e) {
                        if (_logger.isWarnEnabled()) {
                            _logger.warn("Failed to read rendering format of renderer class '"
                                    + renderingFormatClassName + "', ignoring: " + _name, e);
                        }
                        return;
                    }

                    final Boolean proceed = _renderingFormatPredicate.eval(renderingFormatClass);
                    if (proceed == null || !proceed.booleanValue()) {
                        _logger.info("Skipping renderer because it's format was not accepted by predicate: {}", _name);
                        return;
                    }
                    initializeClass();
                }
            };
        }
        if (isAnnotation(desc, AnalyzerBean.class) || isAnnotation(desc, TransformerBean.class)
                || isAnnotation(desc, FilterBean.class)) {
            initializeClass();
        }
        return null;
    }

    private boolean isAnnotation(String annotationDesc, Class<? extends Annotation> annotationClass) {
        return annotationDesc.indexOf(annotationClass.getName().replace('.', '/')) != -1;
    }

    private Class<?> initializeClass() {
        if (_beanClazz == null) {
            String javaName = _name.replace('/', '.');
            try {
                _beanClazz = Class.forName(javaName, true, _classLoader);
            } catch (ClassNotFoundException e) {
                // This happens when the class itself does not exist
                _logger.error("Could not find class to be loaded: " + javaName, e);
            } catch (NoClassDefFoundError e) {
                // This happens if the class depends on a unsatisfied
                // dependency. For instance when it is a renderer bean that
                // depends on a particular rendering format. We will gracefully
                // recover from this scenario with just a warning.

                _logger.error("Failed to load class {} because of unsatisfied class dependency: {}", javaName,
                        e.getMessage());
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Failed to load class: " + javaName, e);
                }
            }
        }
        return _beanClazz;
    }

    public boolean isAnalyzer() {
        if (_beanClazz != null) {
            return ReflectionUtils.isAnnotationPresent(_beanClazz, AnalyzerBean.class)
                    && ReflectionUtils.is(_beanClazz, Analyzer.class);
        }
        return false;
    }

    public boolean isTransformer() {
        if (_beanClazz != null) {
            return ReflectionUtils.isAnnotationPresent(_beanClazz, TransformerBean.class)
                    && ReflectionUtils.is(_beanClazz, Transformer.class);
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
            return ReflectionUtils.isAnnotationPresent(_beanClazz, FilterBean.class)
                    && ReflectionUtils.is(_beanClazz, Filter.class);
        }
        return false;
    }

    public Class<?> getBeanClass() {
        return _beanClazz;
    }

    @Override
    public void visitAttribute(Attribute arg0) {
    }

    @Override
    public void visitEnd() {
    }

    @Override
    public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
        return null;
    }

    @Override
    public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
    }

    @Override
    public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4) {
        return null;
    }

    @Override
    public void visitOuterClass(String arg0, String arg1, String arg2) {
    }

    @Override
    public void visitSource(String arg0, String arg1) {
    }
}