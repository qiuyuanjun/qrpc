package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.utils.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * rpc报文头信息
 * @author qiuyj
 * @since 2020-03-15
 */
public class MessageHeaders implements Serializable {

    private final Map<String, Object> headers;

    public MessageHeaders() {
        this(new HashMap<>());
    }

    public MessageHeaders(Map<String, Object> headers) {
        this.headers = headers;
        if (!headers.isEmpty()) {
            headers.keySet().forEach(this::checkSystemDefinedHeaderKey);
        }
        addHeader(KEY_TIMESTAMP, System.currentTimeMillis(), true);
    }

    public Object getHeader(String key) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Header key can not be null or empty");
        }
        return headers.get(key);
    }

    public <T> T getHeader(String key, Class<T> type) {
        Object header = getHeader(key);
        if (Objects.isNull(header)) {
            return null;
        }
        // 转换成对应的类型
        return type.cast(header);
    }

    public void addHeader(String key, Object value) {
        addHeader(key, value, false);
    }

    private void addHeader(String key, Object value, boolean internal) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Header key can not be null or empty");
        }
        if (!internal) {
            checkSystemDefinedHeaderKey(key);
        }
        if (Objects.isNull(value)) {
            headers.remove(key);
        }
        else {
            headers.put(key, value);
        }
    }

    public boolean isAsync() {
        Boolean async = getHeader(KEY_ASYNC, Boolean.class);
        return Boolean.TRUE.equals(async);
    }

    /**
     * 判断是否是系统预定义的key，如果是，那么抛出异常
     * @param key header的key
     */
    private void checkSystemDefinedHeaderKey(String key) {
        if (SYSTEM_DEFINED_HEADER_KEYS.contains(key)) {
            throw new IllegalStateException("The current key: " + key + " is a system defined key and cannot be updated");
        }
    }

    private static final Set<String> SYSTEM_DEFINED_HEADER_KEYS;

    /**
     * 异步执行的key
     */
    public static final String KEY_ASYNC = "async";

    public static final String KEY_TIMESTAMP = "timestamp";

    static {
        SYSTEM_DEFINED_HEADER_KEYS = new HashSet<>();
        SYSTEM_DEFINED_HEADER_KEYS.add(KEY_ASYNC);
        SYSTEM_DEFINED_HEADER_KEYS.add(KEY_TIMESTAMP);
    }
}
