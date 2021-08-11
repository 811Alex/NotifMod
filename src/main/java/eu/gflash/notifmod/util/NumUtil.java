package eu.gflash.notifmod.util;

import org.apache.commons.lang3.tuple.Triple;

/**
 * Number related utility functions.
 * @author Alex811
 */
public class NumUtil {
    /**
     * Maps int within a range, to a 0 - 1 float.
     * @param value the int to map
     * @param min range's min
     * @param max range's max
     * @return mapped float
     */
    public static double mapValue(int value, int min, int max){
        return ((double) value - min) / (max - min);
    }

    /**
     * Maps 0 - 1 float to an int withing a range.
     * @param value the float to map
     * @param min range's min
     * @param max range's max
     * @return mapped int
     */
    public static long mapValue(double value, int min, int max){
        return Math.round((max - min) * value) + min;
    }

    /**
     * Split seconds to hours, minutes, seconds.
     * @param seconds input to split
     * @return hours, minutes, seconds
     */
    public static Triple<Integer, Integer, Integer> secToHMS(int seconds){
        int s = seconds % 60;
        int hm = seconds / 60;
        int m = hm % 60;
        int h = hm / 60;
        return Triple.of(h, m, s);
    }

    /**
     * Get hh:mm:ss {@link String} from total seconds.
     * @param seconds total seconds
     * @return hh:mm:ss {@link String}
     */
    public static String secToHMSString(int seconds){
        Triple<Integer, Integer, Integer> hms = secToHMS(seconds);
        return padHMSNum(hms.getLeft()) + ":" + padHMSNum(hms.getMiddle()) + ":" + padHMSNum(hms.getRight());
    }

    private static String padHMSNum(int num){
        return String.format("%02d", num);
    }

    /**
     * Get total seconds from hours, minutes and seconds.
     * @param hours amount of hours
     * @param minutes amount of minutes
     * @param seconds amount of seconds
     * @return total seconds
     */
    public static int HMSToSec(int hours, int minutes, int seconds){
        return hours * 3600 + minutes * 60 + seconds;
    }
}
