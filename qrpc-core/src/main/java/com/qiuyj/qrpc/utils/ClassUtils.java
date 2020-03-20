package com.qiuyj.qrpc.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

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

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable e) {
            // ignore
        }
        if (Objects.isNull(cl)) {
            try {
                cl = ClassUtils.class.getClassLoader(); // 当前线程的类加载器不存在，那么获取当前类的类加载器（一般不为null）
            }
            catch (Throwable e) {
                // ignore
            }
            if (Objects.isNull(cl)) {
                try {
                    cl = ClassLoader.getSystemClassLoader(); // 得到当前系统的类加载器（一般是AppClassLoader）
                }
                catch (Throwable e) {
                    // ignore
                }
                if (Objects.isNull(cl)) {
                    try {
                        cl = ClassLoader.getPlatformClassLoader(); // 得到平台类加载器（一般是PlatformClassLoader）
                    }
                    catch (Throwable e) {
                        // ignore
                    }
                }
            }
        }
        return cl;
    }

    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className, false, getDefaultClassLoader());
    }

    public static Class<?> resolveClass(String className) {
        try {
            return classForName(className);
        }
        catch (ClassNotFoundException e) {
            // ignore and return null
            return null;
        }
    }

    public static Field getDeclaredField(Class<?> cls, String fieldName) {
        try {
            Field f = cls.getDeclaredField(fieldName);
            f.trySetAccessible();
            return f;
        }
        catch (NoSuchFieldException e) {
            throw new IllegalStateException("Can not find field: " + fieldName + " in class: " + cls);
        }
    }
}
