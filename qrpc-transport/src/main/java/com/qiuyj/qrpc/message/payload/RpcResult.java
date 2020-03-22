package com.qiuyj.qrpc.message.payload;

import com.qiuyj.qrpc.QrpcException;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcResult implements Serializable {

    /**
     * 对应同一次rpc请求的id，该字段直接从{@code RpcRequest}里面获取
     */
    private String requestId;

    /**
     * 服务执行过程中，抛出的异常（如果有的话）
     */
    private Throwable cause;

    /**
     * 服务执行的结果，可能为null
     */
    private Object result;

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return Objects.isNull(cause);
    }

    public Object getResult() {
        return result;
    }

    public Throwable getCause() {
        return cause;
    }

    public Object recreate() {
        if (isSuccess()) {
            return getResult();
        }
        else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        }
        else if (cause instanceof Exception) {
            throw new QrpcException(QrpcException.ERR_CODE_CAUGHT_EXCEPTION_UNHANDLER, cause);
        }
        else {
            throw new UndeclaredThrowableException(cause);
        }
    }
}
