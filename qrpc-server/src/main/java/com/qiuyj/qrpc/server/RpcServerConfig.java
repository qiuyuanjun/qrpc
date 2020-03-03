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

    private boolean ignoreTypeMismatch = true;

    private String rpcServerClassName = DEFAULT_RPC_SERVER_CLASS_NAME;

    private int asyncServiceRegistrationQueueSize = 50;

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
            else if ("asyncServiceRegistrationQueueSize".equals(k)) {
                asyncServiceRegistrationQueueSize = Integer.parseInt(val);
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

    public int getAsyncServiceRegistrationQueueSize() {
        return asyncServiceRegistrationQueueSize;
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
                "\nignoreTypeMismatch=" + ignoreTypeMismatch +
                ",\nrpcServerClassName='" + rpcServerClassName + "'" +
                "\n}";
    }
}
