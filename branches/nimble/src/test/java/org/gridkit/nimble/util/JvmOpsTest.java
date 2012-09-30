package org.gridkit.nimble.util;

import java.util.Map;

import org.junit.Test;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

@SuppressWarnings("restriction")
public class JvmOpsTest {
    @Test
    public void test() {
        for (Map.Entry<VirtualMachineDescriptor, VirtualMachine> vm : JvmOps.listVms().entrySet()) {
            System.out.println(vm.getKey());
            System.out.println(vm.getValue());
            System.out.println("-----------");
        }
    }
}
