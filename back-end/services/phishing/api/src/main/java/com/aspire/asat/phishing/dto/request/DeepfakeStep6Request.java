package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.enums.VideoRenderProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeepfakeStep6Request {

    /**
     * Optional {@code provider_credentials} document id. When set, provider name and
     * model are taken from the database and {@link #videoProvider}/{@link #model} are ignored.
     */
    private String providerId;

    /**
     * Required when {@link #providerId} is not provided (legacy clients).
     */
    private VideoRenderProvider videoProvider;

    private String model;
}
