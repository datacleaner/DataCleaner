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
package org.eobjects.datacleaner.monitor.shared.model;

/**
 * Exception or super-class of exceptions that relate to the user input of the
 * DC monitor. When exceptions like this is thrown, the user interface will
 * treat it as a missing or faulty user input situation, rather than an
 * application error.
 */
public class DCUserInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@link DCUserInputException} with a particular message to
     * the user.
     * 
     * @param message
     */
    public DCUserInputException(String message) {
        super(message);
    }
    
    public DCUserInputException() {
        super();
    }
}
