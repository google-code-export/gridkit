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

import java.io.Serializable;

/**
 * A class for representing information on a Solaris process, matching the
 * psinfo_t structure.
 *
 * @author Peter Tribble
 */
@SuppressWarnings("serial")
public class JProcInfo implements Serializable {

    private int pr_nlwp;
    private int pr_pid;
    private int pr_ppid;
    private int pr_uid;
    private int pr_euid;
    private int pr_gid;
    private int pr_egid;
    private long pr_size;
    private long pr_rssize;
    private long stime;
    private long etime;
    private long ntime;
    private long ectime;
    private long nctime;
    private int pr_taskid;
    private int pr_projid;
    private int pr_zoneid;
    private int pr_contract;
    private String pr_fname;

    /**
     * Populate this object with data. This routine should never be called
     * by clients, and is only for the JNI layer to interface with.
     *
     * FIXME pr_pgid pr_sid
     *
     * @param pr_pid  the process id
     * @param pr_ppid  process id of parent
     * @param pr_uid  real user id
     * @param pr_euid  effective user id
     * @param pr_gid  real group id
     * @param pr_egid  effective group id
     * @param pr_nlwp  number of active lwps in the process
     * @param pr_size  size of process image in Kbytes
     * @param pr_rssize  resident set size in Kbytes
     * @param stime  start time
     * @param etime  execution time
     * @param ntime  execution time, nanosecond part
     * @param ectime  reaped children execution time
     * @param nctime  repaed children execution time, nanosecond part
     * @param pr_taskid  task id
     * @param pr_projid  project id
     * @param pr_zoneid  zone id
     * @param pr_contract  process contract
     * @param pr_fname  name of execed file
     */
    public void insert(int pr_pid, int pr_ppid, int pr_uid, int pr_euid,
			int pr_gid, int pr_egid, int pr_nlwp,
			long pr_size, long pr_rssize, long stime,
			long etime, long ntime, long ectime, long nctime,
			int pr_taskid, int pr_projid, int pr_zoneid,
			int pr_contract, String pr_fname) {
	this.pr_pid = pr_pid;
	this.pr_ppid = pr_ppid;
	this.pr_uid = pr_uid;
	this.pr_euid = pr_euid;
	this.pr_gid = pr_gid;
	this.pr_egid = pr_egid;
	this.pr_nlwp = pr_nlwp;
	this.pr_size = pr_size;
	this.pr_rssize = pr_rssize;
	this.stime = stime;
	this.etime = etime;
	this.ntime = ntime;
	this.ectime = ectime;
	this.nctime = nctime;
	this.pr_taskid = pr_taskid;
	this.pr_projid = pr_projid;
	this.pr_zoneid = pr_zoneid;
	this.pr_contract = pr_contract;
	this.pr_fname = pr_fname;
    }

    /*
     * Accessors.
     */

    /**
     * Return the process id.
     *
     * @return the process id
     */
    public int getpid() {
	return pr_pid;
    }

    /**
     * Return the parent process id.
     *
     * @return the parent process id
     */
    public int getppid() {
	return pr_ppid;
    }

    /**
     * Return the real userid.
     *
     * @return the real userid
     */
    public int getuid() {
	return pr_uid;
    }

    /**
     * Return the effective userid.
     *
     * @return the effective userid
     */
    public int geteuid() {
	return pr_euid;
    }

    /**
     * Return the real group id.
     *
     * @return the real group id
     */
    public int getgid() {
	return pr_gid;
    }

    /**
     * Return the effective group id.
     *
     * @return the effective group id
     */
    public int getegid() {
	return pr_egid;
    }

    /**
     * Return the number of lwps in the process.
     *
     * @return the number of lwps in the process
     */
    public int getnlwp() {
	return pr_nlwp;
    }

    /**
     * Return the process size in Kbytes.
     *
     * @return the process size in Kbytes
     */
    public long getsize() {
	return pr_size;
    }

    /**
     * Return the resident size in Kbytes.
     *
     * @return the resident size in Kbytes
     */
    public long getrssize() {
	return pr_rssize;
    }

    /**
     * Return the start time of the process. This is measured in seconds
     * since the epoch.
     *
     * @return the start time of the process
     */
    public long getstime() {
	return stime;
    }

    /**
     * Return the execution time of the process. This is measured in seconds
     * and includes usr+sys cpu time.
     *
     * @return the execution time of the process
     */
    public double gettime() {
	return (double) etime + ((double) ntime)/1000000000.0;
    }

    /**
     * Return the execution time of reaped children of this process. This is
     * measured in seconds and includes usr+sys cpu time.
     *
     * @return the execution time of reaped children of this process
     */
    public double getctime() {
	return (double) ectime + ((double) nctime)/1000000000.0;
    }

    /**
     * Return the task id of the process.
     *
     * @return the task id of the process
     */
    public int gettaskid() {
	return pr_taskid;
    }

    /**
     * Return the project id of the process.
     *
     * @return the project id of the process
     */
    public int getprojid() {
	return pr_projid;
    }

    /**
     * Return the zone id of the process.
     *
     * @return the zone id of the process
     */
    public int getzoneid() {
	return pr_zoneid;
    }

    /**
     * Return the process contract.
     *
     * @return the process contract
     */
    public int getcontract() {
	return pr_contract;
    }

    /**
     * Return the name of the execed file of this process.
     *
     * @return the name of the execed file
     */
    public String getfname() {
	return pr_fname;
    }

    /**
     * Generate a JSON representation of this {@code JProcInfo}.
     *
     * @return A String containing a JSON representation of this
     * {@code JProcInfo}.
     */
    public String toJSON() {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"fname\":\"").append(pr_fname).append("\",");
	sb.append("\"pid\":").append(pr_pid).append(",");
	sb.append("\"ppid\":").append(pr_ppid).append(",");
	sb.append("\"uid\":").append(pr_uid).append(",");
	sb.append("\"euid\":").append(pr_euid).append(",");
	sb.append("\"gid\":").append(pr_gid).append(",");
	sb.append("\"egid\":").append(pr_egid).append(",");
	sb.append("\"nlwp\":").append(pr_nlwp).append(",");
	sb.append("\"size\":").append(pr_size).append(",");
	sb.append("\"rssize\":").append(pr_rssize).append(",");
	sb.append("\"stime\":").append(stime).append(",");
	sb.append("\"etime\":").append(etime).append(",");
	sb.append("\"ntime\":").append(ntime).append(",");
	sb.append("\"ectime\":").append(ectime).append(",");
	sb.append("\"nctime\":").append(nctime).append(",");
	sb.append("\"taskid\":").append(pr_taskid).append(",");
	sb.append("\"projid\":").append(pr_projid).append(",");
	sb.append("\"zoneid\":").append(pr_zoneid).append(",");
	sb.append("\"contract\":").append(pr_contract);
	sb.append("}");
	return sb.toString();
    }
}
