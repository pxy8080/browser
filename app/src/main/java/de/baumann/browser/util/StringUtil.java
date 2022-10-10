package de.baumann.browser.util;

public class StringUtil {
    /**
     * 截取字符串前面的正整数，如"22天"得"22","18个人"得到"18".
     *
     * @return
     */
    public static String getQuantity(String regular) {
        int index = 0;
        for (int i = 0; i < regular.length(); i++) {
            char c = regular.charAt(i);
            if (Character.isDigit(c)) {
                if (i == regular.length() - 1) {
                    index = i + 1;
                } else {
                    index = i;
                }
                continue;
            } else {
                index = i;
                break;
            }
        }
        return regular.substring(0, index);
    }
}
