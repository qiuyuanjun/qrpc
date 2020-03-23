package com.qiuyj.qrpc.invoke;

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
     * 方法参数类型
     */
    private Class<?>[] methodArgTypes;

    protected MethodInvocation(Class<?> interfaceClass, Object o, Class<?>[] methodArgTypes, Object... methodArgs) {
        this.interfaceClass = interfaceClass;
        this.o = o;
        this.methodArgTypes = methodArgTypes;
        this.methodArgs = methodArgs;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Object getThis() {
        return o;
    }

    public Class<?>[] getMethodArgTypes() {
        return methodArgTypes;
    }

    public Object[] getMethodArgs() {
        return methodArgs;
    }

    /**
     * 得到方法的返回值类型
     */
    public abstract Class<?> getReturnType();

    public abstract String getMethodName();

    /**
     * 执行具体的方法
     */
    public abstract Object proceed() throws Throwable;
}
