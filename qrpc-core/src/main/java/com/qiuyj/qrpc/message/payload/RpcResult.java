package com.qiuyj.qrpc.message.payload;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcResult implements Serializable {

    private static final long serialVersionUID = 8873656259008902273L;

    /**
     * 服务执行过程中，抛出的异常（如果有的话）
     */
    private Throwable exception;

    /**
     * 服务执行的结果，可能为null
     */
    private Object value;

    /**
     * 附加信息
     */
    private Map<String, Object> attachment;

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    public void addAttachment(String key, Object value) {
        this.attachment = RpcRequest.addAttachment(this.attachment, key, value);
    }

    public Object recreate() throws Throwable {
        if (Objects.nonNull(exception)) {
            throw exception;
        }
        return value;
    }

    public boolean isSuccess() {
        return Objects.isNull(exception);
    }
}
