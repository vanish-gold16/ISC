package org.example.isc.main.dto.messenger;

import java.util.List;

public record ScheduleDayView(String key,
                              String label,
                              String shortLabel,
                              int lessonCount,
                              int hiddenLessonCount,
                              List<ScheduleLessonView> lessons
) {
}
