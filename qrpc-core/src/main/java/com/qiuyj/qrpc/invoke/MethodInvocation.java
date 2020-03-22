package com.qiuyj.qrpc.invoke;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-22
 */
public abstract class MethodInvocation {

    /**
     * 要执行的接口{@code Class}对象
     */
    private final Class<?> interfaceClass;

    /**
     * 执行的对象，如果是服务器端，那么就是rpc服务实例对象，如果是客户端，那么就是rpc接口代理对象
     */
    private final Object o;

    /**
     * 要执行的方法参数
     */
    private final Object[] methodArgs;

    /**
     * 方法执行的结果，只能被设置一次
     */
    private Object methodInvokeResult;
    private boolean resultSetted;

    protected MethodInvocation(Class<?> interfaceClass, Object o, Object... methodArgs) {
        this.interfaceClass = interfaceClass;
        this.o = o;
        this.methodArgs = methodArgs;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Object getThis() {
        return o;
    }

    protected Object[] getMethodArgs() {
        return methodArgs;
    }

    public Object getMethodInvokeResult() {
        if (!resultSetted) {
            throw new IllegalStateException("Method not implemented yet");
        }
        return methodInvokeResult;
    }

    protected void setMethodInvokeResult(Object o) throws MethodInvocationException {
        if (resultSetted) {
            throw new MethodInvocationException("The method execution result has been set");
        }
        if (Objects.nonNull(o)) {
            // 要设置的返回值结果不为null，那么必须要检查类型是否一致
            if (!getReturnType().isInstance(o)) {
                throw new MethodInvocationException("Method return type mismatch");
            }
        }
        resultSetted = true;
        this.methodInvokeResult = o;
    }

    /**
     * 得到方法的返回值类型
     */
    public abstract Class<?> getReturnType();

    /**
     * 执行具体的方法
     */
    public abstract void proceed() throws Throwable;
}
