package com.qiuyj.qrpc.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * 基于{@code MethodHandle}机制的方法调用
 * @author qiuyj
 * @since 2020-03-22
 */
public class MethodHandleMethodInvocation extends MethodInvocation {

    private final MethodHandle mh;

    private String methodName;

    public MethodHandleMethodInvocation(String methodName,
                                        MethodHandle mh,
                                        Class<?> interfaceClass,
                                        Object o,
                                        Object... methodArgs) {
        super(interfaceClass, o, getMethodArgTypes(mh), methodArgs);
        this.methodName = methodName;
        this.mh = mh;
    }

    private static Class<?>[] getMethodArgTypes(MethodHandle mh) {
        return mh.type().parameterArray();
    }

    public MethodHandleMethodInvocation(Method m,
                                        Class<?> interfaceClass,
                                        Object o,
                                        Object... methodArgs) {
        super(interfaceClass, o, ReflectiveMethodInvocation.getMethodArgTypes(m), methodArgs);
        this.methodName = m.getName();
        try {
            // 所有的rpc服务，均已invokevirtual指令调用
            this.mh = MethodHandles.publicLookup()
//                    .in(o.getClass())
                    .findVirtual(o.getClass(), m.getName(), genMethodType(m));
        }
        catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Can not find method: " + m.getName());
        }
    }

    private MethodType genMethodType(Method m) {
        return MethodType.methodType(m.getReturnType(), m.getParameterTypes());
    }

    @Override
    public Class<?> getReturnType() {
        return mh.type().returnType();
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Object proceed() throws Throwable {
        return mh.bindTo(getThis()).invokeWithArguments(getMethodArgs());
    }
}
