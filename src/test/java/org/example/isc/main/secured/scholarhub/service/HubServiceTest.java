package org.example.isc.main.secured.scholarhub.service;

import org.example.isc.main.secured.models.scholarship.Day;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.example.isc.main.secured.repositories.scholarhub.SchedulesRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.example.isc.main.secured.repositories.scholarhub.TeachersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.lang.reflect.Proxy;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HubServiceTest {

    @Test
    void setupClearsDependentRowsForRemovedLegacyLessonWithoutOrder() {
        User user = new User();
        user.setUsername("alice");

        DaySubject lesson = new DaySubject();
        lesson.setId(38L);

        Day day = new Day();
        day.setDayOfWeek(DayOfWeek.MONDAY);
        day.setLessons(List.of(lesson));
        lesson.setDay(day);

        Schedule schedule = new Schedule();
        schedule.setUser(user);
        schedule.setDays(List.of(day));
        day.setSchedule(schedule);

        Set<Long> deletedHomeworkLessonIds = new HashSet<>();
        Set<Long> deletedGradeLessonIds = new HashSet<>();

        UserRepository userRepository = proxy(UserRepository.class, Map.of(
                "findByUsernameIgnoreCase", args -> Optional.of(user)
        ));
        SchedulesRepository schedulesRepository = proxy(SchedulesRepository.class, Map.of(
                "findByUser", args -> schedule,
                "save", args -> args[0]
        ));
        SubjectsRepository subjectsRepository = proxy(SubjectsRepository.class, Map.of());
        TeachersRepository teachersRepository = proxy(TeachersRepository.class, Map.of());
        GradeRepository gradeRepository = proxy(GradeRepository.class, Map.of(
                "deleteAllByAssignedDaySubjectIdIn", args -> {
                    deletedGradeLessonIds.addAll(castIds(args[0]));
                    return null;
                },
                "flush", args -> null
        ));
        HomeworkRepository homeworkRepository = proxy(HomeworkRepository.class, Map.of(
                "deleteAllByDueDaySubjectIdIn", args -> {
                    deletedHomeworkLessonIds.addAll(castIds(args[0]));
                    return null;
                },
                "flush", args -> null
        ));

        HubService hubService = new HubService(
                userRepository,
                schedulesRepository,
                subjectsRepository,
                teachersRepository,
                gradeRepository,
                homeworkRepository,
                null
        );

        Schedule updatedSchedule = hubService.setup(
                "[{\"dayOfWeek\":\"MONDAY\",\"lessons\":[]}]",
                new UsernamePasswordAuthenticationToken("alice", "pw")
        );

        assertEquals(Set.of(38L), deletedHomeworkLessonIds);
        assertEquals(Set.of(38L), deletedGradeLessonIds);
        assertTrue(updatedSchedule.getDays().getFirst().getLessons().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Map<String, InvocationHandler> handlers) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> type.getSimpleName() + "Proxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == args[0];
                            default -> null;
                        };
                    }

                    InvocationHandler handler = handlers.get(method.getName());
                    if (handler != null) {
                        return handler.invoke(args == null ? new Object[0] : args);
                    }
                    return defaultValue(method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return 0;
    }

    private static List<Long> castIds(Object argument) {
        Collection<?> values = (Collection<?>) argument;
        List<Long> ids = new ArrayList<>();
        for (Object value : values) {
            ids.add((Long) value);
        }
        return ids;
    }

    @FunctionalInterface
    private interface InvocationHandler {
        Object invoke(Object[] args);
    }
}
