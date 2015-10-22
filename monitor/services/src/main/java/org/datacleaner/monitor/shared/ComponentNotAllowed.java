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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Class ComponentNotAllowed
 * 
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Component is not allowed.")
public class ComponentNotAllowed extends RuntimeException {
    private ComponentNotAllowed(String msg) {
        super(msg);
    }

    public static ComponentNotAllowed createInstanceNotAllowed(String componentName) {
        return new ComponentNotAllowed("Component with name " + componentName + " is not allowed.");
    }
}
