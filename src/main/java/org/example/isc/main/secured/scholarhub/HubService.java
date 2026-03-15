package org.example.isc.main.secured.scholarhub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.isc.main.dto.scholarship.NewDayForm;
import org.example.isc.main.dto.scholarship.NewLessonRequest;
import org.example.isc.main.dto.scholarship.NewScheduleForm;
import org.example.isc.main.secured.models.scholarship.Day;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.scholarship.Teacher;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.SchedulesRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.example.isc.main.secured.repositories.scholarhub.TeachersRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HubService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchedulesRepository schedulesRepository;
    private final SubjectsRepository subjectsRepository;
    private final TeachersRepository teachersRepository;

    public HubService(UserRepository userRepository, SchedulesRepository schedulesRepository, SubjectsRepository subjectsRepository, TeachersRepository teachersRepository) {
        this.userRepository = userRepository;
        this.schedulesRepository = schedulesRepository;
        this.subjectsRepository = subjectsRepository;
        this.teachersRepository = teachersRepository;
    }

    @Transactional
    public Schedule setup(
            String schedulePayload,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        NewScheduleForm scheduleForm = parsePayload(schedulePayload);

        Schedule schedule = schedulesRepository.findByUser(me);
        if(schedule == null){
            schedule = new Schedule();
            schedule.setUser(me);
        }

        List<Day> newDays = new ArrayList<>();
        for(NewDayForm dayForm : scheduleForm.getDays()){
            if(dayForm.getDayOfWeek() == null) continue;

            Day day = new Day();
            day.setSchedule(schedule);
            day.setDayOfWeek(dayForm.getDayOfWeek());

            List<DaySubject> lessons = new ArrayList<>();
            if(dayForm.getLessons() != null){
                for (NewLessonRequest lessonForm : dayForm.getLessons()){
                    String subjectName = normalize(lessonForm.getSubjectName());
                    if(subjectName == null) continue;

                    Subject subject = subjectsRepository.findByUserAndResolvedNameIgnoreCase(me, subjectName)
                            .orElseGet(() -> {
                                Subject newSubject = new Subject();
                                newSubject.setUser(me);
                                newSubject.setFullName(subjectName);
                                newSubject.setShortName(resolveShortName(lessonForm, subjectName));
                                newSubject.setRoom(normalize(lessonForm.getRoom()));
                                newSubject.setTeachers(resolveTeachers(lessonForm));
                                return subjectsRepository.save(newSubject);
                            });

                    if (normalize(lessonForm.getShortName()) != null) subject.setShortName(resolveShortName(lessonForm, subjectName));
                    if (normalize(lessonForm.getRoom()) != null) subject.setRoom(normalize(lessonForm.getRoom()));
                    if(normalizeColor(lessonForm.getColor()) != null) subject.setColor(normalizeColor(lessonForm.getColor()));
                    if (normalize(lessonForm.getTeacher()) != null) subject.setTeachers(resolveTeachers(lessonForm));
                    subject = subjectsRepository.save(subject);

                    DaySubject lesson = new DaySubject();
                    lesson.setDay(day);
                    lesson.setSubject(subject);
                    lesson.setLessonOrder(lessonForm.getLessonOrder() != null ? lessonForm.getLessonOrder().longValue() : null);
                    lesson.setRoom(normalize(lessonForm.getRoom()));

                    lessons.add(lesson);
                }
            }
            day.setLessons(lessons);
            newDays.add(day);
        }

        schedule.setDays(newDays);
        return schedulesRepository.save(schedule);
    }

    private NewScheduleForm parsePayload(String schedulePayload){
        try {
            return objectMapper.readValue(
                    "{\"days\":" + schedulePayload + "}",
                    NewScheduleForm.class
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid schedule payload", e);
        }
    }

    private String normalize(String value){
        if(value == null) return null;
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeColor(String color){
        String normalized = normalize(color);
        Pattern pattern = Pattern.compile("^#[0-9A-Fa-f]{6}$");
        return color.matches(String.valueOf(pattern)) ? normalized : null;
    }

    private String buildShortName(String fullName){
        String trimmed = fullName.trim();
        return trimmed.length() <= 12 ? trimmed : trimmed.substring(0, 12);
    }

    private String resolveShortName(NewLessonRequest lessonForm, String subjectName) {
        String shortName = normalize(lessonForm.getShortName());
        return shortName != null ? shortName : buildShortName(subjectName);
    }

    private List<Teacher> resolveTeachers(NewLessonRequest lessonForm) {
        String teacherName = normalize(lessonForm.getTeacher());
        if (teacherName == null) {
            return new ArrayList<>();
        }

        Teacher teacher = teachersRepository.findByFullNameIgnoreCase(teacherName)
                .orElseGet(() -> {
                    Teacher newTeacher = new Teacher();
                    newTeacher.setFullName(teacherName);
                    newTeacher.setSubjects(new ArrayList<>());
                    return teachersRepository.save(newTeacher);
                });

        return List.of(teacher);
    }

}
