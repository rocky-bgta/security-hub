package com.aspire.asat.common.util;


import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.util.UUID;

@UtilityClass
public class KeyStrategy {
    public static String buildKey(String fileType, String id, String filename, boolean fileRename) {

        String fileNewName;
        if(fileRename){
            String ext = "";
            int i = filename == null ? -1 : filename.lastIndexOf('.');
            if (i >= 0 && i < filename.length()-1) ext = filename.substring(i+1);
            String uuid = UUID.randomUUID().toString();
            fileNewName = uuid + (ext.isEmpty() ? "" : "." + ext);
        }else{
            fileNewName = filename;
        }

        var d = LocalDate.now();
        return String.format("%s/%d/%02d/%02d/%s/%s",
                fileType,
                d.getYear(),
                d.getMonthValue(),
                d.getDayOfMonth(),
                id,
                fileNewName
        );
    }

    public static String buildKey(String fileType,  String filename) {
        String uuid = UUID.randomUUID().toString();
        return buildKey(fileType, uuid,filename, true);
    }
}
