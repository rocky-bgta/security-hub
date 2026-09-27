package com.aspire.asat.cms.dto.content.storyblock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopUpDto {
    private String popUpTitle;
    private String popUpDescription;
    private boolean isEnabled;  // Flag to enable/disable the pop-up
}
