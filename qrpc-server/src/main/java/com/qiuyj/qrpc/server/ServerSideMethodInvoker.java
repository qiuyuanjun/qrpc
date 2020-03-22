package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.QrpcException;
import com.qiuyj.qrpc.filter.Filter;
import com.qiuyj.qrpc.filter.FilterContext;
import com.qiuyj.qrpc.invoke.MethodInvocation;
import com.qiuyj.qrpc.invoke.MethodInvoker;
import com.qiuyj.qrpc.message.Message;
import com.qiuyj.qrpc.message.MessageUtils;
import com.qiuyj.qrpc.message.payload.RpcRequest;
import com.qiuyj.qrpc.message.payload.RpcResult;
import com.qiuyj.qrpc.service.ServiceDescriptor;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.utils.ClassUtils;
import com.qiuyj.qrpc.utils.CollectionUtils;

import java.util.List;

/**
 * 服务器端的方法执行器
 * @author qiuyj
 * @since 2020-03-22
 */
class ServerSideMethodInvoker implements MethodInvoker {

    private ServiceDescriptorContainer serviceDescriptorContainer;

    private List<Filter> filters;

    public ServerSideMethodInvoker(ServiceDescriptorContainer serviceDescriptorContainer, List<Filter> filters) {
        this.serviceDescriptorContainer = serviceDescriptorContainer;
        this.filters = filters;
    }

    public Message invoke(Message requestMsg) {
        RpcRequest request = requestMsg.asRequestPayload();
        MethodInvocation invocation = createInvocation(request);
        FilterContext fCtx = CollectionUtils.isEmpty(requestMsg.getAttachment())
                ? new FilterContext(filters, this, invocation)
                : new FilterContext(requestMsg.getAttachment(), filters, this, invocation);
        fCtx.fireNextFilter();
        RpcResult result = (RpcResult) fCtx.getResult();
        // 从request请求里面获取requestId，并设置到result里面
        result.setRequestId(request.getRequestId());
        return MessageUtils.resultMessage(result);
    }

    private MethodInvocation createInvocation(RpcRequest request) {
        Class<?> interfaceClass;
        try {
            interfaceClass = ClassUtils.classForName(request.getInterfaceName());
        }
        catch (ClassNotFoundException e) {
            throw new QrpcException(QrpcException.ERR_CODE_MESSAGE_DESERIALIZE, e);
        }
        ServiceDescriptor sd = serviceDescriptorContainer.get(interfaceClass).orElseThrow(
                () -> new QrpcException(QrpcException.ERR_CODE_SERVICE_NOT_FOUND, "Can not find rpc service: " + interfaceClass.getName()));
        Object service = sd.getObject();
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
