package com.qiuyj.qrpc.utils;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
public abstract class StringUtils {

    private StringUtils() {
        // for private
    }

    public static boolean isEmpty(String s) {
        return Objects.isNull(s) || s.isEmpty();
    }
}
