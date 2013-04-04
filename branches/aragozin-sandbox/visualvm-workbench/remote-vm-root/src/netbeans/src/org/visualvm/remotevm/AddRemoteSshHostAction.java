/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author ragoale
 */
public class AddRemoteSshHostAction extends SingleDataSourceAction<RemoteSshHostsContainer> {
    
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
