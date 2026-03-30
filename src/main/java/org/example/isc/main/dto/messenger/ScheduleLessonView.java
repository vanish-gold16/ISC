package org.example.isc.main.dto.messenger;

public record ScheduleLessonView(Long lessonOrder,
                                 Long daySubjectId,
                                 Long subjectId,
                                 String subjectName,
                                 String shortName,
                                 String lessonTime,
                                 String teacherName,
                                 String room,
                                 String color,
                                 boolean empty
) {
}
