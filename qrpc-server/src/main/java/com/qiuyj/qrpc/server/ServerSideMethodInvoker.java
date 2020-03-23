package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.QrpcException;
import com.qiuyj.qrpc.ctx.RpcContext;
import com.qiuyj.qrpc.filter.Filter;
import com.qiuyj.qrpc.ctx.FilterContext;
import com.qiuyj.qrpc.invoke.AbstractMethodInvoker;
import com.qiuyj.qrpc.invoke.MethodInvocation;
import com.qiuyj.qrpc.message.Message;
import com.qiuyj.qrpc.message.MessageUtils;
import com.qiuyj.qrpc.message.payload.RpcRequest;
import com.qiuyj.qrpc.message.payload.RpcResult;
import com.qiuyj.qrpc.service.ServiceDescriptor;
import com.qiuyj.qrpc.service.ServiceDescriptorContainer;
import com.qiuyj.qrpc.utils.ClassUtils;
import com.qiuyj.qrpc.utils.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * 服务器端的方法执行器
 * @author qiuyj
 * @since 2020-03-22
 */
class ServerSideMethodInvoker extends AbstractMethodInvoker {

    private ServiceDescriptorContainer serviceDescriptorContainer;

    private List<Filter> filters;

    public ServerSideMethodInvoker(ServiceDescriptorContainer serviceDescriptorContainer, List<Filter> filters) {
        this.serviceDescriptorContainer = serviceDescriptorContainer;
        this.filters = filters;
    }

    public Message invoke(Message requestMsg) {
        RpcRequest request = requestMsg.asRequestPayload();
        MethodInvocation invocation = createInvocation(request);
        FilterContext fCtx = CollectionUtils.isEmpty(request.getAttachment())
                ? new FilterContext(filters, this, invocation)
                : new FilterContext(request.getAttachment(), filters, this, invocation);
        fCtx.fireNextFilter();
        RpcResult result = fCtx.getResult();
        // 给RpcResult设置attachment
        if (RpcContext.getContextIfAvailable().isPresent()) {
            RpcContext.getContext().getAttachment().forEach(result::addAttachment);
        }
        else {
            Optional.ofNullable(fCtx.getContext()).ifPresent(c -> c.forEach(result::addAttachment));
        }
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
    protected Object internalInvoke(MethodInvocation invocation) throws Throwable {
        return invocation.proceed();
    }
}
