# Simple Test Runner

Простейшая библиотека для тестирования на Java, использующая аннотации и рефлексию. Запускает тестовые методы параллельно

## Возможности

- Аннотации `@Before`, `@Test`, `@After` для разметки методов;
- Для каждого теста создаётся **новый экземпляр** тестового класса — тесты изолированы;
- `@Before`-методы выполняются перед каждым тестом, `@After` — после;
- Тесты запускаются **параллельно**;
- Исключения в методах перехватываются и логируются, падение одного теста не останавливает остальные;
- По завершении выводится статистика: всего / успешно / упало.

## Как использовать

### 1. Разметьте тестовый класс

```java
public class MyTest {

    @Before
    public void setUp() {
        // подготовка перед каждым тестом
    }

    @Test
    public void testSomething() {
        // тело теста
    }

    @After
    public void tearDown() {
        // уборка после каждого теста
    }
}
```

### 2. Запустите раннер

```java
public class Main {
    public static void main(String[] args) {
        TestRunner.run(CalcTest.class);
    }
}
```

## Пример вывода

```
INFO: Тестовый класс org.example.CalcTest
INFO: Запуск теста getSumTest_twoPositiveNumber
INFO: Запуск теста getSumTest_twoNegativeNumber
...
WARNING: Метод getSumTest_twoPositiveNumber выбросил исключение: java.lang.RuntimeException: Broken test
INFO: Тест getSumTest_twoPositiveNumber упал
...
INFO: Статистика: всего = 4, успешно = 2, упало = 2
```
