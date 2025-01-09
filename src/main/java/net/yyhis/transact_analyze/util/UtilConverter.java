package net.yyhis.transact_analyze.util;

public class UtilConverter {
    public static int convertIteger(String value) {
        int num = -1;

        value = value.replaceAll(" ", "");
        value = value.replaceAll("[원]", ""); // "원" 제거
        value = value.replaceAll(",", ""); // 쉼표 제거
        value = value.replaceAll("[.-]", ""); // 점(.) 및 하이픈(-) 제거

        System.out.println("Converted Num: " + value);

        try {
            if (value.isEmpty()) {
                return 0; // 빈 문자열일 경우 0 반환
            }
            num = Integer.parseInt(value);
            return num;
        } catch (NumberFormatException e) {
            System.out.println(e);
            return num;
        }

    }

    /// isNegative than true.
    public static boolean checkNegative(String value) {
        return value.startsWith("-");
    }

}
