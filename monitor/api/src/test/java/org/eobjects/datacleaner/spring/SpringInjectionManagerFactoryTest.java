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
package org.eobjects.datacleaner.spring;

import javax.inject.Inject;

import org.eobjects.analyzer.configuration.InjectionManager;
import org.eobjects.analyzer.configuration.InjectionManagerFactory;
import org.eobjects.analyzer.configuration.InjectionPoint;
import org.eobjects.analyzer.lifecycle.MemberInjectionPoint;
import org.eobjects.metamodel.schema.Table;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class SpringInjectionManagerFactoryTest extends TestCase {
    
    @Inject
    HelloBean injectedBean;
    
    @Inject
    Table uninjectableBean;
    
    public void testCannotInjectSpringBean() throws Exception {
        ApplicationContext appCtx = new ClassPathXmlApplicationContext(
                "example-spring-injection-manager-context.xml");
        
        InjectionManagerFactory injectionManagerFactory = appCtx.getBean(InjectionManagerFactory.class);
        
        InjectionManager injectionManager = injectionManagerFactory.getInjectionManager(null, null);
        
        assertNull(uninjectableBean);
        InjectionPoint<Table> injectionPoint = new MemberInjectionPoint<Table>(getClass().getDeclaredField("uninjectableBean"), this);
        assertNotNull(injectionManager);

        Table value = injectionManager.getInstance(injectionPoint);
        assertNull(value);
        assertNull(uninjectableBean);
    }

    public void testInjectSpringBean() throws Exception {
        ApplicationContext appCtx = new ClassPathXmlApplicationContext(
                "example-spring-injection-manager-context.xml");
        
        
        InjectionManagerFactory injectionManagerFactory = appCtx.getBean(InjectionManagerFactory.class);
        
        InjectionManager injectionManager = injectionManagerFactory.getInjectionManager(null, null);
        
        assertNull(injectedBean);
        InjectionPoint<HelloBean> injectionPoint = new MemberInjectionPoint<HelloBean>(getClass().getDeclaredField("injectedBean"), this);
        assertNotNull(injectionManager);

        HelloBean value = injectionManager.getInstance(injectionPoint);
        assertNotNull(value);
        assertNull(injectedBean);
        
        assertEquals("Hello DC world", value.getMessage());
    }
}
