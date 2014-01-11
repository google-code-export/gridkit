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

import com.sun.tools.visualvm.core.properties.PropertiesCustomizer;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
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
