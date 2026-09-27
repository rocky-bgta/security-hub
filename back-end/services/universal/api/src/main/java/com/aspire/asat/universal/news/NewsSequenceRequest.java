package com.aspire.asat.universal.news;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsSequenceRequest {
    private String id;
    private int sequence;
}
