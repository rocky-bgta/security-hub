package com.aspire.asat.universal.policy;

import com.aspire.asat.universal.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileAttachment {
    private String fileUrl;
    private FileType fileType;
}

