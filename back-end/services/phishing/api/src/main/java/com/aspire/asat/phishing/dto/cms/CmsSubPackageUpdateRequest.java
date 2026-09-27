package com.aspire.asat.phishing.dto.cms;

import com.aspire.asat.phishing.dto.enums.SubPackageAssignedFor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsSubPackageUpdateRequest {

    private String name;
    private String description;
    private List<String> topicId;
    private SubPackageAssignedFor assignedFor;
    private String channel;
}
