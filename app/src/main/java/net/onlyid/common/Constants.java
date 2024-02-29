package net.onlyid.common;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String USER = "user";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    // 给人看的，数据序列化不要用
    public static final DateTimeFormatter DATE_TIME_FORMATTER_H = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}
