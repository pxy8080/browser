package de.baumann.browser.util;

import java.util.UUID;

/**
 * Created by xm on 15/4/23.
 */
public class UUIDUtil {

    public static String genUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static Integer getNumUUID() {
        Integer uuid = UUID.randomUUID().toString().replaceAll("-", "").hashCode();
        uuid = uuid < 0 ? -uuid : uuid;//String.hashCode() 值会为空
        return uuid;
    }
}
