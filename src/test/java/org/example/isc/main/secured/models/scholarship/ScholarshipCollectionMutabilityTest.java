package org.example.isc.main.secured.models.scholarship;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ScholarshipCollectionMutabilityTest {

    @Test
    void subjectCopiesTeachersIntoMutableList() {
        Subject subject = new Subject();
        Teacher firstTeacher = new Teacher();
        Teacher secondTeacher = new Teacher();

        subject.setTeachers(List.of(firstTeacher));

        assertDoesNotThrow(() -> subject.getTeachers().add(secondTeacher));
    }

    @Test
    void teacherCopiesSubjectsIntoMutableList() {
        Teacher teacher = new Teacher();
        Subject firstSubject = new Subject();
        Subject secondSubject = new Subject();

        teacher.setSubjects(List.of(firstSubject));

        assertDoesNotThrow(() -> teacher.getSubjects().add(secondSubject));
    }
}
