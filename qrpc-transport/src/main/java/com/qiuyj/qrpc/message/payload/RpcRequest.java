package com.qiuyj.qrpc.message.payload;

import com.qiuyj.qrpc.utils.CollectionUtils;
import com.qiuyj.qrpc.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * rpc请求对象，用于和客户端发送请求到服务器端的封装对象
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcRequest implements Serializable {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private static final long serialVersionUID = 8600692601497985790L;

    /**
     * 当次请求id，全局唯一
     */
    private String requestId;

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

    private RpcRequest() {
        // for private
    }

    public String getRequestId() {
        return requestId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getMethodArgs() {
        return methodArgs;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodArgs=" + Arrays.toString(methodArgs) +
                '}';
    }

    public static class Builder {

        private String requestId;

        private String interfaceName;

        private String methodName;

        private List<Object> methodArgs;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

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

        public RpcRequest build() {
            if (StringUtils.isEmpty(requestId)
                    || StringUtils.isEmpty(interfaceName)
                    || StringUtils.isEmpty(methodName)) {
                throw new IllegalArgumentException("requestId, interfaceName and methodName must not be empty");
            }
            RpcRequest request = new RpcRequest();
            request.requestId = this.requestId;
            request.interfaceName = this.interfaceName;
            request.methodName = this.methodName;
            request.methodArgs = CollectionUtils.isEmpty(this.methodArgs)
                    ? EMPTY_OBJECT_ARRAY
                    : this.methodArgs.toArray(EMPTY_OBJECT_ARRAY);
            return request;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
