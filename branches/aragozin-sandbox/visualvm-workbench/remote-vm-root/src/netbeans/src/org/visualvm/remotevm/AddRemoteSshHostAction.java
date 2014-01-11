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

import com.sun.tools.visualvm.core.ui.actions.SingleDataSourceAction;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class AddRemoteSshHostAction extends SingleDataSourceAction<RemoteSshHostsContainer> {
    
	private static final long serialVersionUID = 20140112L;
	
	private static final String ICON_PATH = "com/sun/tools/visualvm/host/resources/addRemoteHost.png";  // NOI18N
    private static final Image ICON =  ImageUtilities.loadImage(ICON_PATH);
    
    private boolean tracksSelection = false;
    
    private static AddRemoteSshHostAction alwaysEnabled;
    private static AddRemoteSshHostAction selectionAware;
    
    
    public static synchronized AddRemoteSshHostAction alwaysEnabled() {
        if (alwaysEnabled == null) {
            alwaysEnabled = new AddRemoteSshHostAction();
            alwaysEnabled.putValue(SMALL_ICON, new ImageIcon(ICON));
            alwaysEnabled.putValue("iconBase", ICON_PATH);  // NOI18N
        }
        return alwaysEnabled;
    }
    
    public static synchronized AddRemoteSshHostAction selectionAware() {
        if (selectionAware == null) {
            selectionAware = new AddRemoteSshHostAction();
            selectionAware.tracksSelection = true;
        }
        return selectionAware;
    }
    
    protected void actionPerformed(RemoteSshHostsContainer remoteHostsContainer, ActionEvent actionEvent) {
        final SshHostProperties hostDescriptor = SshHostCustomizer.defineHost();
        if (hostDescriptor != null) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SshHostsSupport.getInstance().createHost(hostDescriptor, true, true);
                }
            });
        }
    }
    
    protected boolean isEnabled(RemoteSshHostsContainer remoteHostsContainer) {
        return true;
    }
    
    protected void updateState(Set<RemoteSshHostsContainer> remoteHostsContainerSet) {
        if (tracksSelection) super.updateState(remoteHostsContainerSet);
    }
    
    private AddRemoteSshHostAction() {
        super(RemoteSshHostsContainer.class);
        putValue(NAME, NbBundle.getMessage(AddRemoteSshHostAction.class, "LBL_Add_Remote_Host"));  // NOI18N
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(AddRemoteSshHostAction.class, "ToolTip_Add_Remote_Host")); // NOI18N
    }
}
