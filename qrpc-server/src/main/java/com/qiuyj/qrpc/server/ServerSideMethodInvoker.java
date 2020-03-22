package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.invoke.MethodInvocation;
import com.qiuyj.qrpc.invoke.MethodInvoker;
import com.qiuyj.qrpc.message.Message;
import com.qiuyj.qrpc.message.payload.RpcRequest;
import com.qiuyj.qrpc.message.payload.RpcResult;

/**
 * 服务器端的方法执行器
 * @author qiuyj
 * @since 2020-03-22
 */
class ServerSideMethodInvoker implements MethodInvoker {

    public Message invoke(Message requestMsg) {
        RpcRequest request = requestMsg.asRequestPayload();
        return null;
    }

    @Override
    public RpcResult invoke(MethodInvocation invocation) {
        RpcResult result = new RpcResult();
        try {
            invocation.proceed();
            result.setResult(invocation.getMethodInvokeResult());
        }
        catch (Throwable t) {
            // 执行失败，将错误信息设置，方便后续{@code ExceptionFilter}过滤器注解处理
            result.setCause(t);
        }
        return result;
    }
}
