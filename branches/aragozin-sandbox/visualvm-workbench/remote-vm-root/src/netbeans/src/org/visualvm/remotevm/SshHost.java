/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.host.Host;
import java.net.UnknownHostException;

/**
 *
 * @author ragoale
 */
public class SshHost extends Host {
    
    private Storage givenStorage;
    
    
    SshHost(String hostName, Storage givenStorage) throws UnknownHostException {
        super(hostName);
        this.givenStorage = givenStorage;
    }
    
    
    public boolean supportsUserRemove() {
        return true;
    }
    
    protected Storage createStorage() {
        return givenStorage;
    }
}
