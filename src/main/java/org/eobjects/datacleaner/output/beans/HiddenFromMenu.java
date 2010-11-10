package org.eobjects.datacleaner.output.beans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used to signal to DataCleaner that a specific AnalyzerBean
 * is to be hidden from the typical menu (ie. not appear in the "Add analyzer"
 * drop down).
 * 
 * @author Kasper Sørensen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface HiddenFromMenu {

}
