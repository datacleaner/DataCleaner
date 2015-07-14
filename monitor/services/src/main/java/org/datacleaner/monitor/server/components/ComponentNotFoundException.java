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
package org.datacleaner.monitor.server.components;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author j.horcicka (GMC)
 * @since 14. 07. 2015
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "There is no such component.")
public class ComponentNotFoundException extends RuntimeException {
    public ComponentNotFoundException(String type) {
        super("Component type '" + type + "' does not exist.");
    }

    public ComponentNotFoundException(Integer id) {
        super("Component with ID " + id + " does not exist.");
    }
}
