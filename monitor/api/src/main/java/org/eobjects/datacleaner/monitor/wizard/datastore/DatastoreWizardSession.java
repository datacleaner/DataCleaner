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
package org.eobjects.datacleaner.monitor.wizard.datastore;

import javax.xml.parsers.DocumentBuilder;

import org.eobjects.datacleaner.monitor.wizard.WizardSession;
import org.w3c.dom.Element;

/**
 * Represents the session of creating a datastore
 */
public interface DatastoreWizardSession extends WizardSession {

    /**
     * Creates the final datastore node (to be inserted into conf.xml) as
     * prescribed by the wizard. This method will be invoked when no more pages
     * are available and the wizard has ended.
     * 
     * @param documentBuilder
     * 
     * @return
     */
    public Element createDatastoreElement(DocumentBuilder documentBuilder);
}
