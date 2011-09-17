package org.gridkit.search.gemfire.benchmark.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AmountXmlAdapter extends XmlAdapter<String, Double> {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.GERMAN));

    @Override
    public Double unmarshal(String v) throws Exception {
        return decimalFormat.parse(v.trim()).doubleValue();
    }

    @Override
    public String marshal(Double v) throws Exception {
        return decimalFormat.format(v);
    }
}
