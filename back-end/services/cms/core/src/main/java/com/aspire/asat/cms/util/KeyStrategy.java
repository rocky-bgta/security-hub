package com.aspire.asat.cms.util;


import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.UUID;

@UtilityClass
public class KeyStrategy {
    public static String buildKey(String fileType, String filename) {
        String ext = "";
        int i = filename == null ? -1 : filename.lastIndexOf('.');
        if (i >= 0 && i < filename.length()-1) ext = filename.substring(i+1);

        var d = LocalDate.now();
        String uuid = UUID.randomUUID().toString();
        String prefix = fileType+"/uploads";

        return String.format("%s/%d/%02d/%02d/%s%s%s",
                prefix, d.getYear(), d.getMonthValue(), d.getDayOfMonth(),
                uuid, ext.isEmpty() ? "" : ".", ext);
    }
}
