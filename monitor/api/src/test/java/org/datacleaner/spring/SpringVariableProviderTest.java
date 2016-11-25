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
package org.datacleaner.spring;

import java.util.HashMap;
import java.util.Map;

import org.datacleaner.monitor.job.JobContext;
import org.datacleaner.monitor.scheduling.model.ExecutionLog;
import org.easymock.EasyMock;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import junit.framework.TestCase;

public class SpringVariableProviderTest extends TestCase {

    public void testProvideVariables() throws Exception {
        System.setProperty("DataCleaner.foo", "bar");

        final SpringVariableProvider provider = new SpringVariableProvider();
        final StandardEnvironment standardEnvironment = new StandardEnvironment();
        final Map<String, Object> map = new HashMap<>();
        map.put("foo", "fooooo");
        map.put("bar", "baaaar");
        final PropertySource<?> propertySource = new MapPropertySource("foo", map);
        standardEnvironment.getPropertySources().addFirst(propertySource);
        provider.environment = standardEnvironment;

        final JobContext job = EasyMock.createMock(JobContext.class);

        final Map<String, String> inputVariables = new HashMap<>();
        inputVariables.put("foobar", "1");
        inputVariables.put("${foo}", "hello");
        inputVariables.put("#{DataCleaner.foo}", "hello");
        inputVariables.put("greeting", "${foo}");
        inputVariables.put("talking", "${blablabla}");

        EasyMock.expect(job.getVariables()).andReturn(inputVariables);

        EasyMock.replay(job);

        final Map<String, String> result = provider.provideValues(job, new ExecutionLog());

        EasyMock.verify(job);

        assertEquals(result.toString(), 3, result.size());
        assertEquals(result.toString(), "fooooo", result.get("${foo}"));
        assertEquals(result.toString(), "fooooo", result.get("greeting"));
        assertEquals(result.toString(), "bar", result.get("#{DataCleaner.foo}"));
    }
}
