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
 * An class for representing the status of an lwp in a Solaris process,
 * matching the lwpstatus_t structure.
 *
 * @author Peter Tribble
 */
public class JProcLwpStatus {

    private int pid;
    private int lwpid;
    private long utime;
    private long nutime;
    private long stime;
    private long nstime;

    /**
     * Populate this object with data. This routine should never be called
     * by clients, and is only for the JNI layer to interface with.
     *
     * @param pid  the process id
     * @param lwpid  the lwp id
     * @param utime  process user cpu time
     * @param nutime  process user cpu time, nanosecond part
     * @param stime  process sys cpu time
     * @param nstime  process sys cpu time, nanosecond part
     */
    public void insert(int pid, int lwpid,
			long utime, long nutime, long stime, long nstime) {
	this.pid = pid;
	this.lwpid = lwpid;
	this.utime = utime;
	this.nutime = nutime;
	this.stime = stime;
	this.nstime = nstime;
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
     * Return the execution time of this lwp. This is measured in seconds
     * and includes user cpu time.
     *
     * @return the user time of this lwp
     */
    public double getutime() {
	return (double) utime + ((double) nutime)/1000000000.0;
    }

    /**
     * Return the execution time of this lwp. This is measured in seconds
     * and includes system cpu time.
     *
     * @return the system time of this lwp
     */
    public double getstime() {
	return (double) stime + ((double) nstime)/1000000000.0;
    }

    /**
     * Generate a JSON representation of this {@code JProcLwpStatus}.
     *
     * @return A String containing a JSON representation of this
     * {@code JProcLwpStatus}.
     */
    public String toJSON() {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"pid\":").append(pid).append(",");
	sb.append("\"lwpid\":").append(lwpid).append(",");
	sb.append("\"utime\":").append(utime).append(",");
	sb.append("\"nutime\":").append(nutime).append(",");
	sb.append("\"stime\":").append(stime).append(",");
	sb.append("\"nstime\":").append(nstime);
	sb.append("}");
	return sb.toString();
    }
}
