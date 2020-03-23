package com.qiuyj.qrpc.invoke;

import com.qiuyj.qrpc.QrpcException;
import com.qiuyj.qrpc.logger.InternalLogger;
import com.qiuyj.qrpc.logger.InternalLoggerFactory;
import com.qiuyj.qrpc.message.payload.RpcResult;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-23
 */
public class AbstractMethodInvoker implements MethodInvoker {

    private static final InternalLogger LOG = InternalLoggerFactory.getLogger(AbstractMethodInvoker.class);

    @Override
    public RpcResult invoke(MethodInvocation invocation) {
        RpcResult result = new RpcResult();
        try {
            Object value = internalInvoke(invocation);
            if (Objects.nonNull(value) && !invocation.getReturnType().isInstance(value)) {
                result.setException(new QrpcException(QrpcException.ERR_CODE_INVOKE_TARGET, "The method invoke return type mismatch"));
            }
            else {
                result.setValue(value);
            }
        }
        catch (Throwable t) {
            // 执行失败，将错误信息设置，方便后续{@code ExceptionFilter}过滤器注解处理
            result.setException(t);
            // 记录日志
            LOG.error("Error in " + invocation.getMethodName() + " method of RPC service execution with interface: " + invocation.getInterfaceClass().getName(), t);
        }
        return result;
    }

    protected Object internalInvoke(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }
}
