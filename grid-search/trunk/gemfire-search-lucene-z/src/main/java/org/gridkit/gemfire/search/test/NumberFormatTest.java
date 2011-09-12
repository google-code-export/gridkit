package org.gridkit.gemfire.search.test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class NumberFormatTest {
    public static void main(String[] args) throws ParseException {
        Locale[] locales = NumberFormat.getAvailableLocales();
        double myNumber = 289108435.5;

        NumberFormat form;
        for (int j = 0; j < 4; ++j) {
            System.out.println("FORMAT");
            for (int i = 0; i < locales.length; ++i) {
                if (locales[i].getCountry().length() == 0) {
                    continue; // Skip language-only locales
                }
                System.out.print(locales[i].getDisplayName());
                switch (j) {
                    case 0:
                        form = NumberFormat.getInstance(locales[i]);
                        break;
                    case 1:
                        form = NumberFormat.getIntegerInstance(locales[i]);
                        break;
                    case 2:
                        form = NumberFormat.getCurrencyInstance(locales[i]);
                        break;
                    default:
                        form = NumberFormat.getPercentInstance(locales[i]);
                        break;
                }
                if (form instanceof DecimalFormat) {
                    System.out.print(": " + ((DecimalFormat) form).toPattern());
                }
                System.out.print(" -> " + form.format(myNumber));
                try {
                    System.out.println(" -> " + form.parse(form.format(myNumber)).doubleValue());
                } catch (ParseException e) {
                }
            }
        }

        DecimalFormat df = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.GERMAN));

        //289.108.435,00
        //65.000.000,00

        System.out.println(df.format(289108435.53));

        System.out.println(df.parse(df.format(289108435.5)).doubleValue());
        System.out.println(df.parse("289.108.435,5").doubleValue());

        //System.out.println("289.108.435,00").doubleValue());

        //System.out.println(df.format(65000000));
    }
}
