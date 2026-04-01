package org.example.isc.main.secured.scholarhub.service;

import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradeServiceTest {

    @Test
    void getAverageForUserAveragesAllGrades() {
        GradeRepository gradeRepository = proxy(GradeRepository.class, Map.of(
                "findAllBySubjectUserUsername", args -> List.of(
                        grade("0.8000"),
                        grade("0.6000"),
                        grade("1.0000")
                )
        ));

        GradeService gradeService = new GradeService(null, null, gradeRepository);

        assertEquals(Optional.of(new BigDecimal("0.8000")), gradeService.getAverageForUser("alice"));
    }

    @Test
    void getAverageForUserReturnsEmptyWhenUserHasNoGrades() {
        GradeRepository gradeRepository = proxy(GradeRepository.class, Map.of(
                "findAllBySubjectUserUsername", args -> List.of()
        ));

        GradeService gradeService = new GradeService(null, null, gradeRepository);

        assertEquals(Optional.empty(), gradeService.getAverageForUser("alice"));
    }

    private Grade grade(String convertedValue) {
        Grade grade = new Grade();
        grade.setConverted(new BigDecimal(convertedValue));
        return grade;
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

    @FunctionalInterface
    private interface InvocationHandler {
        Object invoke(Object[] args);
    }
}
