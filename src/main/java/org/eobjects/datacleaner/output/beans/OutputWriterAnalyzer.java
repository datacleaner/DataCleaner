package org.eobjects.datacleaner.output.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used to signal to DataCleaner that a specific AnalyzerBean
 * is used to write output and not to present analysis results. Such analyzers
 * will be hidden from the main "add analyzer" menu but shown in the filter
 * outcome mapping menu.
 * 
 * @author Kasper Sørensen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface OutputWriterAnalyzer {

}
