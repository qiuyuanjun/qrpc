package com.qiuyj.qrpc.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * @author qiuyj
 * @since 2020-03-02
 */
public abstract class RpcRuntimeUtils {

    private RpcRuntimeUtils() {
        // for private
    }

    public static String getMethodSig(Method method) {
        return getMethodSig(method.getName(), method.getParameterTypes());
    }

    /**
     * 得到方法签名
     * @param methodName 方法名
     * @param parameterTypes 方法参数类型
     * @return 对应的签名
     */
    public static String getMethodSig(String methodName, Class<?>... parameterTypes) {
        StringJoiner sig = new StringJoiner("", methodName + "(", ")");
        for (Class<?> c : parameterTypes) {
            sig.add(getInternalTypeName(c));
        }
        return sig.toString();
    }

    private static String getInternalTypeName(Class<?> c) {
        if (c.isPrimitive()) {
            return Array.newInstance(c, 0).getClass().getName().substring(1);
        }
        else if (c.isArray()) {
            return "[" + getInternalTypeName(c.getComponentType());
        }
        else {
            return "L" + c.getName().replace(".", "/") + ";";
        }
    }
}
