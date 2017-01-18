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
package org.datacleaner.monitor.server.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.metamodel.util.FileResource;
import org.datacleaner.monitor.configuration.TenantContext;
import org.datacleaner.monitor.configuration.TenantContextFactory;
import org.datacleaner.monitor.server.controllers.referencedata.model.DictionaryModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.ReferenceDataModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.ReferenceDataType;
import org.datacleaner.monitor.server.controllers.referencedata.model.StringPatternModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.SynonymCatalogModel;
import org.datacleaner.monitor.server.controllers.referencedata.model.SynonymModel;
import org.datacleaner.monitor.server.dao.ReferenceDataDao;
import org.datacleaner.monitor.shared.model.SecurityRoles;
import org.datacleaner.reference.Dictionary;
import org.datacleaner.reference.ReferenceDataCatalog;
import org.datacleaner.reference.RegexStringPattern;
import org.datacleaner.reference.SimpleStringPattern;
import org.datacleaner.reference.StringPattern;
import org.datacleaner.reference.Synonym;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.TextFileSynonymCatalog;
import org.datacleaner.reference.regexswap.RegexSwapStringPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Joiner;

@RestController
@RequestMapping(value = "/{tenant}/referencedata")
public class ReferenceDataController {
    private static final Logger logger = LoggerFactory.getLogger(ReferenceDataController.class);

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public class NoSuchResourceException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final TenantContextFactory _contextFactory;
    private final ReferenceDataDao _referenceDataDao;

    @Autowired
    public ReferenceDataController(final TenantContextFactory contextFactory, final ReferenceDataDao referenceDataDao) {
        _contextFactory = contextFactory;
        _referenceDataDao = referenceDataDao;
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReferenceDataModel> getReferenceData(@PathVariable("tenant") final String tenant) {
        return Stream.of(createRDMStream(tenant, ReferenceDataType.DICTIONARY),
                createRDMStream(tenant, ReferenceDataType.SYNONYM_CATALOG),
                createRDMStream(tenant, ReferenceDataType.STRING_PATTERN)).flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/dictionaries", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReferenceDataModel> getDictionaries(@PathVariable("tenant") final String tenant) {
        return createRDMStream(tenant, ReferenceDataType.DICTIONARY).collect(Collectors.toList());
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/synonymCatalogs", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReferenceDataModel> getSynonymCatalogs(@PathVariable("tenant") final String tenant) {
        return createRDMStream(tenant, ReferenceDataType.SYNONYM_CATALOG).collect(Collectors.toList());
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/stringPatterns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ReferenceDataModel> getStringPatterns(@PathVariable("tenant") final String tenant) {
        return createRDMStream(tenant, ReferenceDataType.STRING_PATTERN).collect(Collectors.toList());
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/stringPattern/{name}", method = { RequestMethod.GET, RequestMethod.HEAD },
            produces = MediaType.APPLICATION_JSON_VALUE)
    public StringPatternModel getStringPattern(@PathVariable("tenant") final String tenant,
            @PathVariable("name") final String name, final HttpServletRequest request) {
        final StringPattern stringPattern = getReferenceDataCatalog(tenant).getStringPattern(name);

        if (stringPattern == null) {
            logger.warn("Could not find string pattern \"" + name + "\"");
            throw new NoSuchResourceException();
        }

        if (request.getMethod().equals(RequestMethod.HEAD.name())) {
            return null;
        }

        if (stringPattern instanceof SimpleStringPattern) {
            return new StringPatternModel(name, ((SimpleStringPattern) stringPattern).getExpression(),
                    StringPatternModel.PatternType.STRING);
        } else if (stringPattern instanceof RegexStringPattern) {
            final RegexStringPattern regexStringPattern = (RegexStringPattern) stringPattern;
            return new StringPatternModel(name, regexStringPattern.getExpression(),
                    regexStringPattern.isMatchEntireString() ? StringPatternModel.PatternType.REGEX_MATCH_ENTIRE_STRING
                            : StringPatternModel.PatternType.REGEX);
        } else { // RegexSwap. Treat as regex.
            final RegexSwapStringPattern regexStringPattern = (RegexSwapStringPattern) stringPattern;
            return new StringPatternModel(name, regexStringPattern.getRegex().getExpression(),
                    StringPatternModel.PatternType.REGEX);
        }
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/dictionary/{name}", method = { RequestMethod.GET, RequestMethod.HEAD },
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DictionaryModel getDictionary(@PathVariable("tenant") final String tenant,
            @PathVariable("name") final String name, final HttpServletRequest request) {
        final Dictionary dictionary = getReferenceDataCatalog(tenant).getDictionary(name);

        if (dictionary == null) {
            throw new NoSuchResourceException();
        }

        if (request.getMethod().equals(RequestMethod.HEAD.name())) {
            return null;
        }

        final List<String> entries =
                dictionary.openConnection(_contextFactory.getContext(tenant).getConfiguration()).stream()
                        .collect(Collectors.toList());
        return new DictionaryModel(name, entries);
    }

    @RolesAllowed(SecurityRoles.VIEWER)
    @RequestMapping(value = "/synonymCatalog/{name}", method = { RequestMethod.GET, RequestMethod.HEAD },
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SynonymCatalogModel getSynonymCatalog(@PathVariable("tenant") final String tenant,
            @PathVariable("name") final String name, final HttpServletRequest request) {
        final SynonymCatalog synonymCatalog = getReferenceDataCatalog(tenant).getSynonymCatalog(name);
        if (synonymCatalog == null) {
            throw new NoSuchResourceException();
        }

        if (request.getMethod().equals(RequestMethod.HEAD.name())) {
            return null;
        }

        final Collection<SynonymModel> entries =
                synonymCatalog.openConnection(_contextFactory.getContext(tenant).getConfiguration()).getSynonyms()
                        .stream().map(s -> new SynonymModel(s.getMasterTerm(), s.getSynonyms()))
                        .collect(Collectors.toList());

        return new SynonymCatalogModel(name, entries, synonymCatalog.isCaseSensitive());
    }

    @RolesAllowed(SecurityRoles.CONFIGURATION_EDITOR)
    @RequestMapping(value = "/synonymCatalog/{name}", method = { RequestMethod.PUT },
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void putSynonymCatalog(@PathVariable("tenant") final String tenant, @PathVariable("name") final String name,
            @RequestBody final SynonymCatalogModel synonymCatalogModel) {
        logger.warn("I'm here");
        final TenantContext context = _contextFactory.getContext(tenant);
        final File tenantHome = context.getConfiguration().getHomeFolder().toFile();
        final String filePath =
                tenantHome.getAbsolutePath() + File.separator + "reference-data" + File.separator + name;
        final FileResource resource = new FileResource(filePath);
        resource.write(stream -> {
            for (final Synonym synonym : synonymCatalogModel.getEntries()) {
                final List<String> line = new ArrayList<>(synonym.getSynonyms().size() + 1);
                line.add(synonym.getMasterTerm());
                line.addAll(synonym.getSynonyms());
                stream.write((Joiner.on(",").join(line) + "\n").getBytes("UTF-8"));
            }
        });
        final TextFileSynonymCatalog catalog =
                new TextFileSynonymCatalog(synonymCatalogModel.getName(), resource.getFile(),
                        synonymCatalogModel.isCaseSensitive(), "UTF-8");

        _referenceDataDao.addSynonymCatalog(context, catalog);
    }

    private Stream<ReferenceDataModel> createRDMStream(final String tenant, final ReferenceDataType type) {
        return getNameStreamOf(type, tenant).map(d -> createReferenceDataModel(d, tenant, type));
    }

    private Stream<String> getNameStreamOf(final ReferenceDataType type, final String tenant) {
        final ReferenceDataCatalog referenceDataCatalog = getReferenceDataCatalog(tenant);

        switch (type) {
        case DICTIONARY:
            return Arrays.stream(referenceDataCatalog.getDictionaryNames());
        case SYNONYM_CATALOG:
            return Arrays.stream(referenceDataCatalog.getSynonymCatalogNames());
        case STRING_PATTERN:
            return Arrays.stream(referenceDataCatalog.getStringPatternNames());
        default:
            throw new IllegalArgumentException("No such ReferenceDataType: " + type);
        }
    }

    private ReferenceDataCatalog getReferenceDataCatalog(final String tenant) {
        return _contextFactory.getContext(tenant).getConfiguration().getReferenceDataCatalog();
    }

    private ReferenceDataModel createReferenceDataModel(final String name, final String tenant,
            final ReferenceDataType type) {
        return new ReferenceDataModel(name, "/repository" + tenant + "/referencedata/" + type.getName() + "/" + name, type);
    }

}
