package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.message.converter.AbstractMessageConverter;
import com.qiuyj.qrpc.message.converter.MessageConverter;
import com.qiuyj.qrpc.message.converter.MessageConverterRegistrar;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qiuyj
 * @since 2020-03-16
 */
public class MessageConverters implements MessageConverter, MessageConverterRegistrar {

    private static final int MESSAGE_MAGIC = 0x19961122;

    /**
     * 所有注册的转换器
     */
    private ConcurrentMap<Integer, AbstractMessageConverter> messageConverterMap = new ConcurrentHashMap<>();

    /**
     * 默认的消息转换器，当如果无法从请求报文里面获取到对应的消息转换器的时候，使用该默认的转换器
     */
    private AbstractMessageConverter defaultMessageConverter;

    public void setDefaultMessageConverter(MessageConverter defaultMessageConverter) {
        if (!(defaultMessageConverter instanceof AbstractMessageConverter)) {
            throw new IllegalArgumentException("Not an AbstractMessageConverter subclass");
        }
        this.defaultMessageConverter = (AbstractMessageConverter) defaultMessageConverter;
    }

    @Override
    public Integer type() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message toMessage(byte[] bytes) {
        if (bytes.length < 6) {
            // 消息有问题
            throw new BadMessageException("Message bytes' length must gte 6");
        }
        // 读取前面4个字节（魔数）
        int magic = ByteOrder.platformReversed().getInt(bytes, 0);
        if (magic != MESSAGE_MAGIC) {
            throw new BadMessageException("Message's magic number is wrong");
        }
        MessageConverter converter = messageConverterMap.get((int) bytes[4]);
        if (Objects.isNull(converter)) {
            // 找不到消息转换器，那么使用默认的消息转换器
            converter = defaultMessageConverter;
            if (Objects.isNull(converter)) {
                // 抛出异常
                throw new UnknownMessageTypeException("Can not find out message converter to decode this message");
            }
        }
        return converter.toMessage(bytes);
    }

    @Override
    public byte[] fromMessage(Message message) {
        return new byte[0];
    }

    private enum ByteOrder {

        /**
         * 大端方式的字节序，数据的高字节保存在内存的低地址
         */
        BIG_ENDIAN() {

            @Override
            int getInt(byte[] bytes, int offset) {
                return bytes[offset] << 24
                        | bytes[offset + 1] << 16
                        | bytes[offset + 2] << 8
                        | bytes[offset + 3];
            }

            @Override
            void putInt(byte[] bytes, int val, int offset) {
                bytes[offset] = (byte) (val >>> 24);
                bytes[offset + 1] = (byte) (val >>> 16);
                bytes[offset + 2] = (byte) (val >>> 8);
                bytes[offset + 3] = (byte) val;
            }
        },

        /**
         * 小端方式的字节序，数据的高字节保存在内存的高地址中
         */
        LITTLE_ENDIAN() {

            @Override
            int getInt(byte[] bytes, int offset) {
                return bytes[offset + 3] << 24
                        | bytes[offset + 2] << 16
                        | bytes[offset + 1] << 8
                        | bytes[offset];
            }

            @Override
            void putInt(byte[] bytes, int val, int offset) {
                bytes[offset] = (byte) val;
                bytes[offset + 1] = (byte) (val >>> 8);
                bytes[offset + 2] = (byte) (val >>> 16);
                bytes[offset + 3] = (byte) (val >>> 24);
            }
        };

        abstract int getInt(byte[] bytes, int offset);

        abstract void putInt(byte[] bytes, int val, int offset);

        /**
         * 获取和平台相反的字节序
         */
        private static ByteOrder platformReversed() {
            long l = 0x0102030405060708L;
            switch ((byte) l) {
                case 0x01: // 表明系统是大端方式
                    return LITTLE_ENDIAN;
                case 0x08: // 表明系统是小端方式
                    return BIG_ENDIAN;
                default:
                    return BIG_ENDIAN;
            }
        }
    }
}
