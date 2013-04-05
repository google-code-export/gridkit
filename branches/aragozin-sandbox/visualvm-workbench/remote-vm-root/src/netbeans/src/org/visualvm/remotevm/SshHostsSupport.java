/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import java.io.File;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import com.sun.tools.visualvm.core.datasupport.Utils;

/**
 *
 * @author ragoale
 */
public final class SshHostsSupport {
    
    private static final Object hostsStorageDirectoryLock = new Object();
    // @GuardedBy hostsStorageDirectoryLock
    private static File hostsStorageDirectory;
    
    private static SshHostsSupport instance;

    private final SshHostProvider hostProvider = new SshHostProvider();


    /**
     * Returns singleton instance of HostsSupport.
     * 
     * @return singleton instance of HostsSupport.
     */
    public static synchronized SshHostsSupport getInstance() {
        if (instance == null) instance = new SshHostsSupport();
        return instance;
    }
    

    /**
     * Creates new host from provided hostname. Displays a popup dialog if wrong
     * hostname is provided or the host has already been defined.
     * 
     * @param hostname hostname of the host to be created.
     * @return new host from provided hostname or null if the hostname could not be resolved.
     */
    public SshHost createHost(String hostname) {
        return createHost(new SshHostProperties(hostname, hostname, null), true, true);
    }
    
    /**
     * Creates new host from provided hostname and display name. Displays a popup
     * dialog if wrong hostname is provided or the host has already been defined.
     * 
     * @param hostname hostname of the host to be created.
     * @param displayname displayname of the host to be created.
     * @return new host from provided hostname or null if the hostname could not be resolved.
     */
    public SshHost createHost(String hostname, String displayname) {
        return createHost(new SshHostProperties(hostname, displayname, null), true, true);
    }

    /**
     * Returns an existing Host instance or creates a new Host if needed.
     *
     * @param hostname hostname of the host to be created
     * @param interactive true if any failure should be visually presented to the user, false otherwise.
     * @return an existing or a newly created Host
     *
     * @since VisualVM 1.1.1
     */
    public SshHost getOrCreateHost(String hostname, boolean interactive) {
        return createHost(new SshHostProperties(hostname, hostname, null), false, interactive);
    }

    SshHost createHost(SshHostProperties properties, boolean createOnly, boolean interactive) {
        return hostProvider.createHost(properties, createOnly, interactive);
    }
        
    /**
     * Returns storage directory for defined hosts.
     * 
     * @return storage directory for defined hosts.
     */
    public static File getStorageDirectory() {
        synchronized(hostsStorageDirectoryLock) {
            if (hostsStorageDirectory == null) {
                String snapshotsStorageString = SshHostsSupportImpl.getStorageDirectoryString();
                hostsStorageDirectory = new File(snapshotsStorageString);
                if (hostsStorageDirectory.exists() && hostsStorageDirectory.isFile())
                    throw new IllegalStateException("Cannot create hosts storage directory " + snapshotsStorageString + ", file in the way");   // NOI18N
                if (hostsStorageDirectory.exists() && (!hostsStorageDirectory.canRead() || !hostsStorageDirectory.canWrite()))
                    throw new IllegalStateException("Cannot access hosts storage directory " + snapshotsStorageString + ", read&write permission required");    // NOI18N
                if (!Utils.prepareDirectory(hostsStorageDirectory))
                    throw new IllegalStateException("Cannot create hosts storage directory " + snapshotsStorageString); // NOI18N
            }
            return hostsStorageDirectory;
        }
    }
    
    /**
     * Returns true if the storage directory for defined hosts already exists, false otherwise.
     * 
     * @return true if the storage directory for defined hosts already exists, false otherwise.
     */
    public static boolean storageDirectoryExists() {
        return new File(SshHostsSupportImpl.getStorageDirectoryString()).isDirectory();
    }
    
    
    private SshHostsSupport() {
        DataSourceDescriptorFactory.getDefault().registerProvider(new SshHostDescriptorProvider());
        
        RemoteSshHostsContainer container = RemoteSshHostsContainer.sharedInstance();
        DataSource.ROOT.getRepository().addDataSource(container);
        
        hostProvider.initialize();
    }

}