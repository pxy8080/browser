package de.baumann.browser.util;

import java.text.DecimalFormat;

public class CalculateUtil {
    public static int getPercent(Long a, Long b) {
        Double res = (double) a / b;
        DecimalFormat df = new java.text.DecimalFormat("#.00");
        df.format(res);

        return (int) (res*100);
    }

}
