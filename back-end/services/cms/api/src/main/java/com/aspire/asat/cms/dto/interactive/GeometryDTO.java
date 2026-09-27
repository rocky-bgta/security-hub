package com.aspire.asat.cms.dto.interactive;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeometryDTO {
    private BoundsDTO bounds;
    private Double x;
    private Double y;
    private Double w;
    private Double h;
    /** Polygon vertices as [x, y] pairs (Annotorious / hotspot POLYGON selectors). */
    private List<List<Double>> points;
}
