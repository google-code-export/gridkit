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

import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * An access class for Solaris /proc. Allows details on individual processes
 * to be queried.
 *
 * @author Peter Tribble
 */
public class NativeJProc extends ProcessInterface {

    private File fproc;

    /**
     * Creates a new NativeJProc object.
     */
    public NativeJProc() {
	super();
	fproc = new File("/proc");
    }

    /**
     * Return a Set of all processes in the system.
     *
     * @return A Set of all the processes running on the system.
     */
    public Set <JProcess> getProcesses() {
	Set <JProcess> pset = new HashSet <JProcess> ();
	for (String s : fproc.list()) {
	    int pid = Integer.parseInt(s);
	    JProcInfo jpi = getInfo(pid);
	    // If getInfo is null then the process has exited, so ignore.
	    if (jpi != null) {
		pset.add(new JProcess(pid, jpi));
	    }
	}
	return pset;
    }

    /**
     * Return a Set of JLwp objects representing the lwps in this process.
     * If the process no longer exists, returns null
     *
     * @param pid the pid of the process to query
     *
     * @return A Set of JLwp objects representing the lwps in this process.
     */
    public Set <JLwp> getLwps(int pid) {
	File pf = new File("/proc/" + Integer.toString(pid) + "/lwp");
	String[] lwpids = pf.list();
	if (lwpids == null) {
	    return null;
	}
	Set <JLwp> lset = new HashSet <JLwp> ();
	for (String s : lwpids) {
	    lset.add(new JLwp(pid, Integer.parseInt(s)));
	}
	return lset;
    }

    /**
     * Retrieves status of a process.
     *
     * @param pid The process pid to query
     *
     * @return A new JProcStatus object populated with current data, or null
     * if the process does not exist
     */
    public native JProcStatus getStatus(int pid);

    /**
     * Retrieves status of an lwp in a process.
     *
     * @param pid The process pid to query
     * @param lwpid The id of the lwp to query
     *
     * @return A new JProcLwpStatus object populated with current data, or null
     * if the process or lwp does not exist
     */
    public native JProcLwpStatus getLwpStatus(int pid, int lwpid);

    /**
     * Retrieves information about a process.
     *
     * @param pid The process pid to query
     *
     * @return A new JProcInfo object populated with current data, or null
     * if the process does not exist
     */
    public native JProcInfo getInfo(int pid);

    /**
     * Retrieves information about an lwp in a process.
     *
     * @param pid The process pid to query
     * @param lwpid The id of the lwp to query
     *
     * @return A new JProcLwpInfo object populated with current data, or null
     * if the process or lwp does not exist
     */
    public native JProcLwpInfo getLwpInfo(int pid, int lwpid);

    /**
     * Retrieves usage information about a process.
     *
     * @param pid The process pid to query
     *
     * @return A new JProcUsage object populated with current data, or null
     * if the process does not exist
     */
    public native JProcUsage getUsage(int pid);

    /**
     * Retrieves usage information about an lwp in a process.
     *
     * @param pid The process pid to query
     * @param lwpid The id of the lwp to query
     *
     * @return A new JProcUsage object populated with current data, or null
     * if the process or lwp does not exist
     */
    public native JProcUsage getLwpUsage(int pid, int lwpid);

    /**
     * Retrieves the user name corresponding to a given numeric uid.
     *
     * @param uid The numeric userid.
     *
     * @return The user name, or null if no user matches.
     */
    public native String getUserName(int uid);

    /**
     * Retrieves the user id corresponding to a given username.
     *
     * @param username The username.
     *
     * @return The userid, or -1 if no user matches.
     */
    public native int getUserId(String username);

    /**
     * Retrieves the group name corresponding to a given numeric gid.
     *
     * @param gid The numeric groupid.
     *
     * @return The group name, or null if no group matches.
     */
    public native String getGroupName(int gid);

    /**
     * Retrieves the group id corresponding to a given group name.
     *
     * @param group The group name.
     *
     * @return The groupid, or -1 if no group matches.
     */
    public native int getGroupId(String group);

    /**
     * Retrieves the project name corresponding to a given numeric project id.
     *
     * @param projid The numeric project id.
     *
     * @return The project name, or null if no project matches.
     */
    public native String getProjectName(int projid);

    /**
     * Retrieves the project id corresponding to a given project name.
     *
     * @param project The project name.
     *
     * @return The project id, or -1 if no project matches.
     */
    public native int getProjectId(String project);

    /**
     * Retrieves the zone name corresponding to a given numeric zone id.
     *
     * @param zoneid The numeric zone id.
     *
     * @return The zone name, or null if no zone matches.
     */
    public native String getZoneName(int zoneid);

    /**
     * Retrieves the zone id corresponding to a given zone name.
     *
     * @param zone The zone name.
     *
     * @return The zone id, or -1 if no zone matches.
     */
    public native int getZoneId(String zone);

    /*
     * Caches all the methodids once, for efficiency and guaranteed code
     * coverage.
     */
    public native static void cacheids();
}
