/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import java.awt.Image;
import java.util.Comparator;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author ragoale
 */
public final class RemoteSshHostsContainerDescriptor extends DataSourceDescriptor<RemoteSshHostsContainer> {

    private static final Image NODE_ICON = ImageUtilities.loadImage(
                "com/sun/tools/visualvm/host/resources/remoteHosts.png", true);  // NOI18N

    RemoteSshHostsContainerDescriptor() {
        super(RemoteSshHostsContainer.sharedInstance(), NbBundle.getMessage(
              RemoteSshHostsContainerDescriptor.class, "LBL_Remote"), null, NODE_ICON, 10, // NOI18N
              EXPAND_ON_EACH_NEW_CHILD);
        
        // Initialize sorting
        // setChildrenComparator(HostsSorting.instance().getInitialSorting());
    }

    /**
     * Sets a custom comparator for sorting DataSources within the RemoteHostsContainer.
     * Use setChildrenComparator(null) to restore the default sorting.
     *
     * @param newComparator comparator for sorting DataSources within the RemoteHostsContainer
     */
    public void setChildrenComparator(Comparator<DataSource> newComparator) {
        super.setChildrenComparator(newComparator);
    }

}
