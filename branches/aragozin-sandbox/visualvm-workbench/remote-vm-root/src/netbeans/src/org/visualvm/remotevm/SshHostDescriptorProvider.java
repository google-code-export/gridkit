/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;
import com.sun.tools.visualvm.host.RemoteHostDescriptor;

/**
 *
 * @author ragoale
 */
public class SshHostDescriptorProvider extends AbstractModelProvider<DataSourceDescriptor,DataSource> {
    
    public DataSourceDescriptor createModelFor(DataSource ds) {
        if (ds instanceof SshHost) {
            SshHost host = (SshHost) ds;
            return new RemoteHostDescriptor(host);
        }
        return null;
    }
}
