package TestMethods;

import java.util.Arrays;

public class ArraysMethods {

    public static int[] arrayAfterFourExtract(int[] array) {
        int[] arrayResult = null;
        if (array != null && array.length != 1) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (array[i] == 4) {
                    arrayResult = Arrays.copyOfRange(array, i + 1, array.length);
                    break;
                } else if (i == 0) {
                    throw new RuntimeException();
                }
            }
        }
        return arrayResult;
    }

    public static boolean oneOrFourChecker(int[] array) {
        if (array != null) {
            Arrays.sort(array);
        } else {
            return false;
        }
        return Arrays.binarySearch(array, 1) >= 0 || Arrays.binarySearch(array, 4) >= 0;
    }
}
