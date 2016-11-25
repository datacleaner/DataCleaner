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

import javax.inject.Inject;

import org.apache.metamodel.schema.Table;
import org.datacleaner.configuration.InjectionManager;
import org.datacleaner.configuration.InjectionManagerFactory;
import org.datacleaner.configuration.InjectionPoint;
import org.datacleaner.lifecycle.MemberInjectionPoint;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class SpringInjectionManagerFactoryTest extends TestCase {

    @Inject
    HelloBean injectedBean;

    @Inject
    Table uninjectableBean;

    public void testCannotInjectSpringBean() throws Exception {
        try (ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(
                "example-spring-injection-manager-context.xml")) {

            final InjectionManagerFactory injectionManagerFactory = appCtx.getBean(InjectionManagerFactory.class);

            final InjectionManager injectionManager = injectionManagerFactory.getInjectionManager(null, null);

            assertNull(uninjectableBean);
            final InjectionPoint<Table> injectionPoint =
                    new MemberInjectionPoint<>(getClass().getDeclaredField("uninjectableBean"), this);
            assertNotNull(injectionManager);

            final Table value = injectionManager.getInstance(injectionPoint);
            assertNull(value);
            assertNull(uninjectableBean);
        }
    }

    public void testInjectSpringBean() throws Exception {
        try (ClassPathXmlApplicationContext appCtx = new ClassPathXmlApplicationContext(
                "example-spring-injection-manager-context.xml")) {

            final InjectionManagerFactory injectionManagerFactory = appCtx.getBean(InjectionManagerFactory.class);

            final InjectionManager injectionManager = injectionManagerFactory.getInjectionManager(null, null);

            assertNull(injectedBean);
            final InjectionPoint<HelloBean> injectionPoint =
                    new MemberInjectionPoint<>(getClass().getDeclaredField("injectedBean"), this);
            assertNotNull(injectionManager);

            final HelloBean value = injectionManager.getInstance(injectionPoint);
            assertNotNull(value);
            assertNull(injectedBean);

            assertEquals("Hello DC world", value.getMessage());
        }
    }
}
