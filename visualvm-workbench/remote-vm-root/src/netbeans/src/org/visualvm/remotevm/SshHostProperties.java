/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.visualvm.remotevm;

import com.sun.tools.visualvm.core.properties.PropertiesCustomizer;

/**
 *
 * @author ragoale
 */
public class SshHostProperties {

    private final String hostName;
    private final String displayName;
    private final PropertiesCustomizer customizer;


    public SshHostProperties(String hostName, String displayName,
                          PropertiesCustomizer customizer) {
        this.customizer = customizer;
        this.hostName = hostName;
        this.displayName = displayName;
    }


    public String getHostName() { return hostName; }

    public String getDisplayName() { return displayName; }

    public PropertiesCustomizer getPropertiesCustomizer() { return customizer; }

}
