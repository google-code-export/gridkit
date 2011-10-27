package org.gridkit.generators;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Formatter;

/**
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
public class FormatterGenerator implements DeterministicObjectGenerator<String> {

	private NumberFormat format;

	public FormatterGenerator() {
		this(DecimalFormat.getNumberInstance());
	}
	
	public FormatterGenerator(String format) {
		this.format = new DecimalFormat(format);
	}

	public FormatterGenerator(NumberFormat format) {
		this.format = (NumberFormat) format.clone();
	}
	
	@Override
	public String object(long id) {
		return format.format(id);
	}

	@Override
	public FormatterGenerator  clone() {
		return new FormatterGenerator(format);
	}		
}
