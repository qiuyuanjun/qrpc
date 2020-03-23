package com.qiuyj.qrpc.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 基于反射的方法执行
 * @author qiuyj
 * @since 2020-03-22
 */
public class ReflectiveMethodInvocation extends MethodInvocation {

    private final Method m;

    public ReflectiveMethodInvocation(Method m,
                                      Class<?> interfaceClass,
                                      Object o,
                                      Object... methodArgs) {
        super(interfaceClass, o, getMethodArgTypes(m), methodArgs);
        this.m = m;
    }

    static Class<?>[] getMethodArgTypes(Method m) {
        return m.getParameterTypes();
    }

    @Override
    public Class<?> getReturnType() {
        return m.getReturnType();
    }

    @Override
    public String getMethodName() {
        return m.getName();
    }

    @Override
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return m.invoke(getThis(), getMethodArgs());
    }
}
