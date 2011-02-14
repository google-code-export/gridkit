package com.medx.dictionary.accumulator;

import org.apteka.insurance.attribute.AttrKey;

public final class AccumulatorFrame {
	public static final AttrKey<java.util.Collection<com.medx.model.accumulator.Accumulator>> accumulators = new AttrKey<java.util.Collection<com.medx.model.accumulator.Accumulator>>(1, java.util.Collection.class, "");
	public static final AttrKey<java.util.List<com.medx.model.accumulator.AccumulationStage>> stages = new AttrKey<java.util.List<com.medx.model.accumulator.AccumulationStage>>(2, java.util.List.class, "");
	public static final class Id {
		public static final int accumulators = 1;
		public static final int stages = 2;
	}
	public static final class Text {
		public static final String accumulators = "accumulator.AccumulatorFrame.accumulators";
		public static final String stages = "accumulator.AccumulatorFrame.stages";
	}
}
