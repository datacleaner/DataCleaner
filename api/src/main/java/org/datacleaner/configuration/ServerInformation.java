package org.datacleaner.configuration;

import java.io.Serializable;

import org.apache.metamodel.util.HasName;

/**
 * Defines a server which could be used for storing resources.
 */
public interface ServerInformation extends Serializable, HasName {
    String getName();
    String getDescription();
}
