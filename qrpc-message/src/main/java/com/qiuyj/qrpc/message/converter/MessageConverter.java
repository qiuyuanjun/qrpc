package com.qiuyj.qrpc.message.converter;

import com.qiuyj.qrpc.message.Message;

import java.io.IOException;

/**
 * 消息转换器，提供从字节数组转换为{@link com.qiuyj.qrpc.message.Message}的方法
 * 同时也提供将{@link com.qiuyj.qrpc.message.Message}转换为字节数组的方法
 * @author qiuyj
 * @since 2020-03-16
 */
public interface MessageConverter {

    /**
     * 返回当前消息转换器的类型，唯一标识，用于动态获取对应的消息转换器
     * @apiNote 返回的Integer类型范围应为0x00-0xff之间
     * @return 类型标识
     */
    Integer type();

    /**
     * 将字节数组转换为{@code Message}对象
     * @param bytes 字节数组
     * @return 转换后的{{@code Message}对象
     */
    Message toMessage(byte[] bytes) throws IOException;

    /**
     * 将{@code Message}对象转换为字节数组
     * @param message {@code Message}对象
     * @return 转换后的字节数组
     */
    byte[] fromMessage(Message message) throws IOException;
}
