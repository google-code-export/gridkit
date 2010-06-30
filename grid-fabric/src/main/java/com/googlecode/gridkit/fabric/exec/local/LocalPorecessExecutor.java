package com.googlecode.gridkit.fabric.exec.local;

import java.io.IOException;

import com.googlecode.gridkit.fabric.exec.ExecCommand;
import com.googlecode.gridkit.fabric.exec.ProcessExecutor;

public class LocalPorecessExecutor implements ProcessExecutor {

	@Override
	public Process execute(ExecCommand command) throws IOException {
		// TODO work dir handling
		return Runtime.getRuntime().exec(command.getCommand());
	}
}
