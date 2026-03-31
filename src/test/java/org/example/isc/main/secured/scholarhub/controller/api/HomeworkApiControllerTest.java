package org.example.isc.main.secured.scholarhub.controller.api;

import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.enums.scholarhub.HomeworkPriorityEnum;
import org.example.isc.main.enums.scholarhub.HomeworkStatusEnum;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.example.isc.main.secured.scholarhub.service.HomeworkService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeworkApiControllerTest {

    @Test
    void editHomeworkRejectsMismatchedBodyId() {
        AtomicInteger editCalls = new AtomicInteger();
        HomeworkApiController controller = controllerWith(editCalls);
        HomeworkDTO homeworkDTO = validHomework();
        homeworkDTO.setId(2L);

        var response = controller.editHomework(
                1L,
                homeworkDTO,
                new UsernamePasswordAuthenticationToken("alice", "pw")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, editCalls.get());
    }

    @Test
    void editHomeworkAllowsMatchingBodyId() {
        AtomicInteger editCalls = new AtomicInteger();
        HomeworkApiController controller = controllerWith(editCalls);
        HomeworkDTO homeworkDTO = validHomework();
        homeworkDTO.setId(1L);

        var response = controller.editHomework(
                1L,
                homeworkDTO,
                new UsernamePasswordAuthenticationToken("alice", "pw")
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, editCalls.get());
    }

    private HomeworkApiController controllerWith(AtomicInteger editCalls) {
        User user = new User();
        user.setUsername("alice");

        UserRepository userRepository = proxy(UserRepository.class, Map.of(
                "findByUsernameIgnoreCase", args -> Optional.of(user)
        ));
        HomeworkRepository homeworkRepository = proxy(HomeworkRepository.class, Map.of());
        GradeRepository gradeRepository = proxy(GradeRepository.class, Map.of());
        HomeworkService homeworkService = new HomeworkService(null, null, null) {
            @Override
            public Homework edit(Long id, HomeworkDTO homeworkDTO) {
                editCalls.incrementAndGet();
                Homework homework = new Homework();
                homework.setId(id);
                return homework;
            }
        };

        return new HomeworkApiController(userRepository, homeworkRepository, gradeRepository, homeworkService);
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

    private HomeworkDTO validHomework() {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setTitle("Essay");
        homeworkDTO.setPriority(HomeworkPriorityEnum._00FF00);
        homeworkDTO.setSubjectId(1L);
        homeworkDTO.setDueDaySubjectId(1L);
        homeworkDTO.setStatus(HomeworkStatusEnum.Pending);
        homeworkDTO.setWeekStart(LocalDate.of(2026, 3, 30));
        return homeworkDTO;
    }

    @FunctionalInterface
    private interface InvocationHandler {
        Object invoke(Object[] args);
    }
}
