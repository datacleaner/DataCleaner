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

import java.util.HashMap;
import java.util.Map;

import org.eobjects.analyzer.configuration.AnalyzerBeansConfiguration;
import org.eobjects.datacleaner.Main;
import org.eobjects.datacleaner.monitor.configuration.TenantContext;
import org.eobjects.datacleaner.monitor.configuration.TenantContextFactory;
import org.eobjects.datacleaner.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * A controller used for providing a "ping" service which can check if the
 * system is alive, if a particular tenant is valid and if the security allowed
 * access at all.
 */
@Controller
@RequestMapping("/{tenant}/ping")
public class PingController {

    @Autowired
    Repository _repository;

    @Autowired
    TenantContextFactory _tenantContextFactory;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Map<String, Object> ping(@PathVariable("tenant") final String tenant) {
        final TenantContext tenantContext = _tenantContextFactory.getContext(tenant);
        final AnalyzerBeansConfiguration configuration = tenantContext.getConfiguration();

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("tenant", tenant);
        map.put("version", Main.VERSION);
        map.put("ping", "pong");
        map.put("configuration_check", (configuration == null ? "invalid" : "valid"));

        return map;
    }
}
