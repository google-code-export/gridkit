/**
 * Copyright 2012-2014 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptor;
import java.awt.Image;
import java.util.Comparator;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
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
