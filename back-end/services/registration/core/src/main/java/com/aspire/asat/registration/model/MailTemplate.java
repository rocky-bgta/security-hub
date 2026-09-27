package com.aspire.asat.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "mail_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailTemplate {

    private String id;
    private String name;
    private String type;
    private String status;
    private String subject;

    private String sender;
    private List<String> receiverGroups;

    private String bodyHtml;
    private String bodyText;

    private Instant createdAt;
    private Instant updatedAt;
}
