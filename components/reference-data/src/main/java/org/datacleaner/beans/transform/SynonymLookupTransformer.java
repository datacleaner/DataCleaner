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
package org.datacleaner.beans.transform;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.metamodel.util.HasName;
import org.datacleaner.api.Alias;
import org.datacleaner.api.Categorized;
import org.datacleaner.api.Close;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExternalDocumentation;
import org.datacleaner.api.ExternalDocumentation.DocumentationLink;
import org.datacleaner.api.ExternalDocumentation.DocumentationType;
import org.datacleaner.api.HasLabelAdvice;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.ImproveSuperCategory;
import org.datacleaner.components.categories.ReferenceDataCategory;
import org.datacleaner.configuration.DataCleanerConfiguration;
import org.datacleaner.reference.SynonymCatalog;
import org.datacleaner.reference.SynonymCatalogConnection;

import com.google.common.base.Joiner;

/**
 * A simple transformer that uses a synonym catalog to replace a synonym with
 * it's master term.
 */
@Named("Synonym lookup")
@Alias("Synonym replacement")
@Description("Replaces strings with their synonyms")
@ExternalDocumentation({
        @DocumentationLink(title = "Segmenting customers on messy data", url = "https://www.youtube.com/watch?v=iy-j5s-uHz4", type = DocumentationType.VIDEO, version = "4.0"),
        @DocumentationLink(title = "Understanding and using Synonyms", url = "https://www.youtube.com/watch?v=_YiPaA8bFt4", type = DocumentationType.VIDEO, version = "2.0") })
@Categorized(superCategory = ImproveSuperCategory.class, value = ReferenceDataCategory.class)
public class SynonymLookupTransformer implements Transformer, HasLabelAdvice {
    public enum ReplacedSynonymsType implements HasName {
        STRING("String"), LIST("List");

        private final String _name;

        ReplacedSynonymsType(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }
    }

    @Configured
    InputColumn<String> column;

    @Configured
    SynonymCatalog synonymCatalog;

    @Configured
    @Description("Retain original value when no synonyms are found. If turned off, <null> will be returned when no synonyms are found.")
    boolean retainOriginalValue = true;

    @Configured
    @Alias("Look up every token")
    @Description("Replace synonyms that occur as a substring within the complete text? If turned off, only synonyms that match the complete text value will be replaced.")
    boolean replaceInlinedSynonyms = true;

    @Inject
    @Configured
    @Description("How should the synonyms and the master terms that replaced them be returned?"
            + " As a concatenated String or as a List.")
    ReplacedSynonymsType replacedSynonymsType = ReplacedSynonymsType.STRING;

    @Provided
    DataCleanerConfiguration configuration;

    private SynonymCatalogConnection synonymCatalogConnection;

    public SynonymLookupTransformer() {
    }

    public SynonymLookupTransformer(InputColumn<String> column, SynonymCatalog synonymCatalog,
            boolean retainOriginalValue, DataCleanerConfiguration configuration) {
        this();
        this.column = column;
        this.synonymCatalog = synonymCatalog;
        this.retainOriginalValue = retainOriginalValue;
        this.configuration = configuration;
    }

    @Override
    public OutputColumns getOutputColumns() {
        final Class<?>[] columnTypes;
        if (replacedSynonymsType == ReplacedSynonymsType.STRING) {
            columnTypes = new Class[] { String.class, String.class, String.class };
        } else {
            columnTypes = new Class[] { String.class, List.class, List.class };
        }

        return new OutputColumns(new String[] { column.getName() + " (synonyms replaced)", column.getName()
                + " (synonyms found)", column.getName() + " (master terms found)" }, columnTypes);
    }

    @Override
    public String getSuggestedLabel() {
        if (synonymCatalog == null) {
            return null;
        }
        return "Lookup: " + synonymCatalog.getName();
    }

    @Initialize
    public void init() {
        synonymCatalogConnection = synonymCatalog.openConnection(configuration);
    }

    @Close
    public void close() {
        if (synonymCatalogConnection != null) {
            synonymCatalogConnection.close();
            synonymCatalogConnection = null;
        }
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        final String originalValue = inputRow.getValue(column);

        if (originalValue == null) {
            return new String[3];
        }

        if (replaceInlinedSynonyms) {
            final SynonymCatalogConnection.Replacement replacement = synonymCatalogConnection.replaceInline(
                    originalValue);
            if (replacedSynonymsType == ReplacedSynonymsType.STRING) {
                return new Object[] { replacement.getReplacedString(), Joiner.on(' ').join(replacement.getSynonyms()),
                        Joiner.on(' ').join(replacement.getMasterTerms()) };
            } else {
                return new Object[] { replacement.getReplacedString(), replacement.getSynonyms(), replacement
                        .getMasterTerms() };
            }
        } else {
            final String masterTerm = synonymCatalogConnection.getMasterTerm(originalValue);
            final Object lookupResult = masterTerm != null ? masterTerm : (retainOriginalValue ? originalValue : null);
            final Object synonym = masterTerm != null ? originalValue : null;

            return new Object[] { lookupResult, synonym, masterTerm };
        }
    }
}
