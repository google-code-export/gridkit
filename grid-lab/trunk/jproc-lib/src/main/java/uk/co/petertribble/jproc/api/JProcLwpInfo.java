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

/**
 * An class for representing information on an lwp in a Solaris process,
 * matching the lwpsinfo_t structure.
 *
 * @author Peter Tribble
 */
public class JProcLwpInfo {

    private int pid;
    private int lwpid;
    private long stime;
    private long etime;
    private long ntime;

    /**
     * Populate this object with data. This routine should never be called
     * by clients, and is only for the JNI layer to interface with.
     *
     * @param pid  the process id
     * @param lwpid  the lwp id
     * @param stime  start time
     * @param etime  execution time
     * @param ntime  execution time, nanosecond part
     */
    public void insert(int pid, int lwpid, long stime, long etime, long ntime) {
	this.pid = pid;
	this.lwpid = lwpid;
	this.stime = stime;
	this.etime = etime;
	this.ntime = ntime;
    }

    /**
     * Return the process id.
     *
     * @return the process id
     */
    public int getpid() {
	return pid;
    }

    /**
     * Return the lwp id.
     *
     * @return the lwp id
     */
    public int getlwpid() {
	return lwpid;
    }

    /**
     * Return the start time of the lwp. This is measured in seconds
     * since the epoch.
     *
     * @return the start time of the lwp
     */
    public long getstime() {
	return stime;
    }

    /**
     * Return the execution time of the process. This is measured in seconds
     * and includes usr+sys cpu time.
     *
     * @return the execution time of the lwp
     */
    public double gettime() {
	return (double) etime + ((double) ntime)/1000000000.0;
    }

    /**
     * Generate a JSON representation of this {@code JProcLwpInfo}.
     *
     * @return A String containing a JSON representation of this
     * {@code JProcLwpInfo}.
     */
    public String toJSON() {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"pid\":").append(pid).append(",");
	sb.append("\"lwpid\":").append(lwpid).append(",");
	sb.append("\"stime\":").append(stime).append(",");
	sb.append("\"etime\":").append(etime).append(",");
	sb.append("\"ntime\":").append(ntime);
	sb.append("}");
	return sb.toString();
    }
}
