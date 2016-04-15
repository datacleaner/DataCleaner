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
package org.datacleaner.monitor.server.components;

import org.datacleaner.descriptors.ComponentDescriptor;
import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.restclient.ProcessStatelessInput;

public class InputRewriterController {

    private InputRewriter[] inputRewriters = new InputRewriter[]{
            new InputColumnAndMappedPropertyRewriter()
    };

    /**
     * Enrich the input data in case the client uses simplified input format
     */
    public void rewriteStatelessInput(ComponentDescriptor<?> compDesc, ProcessStatelessInput processStatelessInput) {
        if(compDesc instanceof TransformerDescriptor) {
            rewriteStatelessInputForTransformer((TransformerDescriptor<?>)compDesc, processStatelessInput);
        }
    }

    private void rewriteStatelessInputForTransformer(TransformerDescriptor<?> compDesc, ProcessStatelessInput input) {
        for(InputRewriter rewriter: inputRewriters) {
            if(rewriter.rewriteInput(compDesc, input)) {
                return;
            }
        }
    }
}
