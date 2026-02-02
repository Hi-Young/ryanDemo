package com.aeon.demo.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具（仅用于 Demo 入参解析）。
 *
 * @author codex
 */
public final class DateTimeUtils {

    private static final DateTimeFormatter DEFAULT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
    }

    public static LocalDateTime parseOrNull(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(text.trim(), DEFAULT_FMT);
    }
}

