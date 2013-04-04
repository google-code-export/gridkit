/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import com.sun.tools.visualvm.host.Host;
import java.awt.Image;
import java.util.Comparator;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author ragoale
 */
public class RemoteSshHostDescriptor extends DataSourceDescriptor<SshHost> {

    private static final Image NODE_ICON = ImageUtilities.loadImage(
            "com/sun/tools/visualvm/host/resources/remoteHost.png", true);   // NOI18N

    /**
     * Creates new instance of RemoteHostDescriptor for a given host.
     * 
     * @param host Host for which to create the descriptor.
     */
    public RemoteSshHostDescriptor(SshHost host) {
        super(host, resolveName(host, host.getHostName()), NbBundle.getMessage(
              RemoteSshHostDescriptor.class, "DESCR_Remote"), NODE_ICON, // NOI18N
              resolvePosition(host, POSITION_AT_THE_END, true), EXPAND_ON_FIRST_CHILD);

    }

    /**
     * Sets a custom comparator for sorting DataSources within a Host.
     * Use setChildrenComparator(null) to restore the default sorting.
     *
     * @param newComparator comparator for sorting DataSources within a Host
     *
     * @since VisualVM 1.3
     */
    public void setChildrenComparator(Comparator<DataSource> newComparator) {
        super.setChildrenComparator(newComparator);
    }

    public boolean supportsRename() {
        return true;
    }

    public boolean providesProperties() {
        return true;
    }
    
}
