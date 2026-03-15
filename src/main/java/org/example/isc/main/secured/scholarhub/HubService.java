package org.example.isc.main.secured.scholarhub;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.example.isc.main.dto.scholarship.NewDayForm;
import org.example.isc.main.dto.scholarship.NewLessonRequest;
import org.example.isc.main.dto.scholarship.NewScheduleForm;
import org.example.isc.main.secured.models.scholarship.Day;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.SchedulesRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class HubService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SchedulesRepository schedulesRepository;
    private final SubjectsRepository subjectsRepository;

    public HubService(UserRepository userRepository, ObjectMapper objectMapper, SchedulesRepository schedulesRepository, SubjectsRepository subjectsRepository) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.schedulesRepository = schedulesRepository;
        this.subjectsRepository = subjectsRepository;
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

                    Subject subject = subjectsRepository.findByUserAndFullNameIgnoreCase(me, subjectName)
                            .orElseGet(() -> {
                                Subject newSubject = new Subject();
                                newSubject.setUser(me);
                                newSubject.setFullName(subjectName);
                                newSubject.setShortName(buildShortName(subjectName));
                                return subjectsRepository.save(newSubject);
                            });

                    DaySubject lesson = new DaySubject();
                    lesson.setDay(day);
                    lesson.setSubject(subject);
                    lesson.setOrder(lessonForm.getLessonOrder() != null ? lessonForm.getLessonOrder().longValue() : null);

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
        return objectMapper.readValue(
                "{\"days\":" + schedulePayload + "}",
                NewScheduleForm.class
        );
    }

    private String normalize(String value){
        if(value == null) return null;
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String buildShortName(String fullName){
        String trimmed = fullName.trim();
        return trimmed.length() <= 10 ? trimmed : trimmed.substring(0, 10);
    }

}
