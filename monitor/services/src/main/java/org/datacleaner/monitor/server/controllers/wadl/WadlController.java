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
package org.datacleaner.monitor.server.controllers.wadl;

import com.autentia.web.rest.wadl.builder.ApplicationBuilder;
import com.autentia.web.rest.wadl.builder.impl.springframework.SpringWadlBuilderFactory;
import com.autentia.xml.schema.ClassTypeNotFoundException;
import com.autentia.xml.schema.SchemaBuilder;
import net.java.dev.wadl._2009._02.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "{tenant}/doc", produces = {"application/xml"})
public class WadlController {
    private final ApplicationBuilder applicationBuilder;
    private final SchemaBuilder schemaBuilder;

    @Autowired
    public WadlController(RequestMappingHandlerMapping handlerMapping) {
        final SpringWadlBuilderFactory wadlBuilderFactory = new SpringWadlBuilderFactory(handlerMapping);
        applicationBuilder = wadlBuilderFactory.getApplicationBuilder();
        schemaBuilder = wadlBuilderFactory.getSchemaBuilder();
    }

    @ResponseBody
    @RequestMapping(value = "wadl", method = RequestMethod.GET)
    public Application generateWadl(HttpServletRequest request) {
        return applicationBuilder.build(request);
    }

    @ResponseBody
    @RequestMapping(value = "schema/{classTypeName}", method = RequestMethod.GET)
    public String generateSchema(@PathVariable String classTypeName) {
        return schemaBuilder.buildFor(classTypeName);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ClassTypeNotFoundException.class)
    public void handleException() {
        // Do nothing, just magic annotations to return 404.
    }

    /*
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String test(HttpServletRequest request) {
        return "test";
    }
    */

}
