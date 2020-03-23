package com.qiuyj.qrpc.message.payload;

import com.qiuyj.qrpc.utils.CollectionUtils;
import com.qiuyj.qrpc.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * rpc请求对象，用于和客户端发送请求到服务器端的封装对象
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcRequest implements Serializable {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private static final long serialVersionUID = -796588684501963088L;

    /**
     * 客户端调用的rpc接口
     */
    private String interfaceName;

    /**
     * 客户端调用的rpc接口方法名称
     */
    private String methodName;

    /**
     * 要执行的方法参数
     */
    private Object[] methodArgs;

    /**
     * 附加信息
     */
    private Map<String, Object> attachment;

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getMethodArgs() {
        return methodArgs;
    }

    public Map<String, Object> getAttachment() {
        return attachment;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setMethodArgs(Object[] methodArgs) {
        this.methodArgs = methodArgs;
    }

    public void setAttachment(Map<String, Object> attachment) {
        this.attachment = attachment;
    }

    public void addAttachment(String key, Object value) {
        this.attachment = addAttachment(attachment, key, value);
    }

    static Map<String, Object> addAttachment(Map<String, Object> attachment, String key, Object value) {
        if (Objects.nonNull(attachment) && Objects.nonNull(value)) {
            attachment.put(key, value);
        }
        else if (Objects.nonNull(attachment)) {
            attachment.remove(key);
        }
        else if (Objects.nonNull(value)) {
            attachment = new HashMap<>();
            attachment.put(key, value);
        }
        return attachment;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodArgs=" + Arrays.toString(methodArgs) +
                ", attachment=" + attachment +
                '}';
    }

    public static class Builder {

        private String interfaceName;

        private String methodName;

        private List<Object> methodArgs;

        private Map<String, Object> attachment;

        public Builder interfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
            return this;
        }

        public Builder interfaceName(Class<?> interfaceClass) {
            this.interfaceName = interfaceClass.getName();
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder addMethodArg(Object methodArg) {
            if (Objects.isNull(methodArgs)) {
                methodArgs = new ArrayList<>(5);
            }
            this.methodArgs.add(methodArg);
            return this;
        }

        public Builder methodArgs(Object... methodArgs) {
            this.methodArgs = new ArrayList<>(List.of(methodArgs));
            return this;
        }

        public Builder addAttachment(String key, Object value) {
            this.attachment = RpcRequest.addAttachment(this.attachment, key, value);
            return this;
        }

        public Builder attachment(Map<String, Object> attachment) {
            this.attachment = attachment;
            return this;
        }

        public RpcRequest build() {
            if (StringUtils.isEmpty(interfaceName) || StringUtils.isEmpty(methodName)) {
                throw new IllegalArgumentException("The interfaceName and methodName must not be empty");
            }
            RpcRequest request = new RpcRequest();
            request.interfaceName = this.interfaceName;
            request.methodName = this.methodName;
            request.methodArgs = CollectionUtils.isEmpty(this.methodArgs)
                    ? EMPTY_OBJECT_ARRAY
                    : this.methodArgs.toArray(EMPTY_OBJECT_ARRAY);
            if (!CollectionUtils.isEmpty(this.attachment)) {
                request.attachment = this.attachment;
            }
            return request;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
