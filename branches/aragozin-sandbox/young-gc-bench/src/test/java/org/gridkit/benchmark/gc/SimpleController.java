package org.gridkit.benchmark.gc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.gridkit.lab.data.Sample;
import org.gridkit.vicluster.MassExec;

public class SimpleController {

	private SweepStrategy strategy;
	private List<Future<Void>> tasks = new ArrayList<Future<Void>>();
	private Collection<? extends DataPointExecutor> executors;
	private List<Sample> output = Collections.synchronizedList(new ArrayList<Sample>());
	
	public SimpleController(SweepStrategy strategy, Collection<? extends DataPointExecutor> executors) {
		this.strategy = strategy;
		this.executors = executors;
	}

	// to be called once
	public List<Sample> run() throws InterruptedException, ExecutionException {
		for(DataPointExecutor dpe: executors) {
			FutureTask<Void> ft = new FutureTask<Void>(newRunner(dpe), null);
			Thread t = new Thread(ft);
			tasks.add(ft);
			t.setName(dpe.toString());
			t.start();
		}
		
		MassExec.waitAll(tasks);
		
		return new ArrayList<Sample>(output);
	}
	
	private Runnable newRunner(final DataPointExecutor dpe) {
		return new Runnable() {
			@Override
			public void run() {
				while(true) {
					Sample s = strategy.nextDataPoint();
					if (s == null) {
						break;
					}
					Sample r = dpe.process(s.clone()).clone();
					System.out.println("Calculated " + r);
					strategy.notifyResult(r);					
					output.add(r);
				}
			}
		};
	}
}
