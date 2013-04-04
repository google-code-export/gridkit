/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.model.AbstractModelProvider;
import com.sun.tools.visualvm.host.RemoteHostsContainer;
import com.sun.tools.visualvm.host.RemoteHostsContainerDescriptor;

/**
 *
 * @author ragoale
 */
public class RemoteSshHostsContainer extends DataSource {

    private static RemoteSshHostsContainer sharedInstance;

    /**
     * Returns singleton instance of RemoteHostsContainer.
     *
     * @return singleton instance of RemoteHostsContainer.
     */
    public static synchronized RemoteSshHostsContainer sharedInstance() {
        if (sharedInstance == null) sharedInstance = new RemoteSshHostsContainer();
        return sharedInstance;
    }

    
    private RemoteSshHostsContainer() {
        DataSourceDescriptorFactory.getDefault().registerProvider(
            new AbstractModelProvider<DataSourceDescriptor,DataSource>() {
                public DataSourceDescriptor createModelFor(DataSource ds) {
                    if (RemoteSshHostsContainer.sharedInstance().equals(ds))
                        return new RemoteSshHostsContainerDescriptor();
                    else 
                        return null;
                }
            }
        );
    }
}
