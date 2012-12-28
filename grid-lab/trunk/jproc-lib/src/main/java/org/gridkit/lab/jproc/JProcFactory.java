/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at usr/src/OPENSOLARIS.LICENSE
 * or http://www.opensolaris.org/os/licensing.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at usr/src/OPENSOLARIS.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

package org.gridkit.lab.jproc;

import java.io.IOException;

import uk.co.petertribble.jproc.api.JProcStub;
import uk.co.petertribble.jproc.api.NativeJProc;
import uk.co.petertribble.jproc.api.ProcessInterface;

public class JProcFactory {
    private static final String JPROC_LIB_DIR = "jproc-0.12";
    
    public static final boolean IS_NATIVE_OS;
    
    static {
        String osName = System.getProperty("os.name");
        
        if (osName != null) {
            osName = osName.toLowerCase();
            IS_NATIVE_OS = osName.contains("solaris") || osName.contains("sunos");
        } else {
            IS_NATIVE_OS = false;
        }
    }
    
    public static ProcessInterface newJProc() {
        if (IS_NATIVE_OS) {
            return Loader.newJProc();
        } else {
            return new JProcStub();
        }
    }
    
    private static class Loader {
        static {
            LibraryExtractor libExtractor = new LibraryExtractor(JPROC_LIB_DIR);
            
            String libFile = "libproc-" + System.getProperty("os.arch") + ".so";
            
            try {
                libExtractor.extractFile(libFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            System.load(libExtractor.getFilePath(libFile));
            NativeJProc.cacheids();
        }
        
        public static ProcessInterface newJProc() {
            return new NativeJProc();
        }
    }
}
