package org.example.isc.main.dto.messenger;

import java.util.List;

public record ScheduleView(int totalLessons,
                           int maxLessonSlots,
                           List<ScheduleDayView> days) {
}
