package com.aspire.asat.billing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "next_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextStep {

    @Id
    private String id;

    private String name;  // Next step name (e.g., "Follow up with finance team", "Proceed with payment processing")
}

