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
 * An class for representing the status of a Solaris process, matching the
 * pstatus_t structure.
 *
 * @author Peter Tribble
 */
public class JProcStatus {

    private int pid;
    private long utime;
    private long nutime;
    private long stime;
    private long nstime;
    private long cutime;
    private long ncutime;
    private long cstime;
    private long ncstime;

    /**
     * Populate this object with data. This routine should never be called
     * by clients, and is only for the JNI layer to interface with.
     *
     * @param pid  the process id
     * @param utime  process user cpu time
     * @param nutime  process user cpu time, nanosecond part
     * @param stime  process sys cpu time
     * @param nstime  process sys cpu time, nanosecond part
     * @param cutime  sum of child user time
     * @param ncutime  sum of child user time, nanosecond part
     * @param cstime  sum of child sys time
     * @param ncstime  sum of child sys time, nanosecond part
     */
    public void insert(int pid,
			long utime, long nutime, long stime, long nstime,
			long cutime, long ncutime, long cstime, long ncstime) {
	this.pid = pid;
	this.utime = utime;
	this.nutime = nutime;
	this.stime = stime;
	this.nstime = nstime;
	this.cutime = cutime;
	this.ncutime = ncutime;
	this.cstime = cstime;
	this.ncstime = ncstime;
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
     * Return the execution time of the process. This is measured in seconds
     * and includes user cpu time.
     *
     * @return the user time consumed by this process
     */
    public double getutime() {
	return (double) utime + ((double) nutime)/1000000000.0;
    }

    /**
     * Return the execution time of the process. This is measured in seconds
     * and includes system cpu time.
     *
     * @return the system time consumed by this process
     */
    public double getstime() {
	return (double) stime + ((double) nstime)/1000000000.0;
    }

    /**
     * Return the sum of the execution time of the process children. This is
     * measured in seconds and includes user cpu time.
     *
     * @return the user time of this processes children
     */
    public double getcutime() {
	return (double) cutime + ((double) ncutime)/1000000000.0;
    }

    /**
     * Return the sum of the execution time of the process children. This is
     * measured in seconds and includes system cpu time.
     *
     * @return the system time of this processes children
     */
    public double getcstime() {
	return (double) cstime + ((double) ncstime)/1000000000.0;
    }

    /**
     * Generate a JSON representation of this {@code JProcStatus}.
     *
     * @return A String containing a JSON representation of this
     * {@code JProcStatus}.
     */
    public String toJSON() {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"pid\":").append(pid).append(",");
	sb.append("\"utime\":").append(utime).append(",");
	sb.append("\"nutime\":").append(nutime).append(",");
	sb.append("\"stime\":").append(stime).append(",");
	sb.append("\"nstime\":").append(nstime).append(",");
	sb.append("\"cutime\":").append(cutime).append(",");
	sb.append("\"ncutime\":").append(ncutime).append(",");
	sb.append("\"cstime\":").append(cstime).append(",");
	sb.append("\"ncstime\":").append(ncstime);
	sb.append("}");
	return sb.toString();
    }
}
