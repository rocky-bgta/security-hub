package com.aspire.asat.common.service.files;



import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileValidationService {
    private final FileProps props;

    public String resolveContentType(PresignedUrlGenerationRequest req) {
        String ext = extension(req.getFilename());
        if (ext.isEmpty() || props.getFiles().getAllowedExtensions() == null ||
                props.getFiles().getAllowedExtensions().stream().noneMatch(e -> e.equalsIgnoreCase(ext))) {
            throw new ValidationException("Extension not allowed: " + (ext.isEmpty() ? "<none>" : "." + ext));
        }
        if (props.getFiles().getContentTypeMap() != null) {
            String ct = props.getFiles().getContentTypeMap().get(ext.toLowerCase());
            if (ct != null && !ct.isBlank()) return ct;
        }
        return props.getFiles().getDefaultContentType();
    }

    private static String extension(String fn) {
        int i = fn == null ? -1 : fn.lastIndexOf('.');
        return (i >= 0 && i < fn.length()-1) ? fn.substring(i+1) : "";
    }
}
