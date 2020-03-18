package com.qiuyj.qrpc.server;

import com.qiuyj.qrpc.server.nio.NioRpcServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author qiuyj
 * @since 2020-03-01
 */
@SuppressWarnings("unused")
public class RpcServerConfig {

    private static final String DEFAULT_RPC_SERVER_CLASS_NAME = NioRpcServer.class.getName();

    private static final String CONFIG_FILE_LOC = "META-INF/qrpc-config.conf";

    /**
     * 是否忽略注册的服务实例和给定的接口类型不匹配，
     * 如果忽略，那么打印warn级别的日志，如果不忽略，那么抛出异常
     */
    private boolean ignoreTypeMismatch = true;

    private String rpcServerClassName = DEFAULT_RPC_SERVER_CLASS_NAME;

    /**
     * 异步服务注册和注销队列的大小
     */
    private int asyncServiceRegistrationUnregistrationQueueSize = 50;

    /**
     * 是否允许将服务注册到服务注册中心
     */
    private boolean enableServiceRegistration = true;

    /**
     * rpc服务端口
     */
    private int port = 11221;

    public RpcServerConfig(Properties properties) {
        this(new HashMap<>(properties));
    }

    public RpcServerConfig(Map<Object, Object> config) {
        config.forEach((k, v) -> {
            String val = (String) v;
            if ("ignoreTypeMismatch".equals(k)) {
                ignoreTypeMismatch = Boolean.parseBoolean(val);
            }
            else if ("rpcServerClassName".equals(k)) {
                rpcServerClassName = val;
            }
            else if ("asyncServiceRegistrationUnregistrationQueueSize".equals(k)) {
                asyncServiceRegistrationUnregistrationQueueSize = Integer.parseInt(val);
            }
            else if ("enableServiceRegistration".equals(k)) {
                enableServiceRegistration = Boolean.parseBoolean(val);
            }
            else if ("server.port".equals(k)) {
                port = Integer.parseInt(val);
            }
        });
    }

    public boolean isIgnoreTypeMismatch() {
        return ignoreTypeMismatch;
    }

    @SuppressWarnings("unchecked")
    Class<? extends RpcServer> getRpcServerClass() {
        Class<? extends RpcServer> serverClass;
        try {
            serverClass = (Class<? extends RpcServer>) Class.forName(rpcServerClassName);
        }
        catch (ClassNotFoundException e) {
            RpcServerFactory.LOG.warn("Can not find rpc server class " + rpcServerClassName + ", and use " + DEFAULT_RPC_SERVER_CLASS_NAME + " as default", e);
            serverClass = NioRpcServer.class;
        }
        return serverClass;
    }

    public int getAsyncServiceRegistrationUnregistrationQueueSize() {
        return asyncServiceRegistrationUnregistrationQueueSize;
    }

    public boolean isEnableServiceRegistration() {
        return enableServiceRegistration;
    }

    public int getPort() {
        return port;
    }

    static RpcServerConfig createDefault() {
        InputStream is = RpcServerConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE_LOC);
        RpcServerConfig config;
        if (Objects.nonNull(is)) {
            Properties properties = new Properties();
            try {
                properties.load(is);
            }
            catch (IOException e) {
                throw new IllegalStateException("Can not load rpc server config file: " + CONFIG_FILE_LOC, e);
            }
            config = new RpcServerConfig(properties);
        }
        else {
            RpcServerFactory.LOG.warn("Rpc server config file: {} not exists, and use default config", CONFIG_FILE_LOC);
            config = new RpcServerConfig();
        }
        return config;
    }

    private RpcServerConfig() {
        // for private
    }

    @Override
    public String toString() {
        return "RpcServerConfig{" +
                "\n ignoreTypeMismatch=" + ignoreTypeMismatch +
                ",\n rpcServerClassName='" + rpcServerClassName + '\'' +
                ",\n asyncServiceRegistrationUnregistrationQueueSize=" + asyncServiceRegistrationUnregistrationQueueSize +
                ",\n enableServiceRegistration=" + enableServiceRegistration +
                ",\n port=" + port +
                "\n}";
    }
}
