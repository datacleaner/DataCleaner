package org.datacleaner.components.remote;

import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.descriptors.RemoteConfiguredPropertyDescriptorImpl;

/**
 * @Author jakub
 * @Since 9/1/15
 */
public class RemoteTransformer implements Transformer {

    @Override
    public OutputColumns getOutputColumns() {
        return new OutputColumns(new String[] {"TEST"}, new Class[] {String.class});
    }

    @Override
    public Object[] transform(InputRow inputRow) {
        return new Object[] {"AHOJ"};
    }

    public void setPropertyValue(RemoteConfiguredPropertyDescriptorImpl remoteConfiguredPropertyDescriptor, Object value) {
        // TODO
    }

    public Object getProperty(RemoteConfiguredPropertyDescriptorImpl remoteConfiguredPropertyDescriptor) {
        // TODO
        return null;
    }
}
