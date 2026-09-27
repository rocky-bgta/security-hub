package com.aspire.asat.vps.client.dto;

import com.aspire.asat.vps.client.enums.VpsCommonStatus;
import com.aspire.asat.vps.client.enums.VpsContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpsContentCommonDto {

    private String contentName;

    private String description;

    private VpsContentType contentType;

    private VpsCommonStatus status;

    private String author;

    private List<String> chapterIds;

    private List<String>tags;
}
