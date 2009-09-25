/*
* Copyright (c) 2006-2009 Grid Dynamics, Inc.
* 2030 Bent Creek Dr., San Ramon, CA 94582
* All Rights Reserved.
*
* This software is the confidential and proprietary information of
* Grid Dynamics, Inc. ("Confidential Information"). You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with Grid Dynamics.
*/
package com.griddynamics.research.coherence.osgi.domain;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * TODO [Need to specify general description of the entity]
 *
 * @author Anton Savelyev
 * @since 1.7
 */
public class Workstation implements Serializable, PortableObject {

    private String proc;

    private int ram;

    private int hardDisk;

    public Workstation() {
    }

    public Workstation(int hardDisk, String proc, int ram) {
        this.hardDisk = hardDisk;
        this.proc = proc;
        this.ram = ram;
    }

    public int getHardDisk() {
        return hardDisk;
    }

    public void setHardDisk(int hardDisk) {
        this.hardDisk = hardDisk;
    }

    public String getProc() {
        return proc;
    }

    public void setProc(String proc) {
        this.proc = proc;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    @Override
    public String toString() {
        return String.format("Computer [proc = %1s, ram = %2d, HDD = %3d]", proc, ram, hardDisk);
    }

    @Override
    public void readExternal(PofReader pofReader) throws IOException {
        setProc(pofReader.readString(0));
        setRam(pofReader.readInt(1));
        setHardDisk(pofReader.readInt(2));
    }

    @Override
    public void writeExternal(PofWriter pofWriter) throws IOException {
        pofWriter.writeString(0, proc);
        pofWriter.writeInt(1, ram);
        pofWriter.writeInt(2, hardDisk);
    }
}
