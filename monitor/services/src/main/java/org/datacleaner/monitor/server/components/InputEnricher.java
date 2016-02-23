package org.datacleaner.monitor.server.components;

import org.datacleaner.descriptors.TransformerDescriptor;
import org.datacleaner.restclient.ProcessStatelessInput;

public interface InputEnricher {
    boolean enrichStatelessInputForTransformer(TransformerDescriptor transformer, ProcessStatelessInput input);
}
