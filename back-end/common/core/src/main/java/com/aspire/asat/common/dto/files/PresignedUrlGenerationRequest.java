package com.aspire.asat.common.dto.files;


import com.aspire.asat.common.enums.FileType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class PresignedUrlGenerationRequest {
    @NotBlank
    private String filename;
    //private FileType fileType;
    private FileType fileType;
}
