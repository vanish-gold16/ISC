package org.example.isc.main.dto.messenger;

public record ScheduleLessonView(Long lessonOrder,
                                 String subjectName,
                                 String shortName,
                                 String teacherName,
                                 String room,
                                 String color,
                                 boolean empty
) {
}
