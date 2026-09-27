package com.aspire.asat.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Minimal mapping of registration end_user_packages for course-assignment checks.
 */
@Document(collection = "end_user_packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndUserPackage {

    @Id
    private String id;

    private String userId;

    private boolean active;
}
