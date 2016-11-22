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
package org.datacleaner.monitor.shared;

import java.util.List;
import java.util.Map;

import org.datacleaner.monitor.shared.model.DCUserInputException;
import org.datacleaner.monitor.shared.model.TenantIdentifier;
import org.datacleaner.monitor.shared.model.WizardPage;
import org.datacleaner.monitor.shared.model.WizardSessionIdentifier;

/**
 * Super interface for wizard services. This interface contains only the
 * agnostic parts of wizard handling, primarily dealing with navigation.
 */
public interface WizardNavigationService {

    WizardPage nextPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) throws DCUserInputException;

    WizardPage previousPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier);

    Boolean cancelWizard(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier);
}
