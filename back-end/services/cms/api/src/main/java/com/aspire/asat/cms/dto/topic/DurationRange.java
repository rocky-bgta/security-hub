package com.aspire.asat.cms.dto.topic;

import lombok.Getter;

@Getter
public enum DurationRange {

    ONE_TO_THREE("1-3 min", 1, 3),
    THREE_TO_FIVE("3-5 min", 3, 5),
    FIVE_TO_TEN("5-10 min", 5, 10),
    TEN_TO_FIFTEEN("10-15 min", 10, 15),
    FIFTEEN_TO_TWENTY("15-20 min", 15, 20),
    TWENTY_PLUS("20+ min", 20, Integer.MAX_VALUE);

    private final String label;
    private final int minMinutes;
    private final int maxMinutes;

    DurationRange(String label, int minMinutes, int maxMinutes) {
        this.label = label;
        this.minMinutes = minMinutes;
        this.maxMinutes = maxMinutes;
    }

    public static DurationRange fromMinutes(int minutes) {
        for (DurationRange range : values()) {
            if (minutes >= range.minMinutes && minutes <= range.maxMinutes) {
                return range;
            }
        }
        return TWENTY_PLUS;
    }
}
