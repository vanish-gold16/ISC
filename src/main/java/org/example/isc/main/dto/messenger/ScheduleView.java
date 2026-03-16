package org.example.isc.main.dto.messenger;

import java.util.List;

public record ScheduleView(int totalLessons,
                           List<ScheduleDayView> days) {
}
