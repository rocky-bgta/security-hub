package com.aspire.asat.universal.news;


import com.aspire.asat.universal.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestNewsRequest {
    private String name;
    private String slug;
    private String categoryId;
    private String content;
    private int sequence;
    private String imageUrl;
    private String videoUrl;
    private LocalDateTime publishedDate;
    private LocalDateTime expireDate;
    private Status status;
}
