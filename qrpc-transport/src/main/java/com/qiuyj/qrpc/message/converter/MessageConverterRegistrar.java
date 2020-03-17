package com.qiuyj.qrpc.message.converter;

/**
 * 消息转换器注册中心，用于动态注册消息转换器
 * @author qiuyj
 * @since 2020-03-16
 */
public interface MessageConverterRegistrar {

    /**
     * 增加{@code MessageConverter}，如果已经存在和要增加的转换器的类型一致的，那么抛出异常
     * @param messageConverter 要增加的{@code MessageConverter}对象
     */
    void addConverter(MessageConverter messageConverter);

    /**
     * 替换为给定的{@code MessageConverter}，如果没有和给定的类型一致的{@code MessageConverter}，那么就增加
     * @param messageConverter 替换后的{@code MessageConverter}对象
     * @return 被替换的{{@code MessageConverter}对象，可能为{@code null}
     */
    MessageConverter replaceConverter(MessageConverter messageConverter);
}
