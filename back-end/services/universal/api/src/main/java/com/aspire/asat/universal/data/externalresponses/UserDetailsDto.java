package com.aspire.asat.universal.data.externalresponses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for end user details fetched from the registration service.
 * Contains essential user information for notification purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsDto {
    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String clientAdminId;
    
    /**
     * Get the display name for the user.
     * Prefers fullName, falls back to firstName + lastName, or email.
     */
    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        if (firstName != null || lastName != null) {
            StringBuilder name = new StringBuilder();
            if (firstName != null) name.append(firstName);
            if (lastName != null) {
                if (!name.isEmpty()) name.append(" ");
                name.append(lastName);
            }
            if (!name.isEmpty()) return name.toString();
        }
        return email != null ? email : "User";
    }
}
