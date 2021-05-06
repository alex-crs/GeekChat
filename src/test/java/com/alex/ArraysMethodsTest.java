package com.alex;

import TestMethods.ArraysMethods;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

public class ArraysMethodsTest {
    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnArrayAfterLastFourNullTest(int[] array) {
        Assertions.assertNull(ArraysMethods.arrayAfterFourExtract(array));
    }

    @Test
    void shouldReturnNullArrayIfArrayLengthEquallyOneAndValueEquallyFour() {
        Assertions.assertNull(ArraysMethods.arrayAfterFourExtract(new int[]{4}));
    }

    @ParameterizedTest
    @MethodSource("ReturnArrayAfterLastFourProviderParams")
    void shouldReturnArrayAfterLastFour(int[] arrayExpected, int[] currentArray) {
        Assertions.assertArrayEquals(arrayExpected, ArraysMethods.arrayAfterFourExtract(currentArray));
    }

    private static Stream<Arguments> ReturnArrayAfterLastFourProviderParams() {
        return Stream.of(
                Arguments.arguments(new int[]{5, 6, 9}, new int[]{3, 5, 6, 3, 1, 4, 2, 6, 3, 4, 5, 6, 9}),
                Arguments.arguments(new int[]{2, 6, 3, 3, 5, 6, 9}, new int[]{3, 5, 6, 3, 1, 4, 2, 6, 3, 3, 5, 6, 9}),
                Arguments.arguments(new int[]{5, 6, 3, 1, 8, 2, 6, 3, 0, 5, 6, 9}, new int[]{4, 5, 6, 3, 1, 8, 2, 6, 3, 0, 5, 6, 9}),
                Arguments.arguments(new int[]{1, 7}, new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7})
        );
    }

    @Test
    void shouldThrowRuntimeExceptionWhenArrayNotContainsFourNumber() {
        Assertions.assertThrows(RuntimeException.class, () -> ArraysMethods.arrayAfterFourExtract(new int[]{3, 5, 6, 3, 1, 2, 2, 6, 3, 3, 5, 6, 9}));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseIfArrayNotContainsFourOrOneNullTest(int[] array) {
        Assertions.assertFalse(ArraysMethods.oneOrFourChecker(array));
    }

    @ParameterizedTest
    @MethodSource("ReturnFalseIfArrayNotContainsFourOrOneParams")
    void shouldReturnFalseIfArrayNotContainsFourOrOne(int[] currentArray) {
        Assertions.assertTrue(ArraysMethods.oneOrFourChecker(currentArray));
    }

    private static Stream<Arguments> ReturnFalseIfArrayNotContainsFourOrOneParams() {
        return Stream.of(
                Arguments.arguments(new int[]{4, 1, 1, 4}),
                Arguments.arguments(new int[]{4, 4, 4, 4}),
                Arguments.arguments(new int[]{1, 1, 1, 1}),
                Arguments.arguments(new int[]{1, 4, 4, 1})
        );
    }


}
