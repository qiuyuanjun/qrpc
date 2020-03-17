package com.qiuyj.qrpc.message.converter;

import com.qiuyj.qrpc.message.Message;
import com.qiuyj.qrpc.message.MessageConverters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author qiuyj
 * @since 2020-03-16
 */
public abstract class AbstractMessageConverter implements MessageConverter {

    @Override
    public Message toMessage(byte[] bytes) throws IOException {
        // 字节数组的前面5个字节，分别用于magic和messageConverter类型选择
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes, 5, bytes.length);
        return internalToMessage(byteStream);
    }

    protected abstract Message internalToMessage(ByteArrayInputStream bytes) throws IOException;

    @Override
    public byte[] fromMessage(Message message) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(8192); // 默认8kb
        MessageConverters.writeMagic(bytes);
        bytes.write(MessageConverters.getConverterType(message));
        internalFromMessage(bytes);
        return bytes.toByteArray();
    }

    protected abstract void internalFromMessage(ByteArrayOutputStream bytes) throws IOException;
}
