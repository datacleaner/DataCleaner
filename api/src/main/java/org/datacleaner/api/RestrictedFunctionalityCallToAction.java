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
package org.datacleaner.api;

import java.io.Serializable;

/**
 * Represents a "Call to Action" for users when restricted functionality is
 * blocked, typically with the use of {@link RestrictedFunctionalityException}
 * or {@link RestrictedFunctionalityMessage}.
 */
public class RestrictedFunctionalityCallToAction implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String _name;
    private final String _href;
    private final String _description;

    /**
     * Constructs a {@link RestrictedFunctionalityCallToAction}.
     * 
     * @param name
     *            the name of the action, e.g. "Upgrade to Enterprise edition"
     *            or "Buy credits"
     * @param href
     *            a HTTP link to open if the user wants to know more
     */
    public RestrictedFunctionalityCallToAction(String name, String href) {
        this(name, href, null);
    }

    /**
     * Constructs a {@link RestrictedFunctionalityCallToAction}.
     * 
     * @param name
     *            the name of the action, e.g. "Upgrade to Enterprise edition"
     *            or "Buy credits"
     * @param href
     *            a HTTP link to open if the user wants to know more
     * @param description
     *            a longer description of the action, typically presented with a
     *            tooltip, a smaller-font text or so
     */
    public RestrictedFunctionalityCallToAction(String name, String href, String description) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Action name cannot be null");
        }
        if (href == null || href.isEmpty()) {
            throw new IllegalArgumentException("Action href cannot be null");
        }
        _name = name;
        _href = href;
        _description = description;
    }

    /**
     * Gets the action's HTTP link to open if the user wants to know more
     * 
     * @return the action HTTP link/URL
     */
    public String getHref() {
        return _href;
    }

    /**
     * Gets the name of the action, e.g. "Upgrade to Enterprise edition" or
     * "Buy credits"
     * 
     * @return the action name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets a longer description of the action, typically presented with a
     * tooltip, a smaller-font text or so
     * 
     * @return a long description or null if none is present
     */
    public String getDescription() {
        return _description;
    }
}
