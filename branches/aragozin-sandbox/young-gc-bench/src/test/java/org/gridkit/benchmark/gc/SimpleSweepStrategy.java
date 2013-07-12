package org.gridkit.benchmark.gc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gridkit.lab.data.Sample;
import org.gridkit.lab.data.SampleList;

public class SimpleSweepStrategy implements SweepStrategy {

	private List<Sample> backlog = new ArrayList<Sample>();
	private List<Sample> pending = new ArrayList<Sample>();

	public SimpleSweepStrategy(SampleList list) {
		backlog.addAll(list.asList());
		Collections.shuffle(backlog);
	}
	
	@Override
	public synchronized Sample nextDataPoint() {
		if (backlog.isEmpty()) {
			return null;
		}
		else {
			Sample s = backlog.remove(backlog.size() - 1);
			pending.add(s);
			System.out.println("Next data point: " + s + " (" + backlog.size() + " remaining)");
			return s;
		}
	}

	@Override
	public synchronized void notifyResult(Sample sample) {
		Sample ss = sample.retainFields(sample.coordinateKeys());
		if (!pending.remove(ss)) {
			backlog.remove(ss);
		}
	}
}
