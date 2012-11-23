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
package org.jasig.cas.client.authentication;

import java.util.Collections;
import java.util.Map;

import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;

/**
 * Concrete implementation of the AttributePrincipal interface.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class AttributePrincipalImpl implements AttributePrincipal {

    // private static final Log LOG =
    // LogFactory.getLog(AttributePrincipalImpl.class);

    /** Unique Id for Serialization */
    private static final long serialVersionUID = -8810123156070148535L;

    /** The unique identifier for this principal. */
    private final String name;

    /** Map of key/value pairs about this principal. */
    @SuppressWarnings("rawtypes")
    private final Map attributes;

    /** The CAS 2 ticket used to retrieve a proxy ticket. */
    private final String proxyGrantingTicket;

    /** The method to retrieve a proxy ticket from a CAS server. */
    private final ProxyRetriever proxyRetriever;

    /**
     * Constructs a new principal with an empty map of attributes.
     * 
     * @param name
     *            the unique identifier for the principal.
     */
    public AttributePrincipalImpl(final String name) {
        this(name, Collections.EMPTY_MAP);
    }

    /**
     * Constructs a new principal with the supplied name and attributes.
     * 
     * @param name
     *            the unique identifier for the principal.
     * @param attributes
     *            the key/value pairs for this principal.
     */
    public AttributePrincipalImpl(final String name, @SuppressWarnings("rawtypes") final Map attributes) {
        this(name, attributes, null, null);
    }

    /**
     * Constructs a new principal with the supplied name and the proxying
     * capabilities.
     * 
     * @param name
     *            the unique identifier for the principal.
     * @param proxyGrantingTicket
     *            the ticket associated with this principal.
     * @param proxyRetriever
     *            the ProxyRetriever implementation to call back to the CAS
     *            server.
     */
    public AttributePrincipalImpl(final String name, final String proxyGrantingTicket, final ProxyRetriever proxyRetriever) {
        this(name, Collections.EMPTY_MAP, proxyGrantingTicket, proxyRetriever);
    }

    /**
     * Constructs a new principal witht he supplied name, attributes, and
     * proxying capabilities.
     * 
     * @param name
     *            the unique identifier for the principal.
     * @param attributes
     *            the key/value pairs for this principal.
     * @param proxyGrantingTicket
     *            the ticket associated with this principal.
     * @param proxyRetriever
     *            the ProxyRetriever implementation to call back to the CAS
     *            server.
     */
    @SuppressWarnings("rawtypes")
    public AttributePrincipalImpl(final String name, final Map attributes, final String proxyGrantingTicket, final ProxyRetriever proxyRetriever) {
        this.name = name;
        this.attributes = attributes;
        this.proxyGrantingTicket = proxyGrantingTicket;
        this.proxyRetriever = proxyRetriever;

        CommonUtils.assertNotNull(this.name, "name cannot be null.");
        CommonUtils.assertNotNull(this.attributes, "attributes cannot be null.");
    }

    /**
     * get Map Attributes
     * 
     * @return atrributes
     */
    public Map getAttributes() {
        return this.attributes;
    }

    /**
     * get proxy ticket
     * 
     * @return proxyTicket
     */
    public String getProxyTicketFor(String service) {
        if (proxyGrantingTicket != null) {
            return this.proxyRetriever.getProxyTicketIdFor(this.proxyGrantingTicket, service);
        }

        // LOG.debug("No ProxyGrantingTicket was supplied, so no Proxy Ticket can be retrieved.");
        return null;
    }

    /**
     * get username
     * 
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * proxy getter
     * 
     * @return String
     */
    public String getProxyGrantingTicket() {
        return this.proxyGrantingTicket;
    }
}
