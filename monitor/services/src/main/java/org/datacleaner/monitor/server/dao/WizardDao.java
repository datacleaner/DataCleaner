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
package org.eobjects.datacleaner.monitor.server.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.wizard.Wizard;
import org.eobjects.datacleaner.monitor.wizard.WizardSession;
import org.apache.metamodel.util.Func;

/**
 * A kind of DAO (Data Access Object) and utility class for {@link Wizard}
 * handling on the server side.
 */
public interface WizardDao {

    /**
     * Gets all wizards of a specific type
     * 
     * @param wizardClass
     * @return
     */
    public <W extends Wizard<?, ?>> Collection<W> getWizardsOfType(Class<W> wizardClass);

    /**
     * Starts a wizard session
     * 
     * @param wizardIdentifier
     * @param session
     * @return
     */
    public WizardPage startSession(WizardIdentifier wizardIdentifier, WizardSession session);

    /**
     * Closes a wizard session
     * 
     * @param sessionId
     */
    public void closeSession(String sessionId);

    /**
     * Navigates to the next page in a wizard session.
     * 
     * @param tenant
     * @param sessionIdentifier
     * @param formParameters
     * @return
     * @throws DCUserInputException
     */
    public WizardPage nextPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) throws DCUserInputException;
    
    /**
     * Navigates to the previous page in a wizard session.
     * 
     * @param tenant
     * @param sessionIdentifier
     * @return
     */
    public WizardPage previousPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier);

    /**
     * Creates a function representing the HTTP session of the user.
     * 
     * @return
     */
    public Func<String, Object> createSessionFunc();
}
