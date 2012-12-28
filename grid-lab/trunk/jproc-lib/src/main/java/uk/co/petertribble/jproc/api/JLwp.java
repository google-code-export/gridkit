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
 * A class for representing a Solaris lwp.
 *
 * @author Peter Tribble
 */
public class JLwp {

    private int pid;
    private int lwpid;

    /**
     * Create a new JLwp object, representing a Solaris lwp.
     *
     * @param pid  The process id of the containing process.
     * @param lwpid  The lwpid of the lwp.
     */
    public JLwp(int pid, int lwpid) {
	this.pid = pid;
	this.lwpid = lwpid;
    }

    /**
     * Return the pid of this process.
     *
     * @return the pid of the process containing this JLwp.
     */
    public int getPid() {
	return pid;
    }

    /**
     * Return the lwpid of this lwp.
     *
     * @return the lwpid of this JLwp.
     */
    public int getLWPid() {
	return lwpid;
    }

    /**
     * Generate a JSON representation of this {@code JLwp}.
     *
     * @return A String containing a JSON representation of this
     * {@code JLwp}.
     */
    public String toJSON() {
	StringBuilder sb = new StringBuilder();
	sb.append("{");
	sb.append("\"pid\":").append(pid).append(",");
	sb.append("\"lwpid\":").append(lwpid);
	sb.append("}");
	return sb.toString();
    }

    /**
     * Returns whether the requested Object is equal to this JLwp.
     * Equality implies that the Object is of class JLwp and has
     * the same pid and lwpid.
     *
     * @param o The object to be tested for equality.
     *
     * @return true if the object is a {@code JLwp} with the same pid
     * and lwpid as this {@code JLwp}.
     */
    @Override
    public boolean equals(Object o) {
	if (o instanceof JLwp) {
	    JLwp jlwp = (JLwp) o;
	    return (pid == jlwp.getPid()) && (lwpid == jlwp.getLWPid());
        }
        return false;
    }
}
