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
 * A class for representing a Solaris process.
 *
 * @author Peter Tribble
 */
@SuppressWarnings("serial")
public class JProcess implements Serializable {

    private int pid;
    private JProcInfo info;

    /**
     * Create a new JProcess object, representing a Solaris process.
     *
     * @param pid  The process id of the process.
     * @param info A JprocInfo object with basic information about the process.
     */
    public JProcess(int pid, JProcInfo info) {
	this.pid = pid;
	this.info = info;
    }

    /**
     * Return the pid of this process.
     *
     * @return the pid of the process represented by this JProcess.
     */
    public int getPid() {
	return pid;
    }

    /**
     * Update the Information on this process.
     *
     * @param info A new JProcInfo object containing updated information
     * about this process.
     */
    public void updateInfo(JProcInfo info) {
	this.info = info;
    }

    /**
     * Return Information on this process. This uses cached information, to
     * avoid native code lookups, and is designed for filtering purposes where
     * data like ids are reasonably static.
     *
     * @return A JProcInfo object containing information about this process.
     */
    public JProcInfo getCachedInfo() {
	return info;
    }

    /**
     * Returns whether the requested Object is equal to this JProcess. Equality
     * implies that the Object is of class JProcess and has the same pid.
     *
     * @param o The object to be tested for equality.
     *
     * @return true if the object is a {@code JProcess} with the same pid
     * as this {@code JProcess}.
     */
    @Override
    public boolean equals(Object o) {
	if (o instanceof JProcess) {
	    JProcess jp = (JProcess) o;
	    return pid == jp.getPid();
        }
        return false;
    }

    /**
     * Returns a hash code value for this {@code JProcess}.
     *
     * @return A hash code value for this {@code JProcess}.
     */
    @Override
    public int hashCode() {
	return pid;
    }
}
