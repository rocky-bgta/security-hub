package com.aspire.asat.billing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {

    @Id
    private String id;

    private String name;  // Action name (e.g., "Payment in review", "Approved for processing")
}

