package com.qiuyj.qrpc.message.converter;

import com.qiuyj.qrpc.message.Message;

import java.nio.ByteBuffer;

/**
 * @author qiuyj
 * @since 2020-03-16
 */
public abstract class AbstractMessageConverter implements MessageConverter {

    @Override
    public Message toMessage(byte[] bytes) {
        // 字节数组的前面6个字节，分别用于magic和messageConverter类型选择
        ByteBuffer bb = ByteBuffer.wrap(bytes, 5, bytes.length).asReadOnlyBuffer();
        return internalToMessage(bb);
    }

    protected abstract Message internalToMessage(ByteBuffer bytes);
}
