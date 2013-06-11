/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.tools.visualvm.core.datasource.DataSource;
import com.sun.tools.visualvm.core.datasource.Storage;
import com.sun.tools.visualvm.core.datasupport.Stateful;

/**
 *
 * @author ragoale
 */
public class SshHost extends DataSource implements Stateful {
    
    private final String hostName;
    private InetAddress inetAddress;
    private String user;
    private int state = STATE_AVAILABLE;

    private Storage givenStorage;
    

    /**
     * Creates new instance of Host defined by hostName.
     * 
     * @param hostName name or IP of the host.
     * @throws java.net.UnknownHostException if host cannot be resolved using provided hostName/IP.
     */
    public SshHost(String user, String hostName, Storage storage) throws UnknownHostException {
        this(user, hostName, InetAddress.getByName(hostName), storage);
    }

    /**
     * Creates new instance of Host defined by hostName and InetAddress instance for the host.
     * 
     * @param hostName name or IP of the host,
     * @param inetAddress InetAddress instance for the host.
     */
    public SshHost(String user, String hostName, InetAddress inetAddress, Storage storage) {
        if (hostName == null) throw new IllegalArgumentException("Host name cannot be null");   // NOI18N
        if (inetAddress == null) throw new IllegalArgumentException("InetAddress cannot be null");  // NOI18N
        
        this.user = user;
        this.hostName = hostName;
        this.inetAddress = inetAddress;
        this.givenStorage = storage;
    }
    
    
    public String getUser() {
    	return user;
    }
    
    /**
     * Returns hostname of the host.
     * 
     * @return hostname of the host.
     */
    public String getHostName() {
        return hostName;
    }
    
    /**
     * Returns InetAddress instance of the host.
     * 
     * @return InetAddress instance of the host.
     */
    public final InetAddress getInetAddress() {
        return inetAddress;
    }
    
    public boolean supportsUserRemove() {
        return true;
    }
    
    protected Storage createStorage() {
        return givenStorage;
    }
    
    public synchronized int getState() {
        return state;
    }
    
    protected final synchronized void setState(int newState) {
        int oldState = state;
        state = newState;
        getChangeSupport().firePropertyChange(PROPERTY_STATE, oldState, newState);
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((inetAddress == null) ? 0 : inetAddress.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SshHost other = (SshHost) obj;
		if (inetAddress == null) {
			if (other.inetAddress != null)
				return false;
		} else if (!inetAddress.equals(other.inetAddress))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public String toString() {
        return user + "@" + getHostName() + " [IP: " + getInetAddress().getHostAddress() + "]";  // NOI18N
    }
}