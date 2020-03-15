package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.utils.CollectionUtils;
import com.qiuyj.qrpc.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * rpc请求对象，用于和客户端发送请求到服务器端的封装对象
 * @author qiuyj
 * @since 2020-03-15
 */
public class RpcRequest implements Serializable {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final long serialVersionUID = -8837942011123431741L;

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
     * 方法参数类型
     */
    private String[] methodArgTypes;

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

    public String[] getMethodArgTypes() {
        return methodArgTypes;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodArgTypes=" + Arrays.toString(methodArgTypes) +
                '}';
    }

    public static class Builder {

        private String requestId;

        private String interfaceName;

        private String methodName;

        private List<String> methodArgTypes;

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

        public Builder addMethodArgType(String methodArgType) {
            if (Objects.isNull(methodArgTypes)) {
                methodArgTypes = new ArrayList<>(5);
            }
            this.methodArgTypes.add(methodArgType);
            return this;
        }

        public Builder addMethodArgType(Class<?> methodArgType) {
            if (Objects.isNull(methodArgTypes)) {
                methodArgTypes = new ArrayList<>(5);
            }
            this.methodArgTypes.add(methodArgType.getName());
            return this;
        }

        public Builder methodArgTypes(String... methodArgTypes) {
            this.methodArgTypes = new ArrayList<>(List.of(methodArgTypes));
            return this;
        }

        public Builder methodArgTypes(Class<?>... methodArgTypes) {
            this.methodArgTypes = Arrays.stream(methodArgTypes)
                    .map(Class::getName)
                    .collect(Collectors.toList());
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
            request.methodArgTypes = CollectionUtils.isEmpty(this.methodArgTypes)
                    ? EMPTY_STRING_ARRAY
                    : this.methodArgTypes.toArray(EMPTY_STRING_ARRAY);
            return request;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
