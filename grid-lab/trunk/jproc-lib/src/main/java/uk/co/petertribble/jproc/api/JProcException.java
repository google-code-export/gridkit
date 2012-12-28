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
 * A JProc exception, so we can identify whether it's one of ours or thrown by
 * java itself.
 *
 * @author Peter Tribble
 */
@SuppressWarnings("serial")
public class JProcException extends RuntimeException {

    /**
     * Create a new JProcException, with no message text.
     */
    public JProcException() {
	super();
    }

    /**
     * Create a new JProcException, with the given detail message.
     *
     * @param s  the detail message
     */
    public JProcException(String s) {
	super(s);
    }
    /**
     * Construct a new JProcException with the givendetail message and cause.
     *
     * @param s  the detail message
     * @param cause  the underlying cause for this JProcException
     */
    public JProcException(String s, Throwable cause) {
	super(s, cause);
    }
}
