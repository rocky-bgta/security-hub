package com.aspire.asat.vps.dto.interactive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentListItem {
    private String id;
    private String contentType;
    private Boolean isDefault;
    private String time;
    private boolean canSkip;
    private ContentBodyDto contentBody;

}
