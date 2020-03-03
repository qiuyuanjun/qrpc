package com.qiuyj.qrpc.utils;

import java.util.Collection;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-03
 */
public abstract class CollectionUtils {

    private CollectionUtils() {
        // for private
    }

    public static boolean isEmpty(Collection<?> c) {
        return Objects.isNull(c) || c.isEmpty();
    }
}
