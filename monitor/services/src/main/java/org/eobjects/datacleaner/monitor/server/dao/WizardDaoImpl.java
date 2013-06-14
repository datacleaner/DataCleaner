/**
 * DataCleaner (community edition)
 * Copyright (C) 2013 Human Inference
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
package org.eobjects.datacleaner.monitor.server.dao;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import org.eobjects.datacleaner.monitor.shared.model.DCUserInputException;
import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardIdentifier;
import org.eobjects.datacleaner.monitor.shared.model.WizardPage;
import org.eobjects.datacleaner.monitor.shared.model.WizardSessionIdentifier;
import org.eobjects.datacleaner.monitor.wizard.Wizard;
import org.eobjects.datacleaner.monitor.wizard.WizardPageController;
import org.eobjects.datacleaner.monitor.wizard.WizardSession;
import org.eobjects.metamodel.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Main implementation of {@link WizardDao}.
 */
@Component
public class WizardDaoImpl implements WizardDao {

    private static final Logger logger = LoggerFactory.getLogger(WizardDaoImpl.class);

    private static class WizardState {
        WizardSession session;
        Deque<WizardPageController> pages;
    }

    private final Cache<String, WizardState> _wizardStateCache;
    private final ApplicationContext _applicationContext;

    @Autowired
    public WizardDaoImpl(ApplicationContext applicationContext) {
        _applicationContext = applicationContext;
        _wizardStateCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    }

    @Override
    public <W extends Wizard<?, ?>> Collection<W> getWizardsOfType(Class<W> wizardClass) {
        return _applicationContext.getBeansOfType(wizardClass).values();
    }
    
    @Override
    public WizardPage previousPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier) {
        final String sessionId = sessionIdentifier.getSessionId();
        final WizardState state = getWizardState(sessionId);
        final WizardSession session = state.session;

        // remove the current page from the stack of pages
        state.pages.pollLast();
        final WizardPageController previousPage = state.pages.getLast();
        
        return createPage(sessionIdentifier, previousPage, session);
    }

    @Override
    public WizardPage nextPage(TenantIdentifier tenant, WizardSessionIdentifier sessionIdentifier,
            Map<String, List<String>> formParameters) throws DCUserInputException {
        final String sessionId = sessionIdentifier.getSessionId();
        final WizardState state = getWizardState(sessionId);
        final WizardPageController controller = state.pages.getLast();

        final WizardPageController nextPageController;

        try {
            nextPageController = controller.nextPageController(formParameters);
        } catch (DCUserInputException e) {
            logger.info("A user input exception was thrown by wizard controller - rethrowing to UI: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            logger.error("An unexpected error occurred in the wizard controller, wizard will be closed", e);
            closeSession(sessionId);
            throw e;
        }

        final WizardSession session = state.session;
        if (nextPageController == null) {
            final String wizardResult;
            try {
                wizardResult = session.finished();
            } finally {
                closeSession(sessionId);
            }

            // returning null signals that no more pages should be shown, the
            // wizard is done.
            return createFinishPage(sessionIdentifier, wizardResult);
        } else {
            state.pages.addLast(nextPageController);
            return createPage(sessionIdentifier, nextPageController, session);
        }
    }

    private WizardState getWizardState(String sessionId) {
        return _wizardStateCache.getIfPresent(sessionId);
    }

    @Override
    public WizardPage startSession(WizardIdentifier wizardIdentifier, WizardSession session) {
        final String sessionId = createSessionId();

        final WizardPageController firstPage = session.firstPageController();

        final WizardState state = new WizardState();
        state.session = session;
        state.pages = new ArrayDeque<WizardPageController>();
        state.pages.add(firstPage);

        _wizardStateCache.put(sessionId, state);

        final WizardSessionIdentifier sessionIdentifier = new WizardSessionIdentifier(sessionId, wizardIdentifier);

        return createPage(sessionIdentifier, firstPage, session);
    }

    @Override
    public void closeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        _wizardStateCache.invalidate(sessionId);
    }

    /**
     * Create a convenience function that wraps the http session.
     * 
     * @return
     */
    @Override
    public Func<String, Object> createSessionFunc() {
        return new Func<String, Object>() {
            @Override
            public Object eval(String key) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();
                HttpSession session = requestAttributes.getRequest().getSession(true);
                return session.getAttribute(key);
            }
        };
    }

    /**
     * Creates a "page" that symbolizes a finished wizard.
     * 
     * @param sessionId
     * @param wizardResult
     * @return
     */
    private WizardPage createFinishPage(WizardSessionIdentifier sessionIdentifier, String wizardResult) {
        WizardPage page = new WizardPage();
        page.setPageIndex(WizardPage.PAGE_INDEX_FINISHED);
        page.setSessionIdentifier(sessionIdentifier);
        page.setWizardResult(wizardResult);
        return page;
    }

    private WizardPage createPage(WizardSessionIdentifier sessionIdentifier, WizardPageController pageController,
            WizardSession session) {
        final WizardPage page = new WizardPage();
        page.setSessionIdentifier(sessionIdentifier);
        page.setFormInnerHtml(pageController.getFormInnerHtml());
        page.setPageIndex(pageController.getPageIndex());
        if (session != null) {
            page.setExpectedPageCount(session.getPageCount());
        }
        return page;
    }

    private String createSessionId() {
        return UUID.randomUUID().toString();
    }

    public long getOpenSessionCount() {
        return _wizardStateCache.size();
    }

}
