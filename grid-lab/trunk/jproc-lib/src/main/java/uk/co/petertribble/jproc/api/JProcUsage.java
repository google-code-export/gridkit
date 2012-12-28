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
 * An class for representing usage of a Solaris process, matching the
 * prusage_t structure.
 *
 * @author Peter Tribble
 */
public class JProcUsage {

    private int lwpid;
    private int count;
    private long rtime;
    private long nrtime;
    private long utime;
    private long nutime;
    private long stime;
    private long nstime;
    private long minf;
    private long majf;
    private long nswap;
    private long inblk;
    private long oublk;
    private long msnd;
    private long mrcv;
    private long sigs;
    private long vctx;
    private long ictx;
    private long sysc;
    private long ioch;

    /**
     * Populate this object with data. This routine should never be called
     * by clients, and is only for the JNI layer to interface with.
     *
     * @param lwpid   lwp id.  0: process or defunc
     * @param count   number of contributing lwp
     * @param rtime   total lwp real (elapsed) time - seconds
     * @param nrtime  total lwp real (elapsed) time - nanoseconds
     * @param utime   user level cpu time - seconds
     * @param nutime  user level cpu time - nanoseconds
     * @param stime   system call cpu time - seconds
     * @param nstime  system call cpu time - nanoseconds
     * @param minf    minor page faults
     * @param majf    major page faults
     * @param nswap   swaps
     * @param inblk   input blocks
     * @param oublk   output blocks
     * @param msnd    messages sent
     * @param mrcv    messages received
     * @param sigs    signals received
     * @param vctx    voluntary context switches
     * @param ictx    involuntary context switches
     * @param sysc    system calls
     * @param ioch    chars read and written
     */
    public void insert(int lwpid, int count, long rtime, long nrtime,
			long utime, long nutime, long stime,
			long nstime,
			long minf, long majf, long nswap, long inblk,
			long oublk, long msnd, long mrcv, long sigs,
			long vctx, long ictx, long sysc, long ioch) {
	this.lwpid = lwpid;
	this.count = count;
	this.rtime = rtime;
	this.nrtime = nrtime;
	this.utime = utime;
	this.nutime = nutime;
	this.stime = stime;
	this.nstime = nstime;
	this.minf = minf;
	this.majf = majf;
	this.nswap = nswap;
	this.inblk = inblk;
	this.oublk = oublk;
	this.msnd = msnd;
	this.mrcv = mrcv;
	this.sigs = sigs;
	this.vctx = vctx;
	this.ictx = ictx;
	this.sysc = sysc;
	this.ioch = ioch;
    }

    /**
     * Return the number of contributing lwp.
     *
     * @return the elapsed time of this process or lwp
     */
    public int getcount() {
	return count;
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
     * Return the total elapsed time of this process or lwp.
     *
     * @return the elapsed time of this process or lwp
     */
    public double getrtime() {
	return (double) rtime + ((double) nrtime)/1000000000.0;
    }

    /**
     * Return the total user time of this process or lwp.
     *
     * @return the user time of this process or lwp
     */
    public double getutime() {
	return (double) utime + ((double) nutime)/1000000000.0;
    }

    /**
     * Return the total system time of this process or lwp.
     *
     * @return the system time of this process or lwp
     */
    public double getstime() {
	return (double) stime + ((double) nstime)/1000000000.0;
    }

    /**
     * Return the number of minor faults incurred by this process or lwp.
     *
     * @return the number of minor faults incurred by this process or lwp
     */
    public long getminf() {
	return minf;
    }

    /**
     * Return the number of major faults incurred by this process or lwp.
     *
     * @return the number of major faults incurred by this process or lwp
     */
    public long getmajf() {
	return majf;
    }

    /**
     * Return the number of system calls incurred by this process or lwp.
     *
     * @return the number of system calls incurred by this process or lwp
     */
    public long getsysc() {
	return sysc;
    }

    /**
     * Return the number of swaps incurred by this process or lwp.
     *
     * @return the number of swaps incurred by this process or lwp
     */
    public long getnswap() {
	return nswap;
    }

    /**
     * Return the number of input blocks incurred by this process or lwp.
     *
     * @return the number of input blocks incurred by this process or lwp
     */
    public long getinblk() {
	return inblk;
    }

    /**
     * Return the number of output blocks incurred by this process or lwp.
     *
     * @return the number of output blocks incurred by this process or lwp
     */
    public long getoublk() {
	return oublk;
    }

    /**
     * Return the number of messages sent by this process or lwp.
     *
     * @return the number of messages sent by this process or lwp
     */
    public long getmsnd() {
	return msnd;
    }

    /**
     * Return the number of messages received by this process or lwp.
     *
     * @return the number of messages received by this process or lwp
     */
    public long getmrcv() {
	return mrcv;
    }

    /**
     * Return the number of signals received by this process or lwp.
     *
     * @return the number of signals received by this process or lwp
     */
    public long getsigs() {
	return sigs;
    }

    /**
     * Return the number of voluntary context switches incurred by this
     * process or lwp.
     *
     * @return the number of voluntary context switches incurred by this
     * process or lwp
     */
    public long getvctx() {
	return vctx;
    }

    /**
     * Return the number of involuntary context switches incurred by this
     * process or lwp.
     *
     * @return the number of involuntary context switches incurred by this
     * process or lwp
     */
    public long getictx() {
	return ictx;
    }

    /**
     * Return the number of chars read and written by this
     * process or lwp.
     *
     * @return the number of chars read and written by this
     * process or lwp
     */
    public long getioch() {
	return ioch;
    }

    /**
     * Generate a JSON representation of this {@code JProcUsage}.
     *
     * @return A String containing a JSON representation of this
     * {@code JProcUsage}.
     */
    public String toJSON() {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"lwpid\":").append(lwpid).append(",");
	sb.append("\"count\":").append(count).append(",");
	sb.append("\"rtime\":").append(rtime).append(",");
	sb.append("\"nrtime\":").append(nrtime).append(",");
	sb.append("\"utime\":").append(utime).append(",");
	sb.append("\"nutime\":").append(nutime).append(",");
	sb.append("\"stime\":").append(stime).append(",");
	sb.append("\"nstime\":").append(nstime).append(",");
	sb.append("\"minf\":").append(minf).append(",");
	sb.append("\"majf\":").append(majf).append(",");
	sb.append("\"nswap\":").append(nswap).append(",");
	sb.append("\"inblk\":").append(inblk).append(",");
	sb.append("\"oublk\":").append(oublk).append(",");
	sb.append("\"msnd\":").append(msnd).append(",");
	sb.append("\"mrcv\":").append(mrcv).append(",");
	sb.append("\"sigs\":").append(sigs).append(",");
	sb.append("\"vctx\":").append(vctx).append(",");
	sb.append("\"ictx\":").append(ictx).append(",");
	sb.append("\"sysc\":").append(sysc).append(",");
	sb.append("\"ioch\":").append(ioch);
	sb.append("}");
	return sb.toString();
    }
}
