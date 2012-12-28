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

package uk.co.petertribble.jproc.api;

import java.util.Collections;
import java.util.Set;

public class JProcStub extends ProcessInterface {
    @Override
    public Set<JProcess> getProcesses() {
        return Collections.emptySet();
    }

    @Override
    public Set<JLwp> getLwps(int pid) {
        return Collections.emptySet();
    }

    @Override
    public JProcStatus getStatus(int pid) {
        return null;
    }

    @Override
    public JProcLwpStatus getLwpStatus(int pid, int lwpid) {
        return null;
    }

    @Override
    public JProcInfo getInfo(int pid) {
        return null;
    }

    @Override
    public JProcLwpInfo getLwpInfo(int pid, int lwpid) {
        return null;
    }

    @Override
    public JProcUsage getUsage(int pid) {
        return null;
    }

    @Override
    public JProcUsage getLwpUsage(int pid, int lwpid) {
        return null;
    }

    @Override
    public String getUserName(int uid) {
        return null;
    }

    @Override
    public int getUserId(String username) {
        return -1;
    }

    @Override
    public int getGroupId(String group) {
        return -1;
    }

    @Override
    public String getProjectName(int projid) {
        return null;
    }

    @Override
    public int getProjectId(String project) {
        return -1;
    }

    @Override
    public String getZoneName(int zoneid) {
        return null;
    }

    @Override
    public int getZoneId(String zone) {
        return -1;
    }
}
