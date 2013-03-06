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
package org.eobjects.datacleaner.monitor.server.controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eobjects.analyzer.connection.Datastore;
import org.eobjects.analyzer.connection.DatastoreCatalog;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.monitor.shared.model.SecurityRoles;
import org.eobjects.datacleaner.repository.RepositoryFile;
import org.eobjects.metamodel.util.Action;
import org.eobjects.metamodel.util.Func;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.xml.DomUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

@Controller
@RequestMapping(value = "/{tenant}/datastores")
public class DatastoresFolderController {

    @Autowired
    TenantContextFactory _contextFactory;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Map<String, String>> datastoresFolderJson(@PathVariable("tenant") String tenant) {
        final TenantContext context = _contextFactory.getContext(tenant);

        final DatastoreCatalog datastoreCatalog = context.getConfiguration().getDatastoreCatalog();
        final String[] names = datastoreCatalog.getDatastoreNames();

        final List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (String name : names) {
            final Datastore datastore = datastoreCatalog.getDatastore(name);
            final Map<String, String> map = new LinkedHashMap<String, String>();
            map.put("name", name);
            map.put("description", datastore.getDescription());
            map.put("type", datastore.getClass().getSimpleName());
            result.add(map);
        }

        return result;
    }

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(method = RequestMethod.POST)
    protected void registerDatastore(@PathVariable("tenant") String tenant, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        // parse the incoming datastore definition
        final BufferedReader reader = request.getReader();
        final InputSource inputSource = new InputSource(reader);
        final Document datastoreDocument = documentBuilder.parse(inputSource);

        final TenantContext tenantContext = _contextFactory.getContext(tenant);

        addDatastoreElementToConfiguration(tenantContext, datastoreDocument.getDocumentElement(), documentBuilder);
    }

    public static void addDatastoreElementToConfiguration(TenantContext tenantContext, Element datastoreElement,
            final DocumentBuilder documentBuilder) throws ParserConfigurationException,
            TransformerConfigurationException, IllegalStateException {

        final RepositoryFile confFile = tenantContext.getConfigurationFile();
        // parse the configuration file
        final Document configurationFileDocument = confFile.readFile(new Func<InputStream, Document>() {
            @Override
            public Document eval(InputStream in) {
                try {
                    return documentBuilder.parse(in);
                } catch (Exception e) {
                    throw new IllegalStateException("Could not parse configuration file", e);
                }
            }
        });

        // add the new datastore to the <datastore-catalog> element of the
        // configuration file
        final Element configurationFileDocumentElement = configurationFileDocument.getDocumentElement();

        final Element datastoreCatalogElement = DomUtils.getChildElementByTagName(configurationFileDocumentElement,
                "datastore-catalog");
        if (datastoreCatalogElement == null) {
            throw new IllegalStateException("Could not find <datastore-catalog> element in configuration file");
        }

        final Node importedNode = configurationFileDocument.importNode(datastoreElement, true);
        datastoreCatalogElement.appendChild(importedNode);

        // write the updated configuration file
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        final Source source = new DOMSource(configurationFileDocument);

        confFile.writeFile(new Action<OutputStream>() {
            @Override
            public void run(OutputStream out) throws Exception {
                final Result outputTarget = new StreamResult(out);
                transformer.transform(source, outputTarget);
                out.flush();
            }
        });
    }
}
