package com.code.trade;

import com.code.trade.springboot.AccumulatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Bala
 */

@SpringBootTest
public class AccumulatorServiceTest {
    @Autowired
    private final AccumulatorService acc = new AccumulatorService();

    @Test
    void testEmptyString() {
        assertEquals(0, acc.add(""));
    }

    @Test
    void testSingleNumber() {
        assertEquals(1, acc.add("1"));
        assertEquals(2, acc.add("2"));
        assertEquals(1000, acc.add("1000"));
    }

    @Test
    void testTwoNumbers() {
        assertEquals(3, acc.add("1,2"));
        assertEquals(5, acc.add("2,3"));
    }

    @Test
    void testNewLines() {
        assertEquals(6, acc.add("1\n2,3"));
        assertEquals(10, acc.add("1\n2\n3\n4"));
    }

    @Test
    void testIgnoringNumbersGreaterThan1000() {
        assertEquals(2, acc.add("2,1001"));
        assertEquals(1005, acc.add("1000,2,3"));
        assertEquals(0, acc.add("1001"));
        assertEquals(0, acc.add("1001,1002,1003"));
    }

    @Test
    void testCustomDelimiter() {
        assertEquals(3, acc.add("//;\n1;2"));
        assertEquals(6, acc.add("//***\n1***2***3"));
        assertEquals(6, acc.add("//*|%\n1*2%3"));
    }

    @Test
    void testNegativeNumbers() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> acc.add("1,-2"));
        assertEquals("negatives not allowed: [-2]", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> acc.add("1,-2,-3"));
        assertEquals("negatives not allowed: [-2, -3]", exception.getMessage());
    }

/*    @Test
    void testMultipleDelimiters() {
        assertEquals(6, acc.add("//;|:\n1;2:3"));
        assertEquals(10, acc.add("//|*\n4*3|3"));
    }

    @Test
    void testLongDelimiters() {
        assertEquals(6, acc.add("//***\n1***2***3"));
        assertEquals(0, acc.add("//*\n"));
        assertEquals(2, acc.add("//\">\n\"2"));
    }*/

}
