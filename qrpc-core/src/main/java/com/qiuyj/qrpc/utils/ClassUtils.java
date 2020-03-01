package com.qiuyj.qrpc.utils;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author qiuyj
 * @since 2020-03-01
 */
public abstract class ClassUtils {

    private ClassUtils() {
        // for private
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... args) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(args);
            ctor.trySetAccessible();
            return ctor;
        }
        catch (NoSuchMethodException e) {
            throw new IllegalStateException("Can not find class: " + clazz.getName() + "'s constructor with args: " + Arrays.toString(args));
        }
    }
}
