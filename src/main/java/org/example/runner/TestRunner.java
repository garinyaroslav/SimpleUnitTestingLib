package org.example.runner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.example.annotation.After;
import org.example.annotation.Before;
import org.example.annotation.Test;

/**
 * Простейшая библиотека для тестирования
 */
public final class TestRunner {

    private static final Logger logger = Logger.getLogger(TestRunner.class.getName());

    /**
     * Запускает все тесты класса параллельно
     *
     * @param testedClass класс с тестами
     */
    public static void run(Class<?> testedClass) {
        logger.info("Тестовый класс " + testedClass.getCanonicalName());

        List<Method> beforeMethods = findMethods(testedClass, Before.class);
        List<Method> testMethods = findMethods(testedClass, Test.class);
        List<Method> afterMethods = findMethods(testedClass, After.class);

        List<CompletableFuture<Boolean>> futures = testMethods.stream()
                .map(test -> CompletableFuture.supplyAsync(
                        () -> runSingleTest(testedClass, beforeMethods, test, afterMethods)))
                .toList();

        long passed = futures.stream().map(CompletableFuture::join).filter(Boolean::booleanValue).count();

        printStatistics(testMethods.size(), passed);
    }

    /**
     * Находит методы класса, помеченные указанной аннотацией
     *
     * @param clazz      исследуемый класс
     * @param annotation искомая аннотация
     * @return список найденных методов
     */
    private static List<Method> findMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(annotation))
                .toList();
    }

    /**
     * Выполняет Before - Test - After на новом экземпляре
     *
     * @param testedClass   тестовый класс
     * @param beforeMethods методы @Before
     * @param testMethod    текущий тест
     * @param afterMethods  методы @After
     * @return true, если тест прошёл успешно
     */
    private static boolean runSingleTest(Class<?> testedClass, List<Method> beforeMethods, Method testMethod,
            List<Method> afterMethods) {
        logger.info("Запуск теста ".concat(testMethod.getName()));

        Object instance;

        try {
            instance = createInstance(testedClass);
        } catch (ReflectiveOperationException e) {
            logger.warning("Не удалось создать экземпляр " + testedClass.getName() + ": " + e);
            return false;
        }

        boolean mainPartPassed = invokeAll(instance, beforeMethods, testMethod);
        boolean afterPassed = invokeAll(instance, afterMethods);
        boolean success = mainPartPassed && afterPassed;

        logger.info("Тест ".concat(testMethod.getName()).concat(success ? " прошёл" : " упал"));

        return success;
    }

    /**
     * Создаёт экземпляр класса через конструктор без параметров
     *
     * @param clazz класс
     * @return новый экземпляр
     */
    private static Object createInstance(Class<?> clazz) throws ReflectiveOperationException {
        var constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);

        return constructor.newInstance();
    }

    /**
     * Последовательно вызывает методы на экземпляре
     *
     * @param instance экземпляр тестового класса
     * @param methods  методы для вызова
     * @return true, если все методы выполнились без исключений
     */
    private static boolean invokeAll(Object instance, List<Method> methods) {
        for (Method method : methods) {
            if (!invokeSafely(instance, method)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Вызывает методы Before, затем тест
     *
     * @param instance      экземпляр тестового класса
     * @param beforeMethods методы @Before
     * @param testMethod    тест
     * @return true, если всё выполнилось без исключений
     */
    private static boolean invokeAll(Object instance, List<Method> beforeMethods, Method testMethod) {
        return invokeAll(instance, beforeMethods) && invokeSafely(instance, testMethod);
    }

    /**
     * Вызывает метод и логирует исключение
     *
     * @param instance экземпляр тестового класса
     * @param method   вызываемый метод
     * @return true, если исключения не было
     */
    private static boolean invokeSafely(Object instance, Method method) {
        try {
            method.setAccessible(true);
            method.invoke(instance);
            return true;
        } catch (InvocationTargetException e) {
            logger.warning(
                    "Метод ".concat(method.getName()).concat(" выбросил исключение: ").concat(e.getCause().toString()));
        } catch (ReflectiveOperationException e) {
            logger.warning("Не удалось вызвать метод ".concat(method.getName()).concat(": ").concat(e.toString()));
        }
        return false;
    }

    /**
     * Выводит итоговую статистику
     *
     * @param total  общее количество тестов
     * @param passed количество успешных тестов
     */
    private static void printStatistics(int total, long passed) {
        logger.info("Статистика: всего = ".concat(String.valueOf(total)).concat(", успешно = ")
                .concat(String.valueOf(passed))
                .concat(", упало = ")
                .concat(String.valueOf(total - passed)));
    }
}
