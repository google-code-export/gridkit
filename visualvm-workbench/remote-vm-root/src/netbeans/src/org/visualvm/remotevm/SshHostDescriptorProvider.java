/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;

/**
 *
 * @author ragoale
 */
@SuppressWarnings("rawtypes")
public class SshHostDescriptorProvider extends AbstractModelProvider<DataSourceDescriptor,DataSource> {
    
    public DataSourceDescriptor createModelFor(DataSource ds) {
        if (ds instanceof SshHost) {
            SshHost host = (SshHost) ds;
            return new RemoteSshHostDescriptor(host);
        }
        return null;
    }
}
