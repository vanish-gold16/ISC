package org.example.isc.main.secured.scholarhub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.example.isc.main.dto.messenger.ScheduleDayView;
import org.example.isc.main.dto.messenger.ScheduleLessonView;
import org.example.isc.main.dto.messenger.ScheduleView;
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
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.example.isc.main.secured.repositories.scholarhub.SchedulesRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.example.isc.main.secured.repositories.scholarhub.TeachersRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class HubService {

    private record PreparedDayUpdate(Day day, List<DaySubject> lessons) {}

    private static final int DEFAULT_LESSON_SLOTS = 8;

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchedulesRepository schedulesRepository;
    private final SubjectsRepository subjectsRepository;
    private final TeachersRepository teachersRepository;
    private final GradeRepository gradeRepository;
    private final HomeworkRepository homeworkRepository;

    public HubService(UserRepository userRepository, SchedulesRepository schedulesRepository, SubjectsRepository subjectsRepository, TeachersRepository teachersRepository, GradeRepository gradeRepository, HomeworkRepository homeworkRepository) {
        this.userRepository = userRepository;
        this.schedulesRepository = schedulesRepository;
        this.subjectsRepository = subjectsRepository;
        this.teachersRepository = teachersRepository;
        this.gradeRepository = gradeRepository;
        this.homeworkRepository = homeworkRepository;
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

        Map<DayOfWeek, Day> existingDaysByWeek = new HashMap<>();
        List<Day> existingDays = schedule.getDays() == null ? List.of() : new ArrayList<>(schedule.getDays());
        for (Day existingDay : existingDays) {
            if (existingDay.getDayOfWeek() != null) {
                existingDaysByWeek.put(existingDay.getDayOfWeek(), existingDay);
            }
        }

        List<PreparedDayUpdate> preparedDays = new ArrayList<>();
        Set<Long> keptPersistedLessonIds = new HashSet<>();
        Set<Long> lessonIdsToClearDependents = new HashSet<>();
        Set<Long> existingPersistedLessonIds = collectPersistedLessonIds(existingDays);
        List<NewDayForm> dayForms = scheduleForm.getDays() == null ? List.of() : scheduleForm.getDays();
        for(NewDayForm dayForm : dayForms){
            if(dayForm.getDayOfWeek() == null) continue;

            Day day = existingDaysByWeek.remove(dayForm.getDayOfWeek());
            if(day == null){
                day = new Day();
                day.setSchedule(schedule);
            }
            day.setDayOfWeek(dayForm.getDayOfWeek());

            Map<Long, Deque<DaySubject>> existingLessonsByOrder = new HashMap<>();
            Deque<DaySubject> unorderedExistingLessons = new ArrayDeque<>();
            List<DaySubject> currentLessons = day.getLessons() == null ? List.of() : new ArrayList<>(day.getLessons());
            for (DaySubject existingLesson : currentLessons) {
                if (existingLesson.getLessonOrder() == null) {
                    unorderedExistingLessons.addLast(existingLesson);
                    continue;
                }
                existingLessonsByOrder
                        .computeIfAbsent(existingLesson.getLessonOrder(), ignored -> new ArrayDeque<>())
                        .addLast(existingLesson);
            }
            List<DaySubject> lessons = new ArrayList<>();
            if(dayForm.getLessons() != null){
                for (NewLessonRequest lessonForm : dayForm.getLessons()){
                    String subjectName = normalize(lessonForm.getSubjectName());
                    if(subjectName == null) continue;

                    Subject subject = resolveSubject(me, lessonForm, subjectName);
                    Long lessonOrder = lessonForm.getLessonOrder() != null ? lessonForm.getLessonOrder().longValue() : null;
                    DaySubject lesson = takeExistingLesson(existingLessonsByOrder, unorderedExistingLessons, lessonOrder);
                    if (lesson == null) {
                        lesson = new DaySubject();
                    } else {
                        Long existingSubjectId = lesson.getSubject() != null ? lesson.getSubject().getId() : null;
                        if (lesson.getId() != null && !Objects.equals(existingSubjectId, subject.getId())) {
                            lessonIdsToClearDependents.add(lesson.getId());
                        }
                    }

                    lesson.setDay(day);
                    lesson.setSubject(subject);
                    lesson.setLessonOrder(lessonOrder);
                    lesson.setRoom(normalize(lessonForm.getRoom()));

                    lessons.add(lesson);
                    if (lesson.getId() != null) {
                        keptPersistedLessonIds.add(lesson.getId());
                    }
                }
            }

            preparedDays.add(new PreparedDayUpdate(day, lessons));
        }

        Set<Long> removedPersistedLessonIds = new HashSet<>(existingPersistedLessonIds);
        removedPersistedLessonIds.removeAll(keptPersistedLessonIds);
        lessonIdsToClearDependents.addAll(removedPersistedLessonIds);

        if (!lessonIdsToClearDependents.isEmpty()) {
            gradeRepository.deleteAllByAssignedDaySubjectIdIn(lessonIdsToClearDependents);
            gradeRepository.flush();
            homeworkRepository.deleteAllByDueDaySubjectIdIn(lessonIdsToClearDependents);
            homeworkRepository.flush();
        }

        List<Day> newDays = new ArrayList<>();
        for (PreparedDayUpdate preparedDay : preparedDays) {
            preparedDay.day().setLessons(preparedDay.lessons());
            newDays.add(preparedDay.day());
        }

        schedule.setDays(newDays);
        return schedulesRepository.save(schedule);
    }

    private DaySubject takeExistingLesson(
            Map<Long, Deque<DaySubject>> existingLessonsByOrder,
            Deque<DaySubject> unorderedExistingLessons,
            Long lessonOrder
    ) {
        if (lessonOrder == null) {
            return unorderedExistingLessons.pollFirst();
        }

        Deque<DaySubject> matchingLessons = existingLessonsByOrder.get(lessonOrder);
        if (matchingLessons == null) {
            return null;
        }

        DaySubject lesson = matchingLessons.pollFirst();
        if (matchingLessons.isEmpty()) {
            existingLessonsByOrder.remove(lessonOrder);
        }
        return lesson;
    }

    private Set<Long> collectPersistedLessonIds(List<Day> days) {
        Set<Long> lessonIds = new HashSet<>();
        for (Day day : days) {
            List<DaySubject> lessons = day.getLessons() == null ? List.of() : day.getLessons();
            for (DaySubject lesson : lessons) {
                if (lesson.getId() != null) {
                    lessonIds.add(lesson.getId());
                }
            }
        }
        return lessonIds;
    }

    @Transactional
    public ScheduleView getScheduleForHub(Authentication authentication, int previewLimitPerDay){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Schedule schedule = schedulesRepository.findByUser(me);
        if(schedule == null) return null;

        List<Day> scheduleDays = schedule.getDays() == null ? List.of() : schedule.getDays();

        List<ScheduleDayView> days = scheduleDays.stream()
                .filter(day -> day.getDayOfWeek() != null)
                .sorted(Comparator.comparing(day -> day.getDayOfWeek().getValue()))
                .map(day -> {
                    List<DaySubject> dayLessons = day.getLessons() == null ? List.of() : day.getLessons();

                    List<ScheduleLessonView> orderedLessons = dayLessons.stream()
                            .filter(lesson -> lesson.getSubject() != null)
                            .sorted(Comparator.comparing(DaySubject::getLessonOrder, Comparator.nullsLast(Long::compareTo)))
                            .map(this::toScheduleLessonView)
                            .toList();

                    List<ScheduleLessonView> visibleLessons = buildVisibleSlots(orderedLessons, previewLimitPerDay);
                    long visibleRealLessons = visibleLessons.stream().filter(lesson -> !lesson.empty()).count();

                    return new ScheduleDayView(
                        day.getDayOfWeek().name(),
                        toLabel(day.getDayOfWeek()),
                        toShortLabel(day.getDayOfWeek()),
                        orderedLessons.size(),
                        Math.max(orderedLessons.size() - (int) visibleRealLessons, 0),
                        visibleLessons
                    );
                }).toList();

        int minSlotCount = previewLimitPerDay > 0 ? previewLimitPerDay : DEFAULT_LESSON_SLOTS;
        int maxLessonSlots = Math.max(
                days.stream()
                        .mapToInt(day -> day.lessons().size())
                        .max()
                        .orElse(0),
                minSlotCount
        );

        List<ScheduleDayView> paddedDays = days.stream()
                .map(day -> new ScheduleDayView(
                        day.key(),
                        day.label(),
                        day.shortLabel(),
                        day.lessonCount(),
                        day.hiddenLessonCount(),
                        padLessons(day.lessons(), maxLessonSlots)
                ))
                .toList();

        int totalLessons = paddedDays.stream().mapToInt(ScheduleDayView::lessonCount).sum();
        return new ScheduleView(totalLessons, maxLessonSlots, paddedDays);
    }

    private ScheduleLessonView toScheduleLessonView(DaySubject lesson) {
        Subject subject = lesson.getSubject();
        String teacherName = subject.getTeachers() == null || subject.getTeachers().isEmpty()
                ? null
                : subject.getTeachers().get(0).getFullName();

        return new ScheduleLessonView(
                lesson.getLessonOrder(),
                lesson.getId(),
                subject.getId(),
                subject.getFullName(),
                subject.getShortName(),
                teacherName,
                lesson.getRoom() != null ? lesson.getRoom() : subject.getRoom(),
                subject.getColor(),
                false
        );
    }

    private List<ScheduleLessonView> buildVisibleSlots(List<ScheduleLessonView> lessons, int previewLimitPerDay) {
        if (lessons.isEmpty()) {
            return List.of();
        }

        Map<Long, ScheduleLessonView> lessonsByOrder = lessons.stream()
                .filter(lesson -> lesson.lessonOrder() != null)
                .collect(Collectors.toMap(ScheduleLessonView::lessonOrder, lesson -> lesson, (left, right) -> left));

        long maxOrder = lessons.stream()
                .map(ScheduleLessonView::lessonOrder)
                .filter(order -> order != null)
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        if (previewLimitPerDay > 0) {
            maxOrder = Math.min(maxOrder, previewLimitPerDay);
        }

        List<ScheduleLessonView> visibleSlots = new ArrayList<>();
        for (long order = 1; order <= maxOrder; order++) {
            ScheduleLessonView lesson = lessonsByOrder.get(order);
            if (lesson != null) {
                visibleSlots.add(lesson);
            } else {
                visibleSlots.add(new ScheduleLessonView(order, null, null, null, null, null, null, null, true));
            }
        }

        return visibleSlots;
    }

    private List<ScheduleLessonView> padLessons(List<ScheduleLessonView> lessons, int maxLessonSlots) {
        List<ScheduleLessonView> padded = new ArrayList<>(lessons);
        for (int index = padded.size() + 1; index <= maxLessonSlots; index++) {
            padded.add(new ScheduleLessonView((long) index, null, null, null, null, null, null, null, true));
        }
        return padded;
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
        return normalized != null && pattern.matcher(normalized).matches() ? normalized : null;
    }

    private String buildShortName(String fullName){
        String trimmed = fullName.trim();
        String[] words = trimmed.split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank() && Character.isLetterOrDigit(word.charAt(0))) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        if (!initials.isEmpty()) {
            return initials.length() <= 12 ? initials.toString() : initials.substring(0, 12);
        }

        return trimmed.length() <= 12 ? trimmed : trimmed.substring(0, 12);
    }

    private String resolveShortName(NewLessonRequest lessonForm, String subjectName) {
        String shortName = normalize(lessonForm.getShortName());
        return shortName != null ? shortName : buildShortName(subjectName);
    }

    private Subject resolveSubject(User me, NewLessonRequest lessonForm, String subjectName) {
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
        return subjectsRepository.save(subject);
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

        List<Teacher> teachers = new ArrayList<>();
        teachers.add(teacher);
        return teachers;
    }

    private String toShortLabel(DayOfWeek day) {
        String shorten = "";
        switch(day){
            case MONDAY:
                shorten = "Mon";
                break;
            case TUESDAY:
                shorten = "Tue";
                break;
            case WEDNESDAY:
                shorten = "Wed";
                break;
            case THURSDAY:
                shorten = "Thu";
                break;
            case FRIDAY:
                shorten = "Fri";
                break;
            case SATURDAY:
                shorten = "Sat";
                break;
            case SUNDAY:
                shorten = "Sun";
                break;
        }

        return shorten;
    }

    private String toLabel(DayOfWeek day) {
        String label = "";
        switch (day){
            case MONDAY:
                label = "Monday";
                break;
            case TUESDAY:
                label = "Tuesday";
                break;
            case WEDNESDAY:
                label = "Wednesday";
                break;
            case THURSDAY:
                label = "Thursday";
                break;
            case FRIDAY:
                label = "Friday";
                break;
            case SATURDAY:
                label = "Saturday";
                break;
            case SUNDAY:
                label = "Sunday";
                break;
        }
        return label;
    }

}
