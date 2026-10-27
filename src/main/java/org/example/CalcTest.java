package org.example;

import java.util.logging.Logger;

import org.example.annotation.After;
import org.example.annotation.Before;
import org.example.annotation.Test;

public class CalcTest {

    private static final Logger logger = Logger.getLogger(CalcTest.class.getName());

    private Calc calc = null;

    @Before
    public void before1() {
        logger.info("Выполняется метод #1 before()");
        calc = new Calc();
    }

    @Before
    public void before2() {
        logger.info("Выполняется метод #2 before()");
    }

    @After
    public void after1() {
        logger.info("Выполняется метод #1 after()");
    }

    @After
    public void after2() {
        logger.info("Выполняется метод #2 after()");
    }

    @Test
    public void getSumTest_twoPositiveNumber() {
        logger.info("Выполняется метод sumTest_twoPositiveNumber()");
        assertEquals(5, calc.sum(2, 3));
        throw new RuntimeException("Broken test");
    }

    @Test
    public void getSumTest_twoNegativeNumber() {
        logger.info("Выполняется метод sumTest_twoNegativeNumber()");
        assertEquals(-5, calc.sum(-2, -3));
    }

    @Test
    public void getMultiplicationTest_twoPositiveNumber() {
        logger.info("Выполняется метод multiplicationTest_twoPositiveNumber()");
        assertEquals(6, calc.multiplication(2, 3));
    }

    @Test
    public void getDivisionTest_byZero() {
        logger.info("Выполняется метод getDivisionTest_byZero()");
        calc.division(10, 0);
    }

    private void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Ожидалось " + expected + ", получено " + actual);
        }
    }
}
