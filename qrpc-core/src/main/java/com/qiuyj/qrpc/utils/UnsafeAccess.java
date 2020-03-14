package com.qiuyj.qrpc.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;

/**
 * @author qiuyj
 * @since 2020-03-14
 */
public abstract class UnsafeAccess {

    private static final Unsafe THE_UNSAFE;

    static {
        PrivilegedExceptionAction<Unsafe> a = () -> {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.trySetAccessible();
            return (Unsafe) f.get(null);
        };
        try {
            THE_UNSAFE = a.run();
        }
        catch (Exception e) {
            throw new IllegalStateException("Can not get Unsafe instance", e);
        }
    }

    public static Unsafe getUnsafe() {
        return THE_UNSAFE;
    }
}
