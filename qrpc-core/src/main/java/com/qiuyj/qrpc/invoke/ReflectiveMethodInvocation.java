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
        super(interfaceClass, o, methodArgs);
        this.m = m;
    }

    @Override
    public Class<?> getReturnType() {
        return m.getReturnType();
    }

    @Override
    public void proceed() throws InvocationTargetException, MethodInvocationException {
        try {
            setMethodInvokeResult(m.invoke(getThis(), getMethodArgs()));
        }
        catch (IllegalAccessException e) {
            // never happen
        }
    }
}
