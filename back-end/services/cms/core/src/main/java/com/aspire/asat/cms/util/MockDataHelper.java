package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.client.responseDto.ClientCourseResponseDTO;
import com.aspire.asat.cms.dto.client.responseDto.PackageResponseDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MockDataHelper {

    private static final String DEFAULT_URL = "https://strgblobstandardasatv2.blob.core.windows.net/strgblobstandardasatv2/cer_link_default.png";

    public static List<ClientCourseResponseDTO> getMockCourses() {
        List<ClientCourseResponseDTO> list = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            list.add(new ClientCourseResponseDTO(
                    "course-" + String.format("%03d", i),
                    "Course Title " + i,
                    10 + i,
                    i % (10 + i),
                    2,
                    (i % (10 + i)) * 100.0 / (10 + i),
                    (i % 3 == 0) ? "completed" : (i % 2 == 0 ? "in_progress" : "not_started"),
                    false,
                    i % 4 == 0 ? DEFAULT_URL : null,
                    i % 5 == 0,
                    DEFAULT_URL,
                    Instant.now()
            ));
        }
        return list;
    }

    public static List<PackageResponseDTO> getMockPackages() {
        List<PackageResponseDTO> list = new ArrayList<>();
        for (int i = 1; i <= 21; i++) {
            int total = 5 + i;
            int completed = i % (total + 1);
            double progress = completed * 100.0 / total;
            boolean expired = i % 3 == 0;

            list.add(new PackageResponseDTO(
                    "pkg-" + String.format("%03d", i),
                    "Package " + i,
                    total,
                    completed,
                    progress,
                    "365 Days",
                    "2024-01-01",
                    "2025-12-31",
                    expired
            ));
        }
        return list;
    }
}
