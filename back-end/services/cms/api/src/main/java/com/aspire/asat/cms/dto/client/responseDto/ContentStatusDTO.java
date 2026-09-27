package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatusDTO {

    @NotBlank
    private String contentId;

    @NotBlank
    private String title;

    private String contentType; // VIDEO, PDF, QUIZ, etc.

    private boolean isDone;
    
    // For INTERACTIVE_VIDEO and INTERACTIVE_CONTENT types
    private List<InteractiveContentListItem> contentList;
    
    // Constructor without contentList for backward compatibility
    public ContentStatusDTO(String contentId, String title, String contentType, boolean isDone) {
        this.contentId = contentId;
        this.title = title;
        this.contentType = contentType;
        this.isDone = isDone;
        this.contentList = null;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InteractiveContentListItem {
        private String id;
        private String contentName;
    }
}
