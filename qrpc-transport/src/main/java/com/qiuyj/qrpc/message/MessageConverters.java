package com.qiuyj.qrpc.message;

import com.qiuyj.qrpc.message.converter.AbstractMessageConverter;
import com.qiuyj.qrpc.message.converter.MessageConverter;
import com.qiuyj.qrpc.message.converter.MessageConverterRegistrar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qiuyj
 * @since 2020-03-16
 */
public class MessageConverters implements MessageConverter, MessageConverterRegistrar {

    private static final int MESSAGE_MAGIC = ByteOrder.genMagic(0x19961122);

    /**
     * 所有注册的转换器
     */
    private static ConcurrentMap<Integer, AbstractMessageConverter> messageConverterMap = new ConcurrentHashMap<>();

    /**
     * 默认的消息转换器，当如果无法从请求报文里面获取到对应的消息转换器的时候，使用该默认的转换器
     */
    private static AbstractMessageConverter defaultMessageConverter;

    /**
     * 设置默认的消息转换器，在服务器启动范围之内，只能设置一次
     * @param defaultMessageConverter 默认的消息转换器
     */
    public static void setDefaultMessageConverter(MessageConverter defaultMessageConverter) {
        if (Objects.nonNull(MessageConverters.defaultMessageConverter)) {
            throw new IllegalStateException("The defaultMessageConverter has already exist");
        }
        if (!(defaultMessageConverter instanceof AbstractMessageConverter)) {
            throw new IllegalArgumentException("Not an AbstractMessageConverter subclass");
        }
        Integer type = checkMessageConverterType(defaultMessageConverter);
        messageConverterMap.putIfAbsent(type,
                (MessageConverters.defaultMessageConverter = (AbstractMessageConverter) defaultMessageConverter));
    }

    private static Integer checkMessageConverterType(MessageConverter messageConverter) {
        Integer type = messageConverter.type();
        if (Objects.isNull(type)) {
            throw new IllegalStateException("The message converter's type must not be null");
        }
        if (type > 0xFF || type < 0x00) {
            throw new IllegalStateException("Type value out of bound, must be in the range of [0x00, 0xff]");
        }
        return type;
    }

    @Override
    public Integer type() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Message toMessage(byte[] bytes) throws IOException {
        if (bytes.length < 6) {
            // 消息有问题
            throw new BadMessageException("Message bytes' length must gte 6");
        }
        // 读取前面4个字节（魔数）
        int magic = ByteOrder.BIG_ENDIAN.getInt(bytes, 0);
        if (magic != MESSAGE_MAGIC) {
            throw new BadMessageException("Message's magic number is wrong");
        }
        MessageConverter converter = messageConverterMap.get(bytes[4] & 0xFF);
        if (Objects.isNull(converter)) {
            // 找不到消息转换器，那么使用默认的消息转换器
            converter = defaultMessageConverter;
            if (Objects.isNull(converter)) {
                // 抛出异常
                throw new UnknownMessageTypeException("Can not find out the message converter to decode this message");
            }
        }
        return converter.toMessage(bytes);
    }

    @Override
    public byte[] fromMessage(Message message) throws IOException {
        MessageConverter converter = getMessageConverter(message);
        return converter.fromMessage(message);
    }

    private static MessageConverter getMessageConverter(Message message) {
        Object type = message.messageHeaders.removeConverterTypeHeader();
        MessageConverter converter = type instanceof Integer
                ? messageConverterMap.get(type)
                : defaultMessageConverter;
        if (Objects.isNull(converter)) {
            throw new UnknownMessageTypeException("Can not find out the message converter to encode this message");
        }
        return converter;
    }

    public static Integer getConverterType(Message message) {
        return getMessageConverter(message).type() & 0xFF;
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
                bytes[offset] = (byte) ((val >>> 24) & 0xFF);
                bytes[offset + 1] = (byte) ((val >>> 16) & 0xFF);
                bytes[offset + 2] = (byte) ((val >>> 8) & 0xFF);
                bytes[offset + 3] = (byte) (val & 0xFF);
            }

            @Override
            byte[] getBytes(int val) {
                return new byte[] {
                        (byte) ((val >>> 24) & 0xFF),
                        (byte) ((val >>> 16) & 0xFF),
                        (byte) ((val >>> 8) & 0xFF),
                        (byte) (val & 0xFF)
                };
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
                bytes[offset] = (byte) (val & 0xFF);
                bytes[offset + 1] = (byte) ((val >>> 8) & 0xFF);
                bytes[offset + 2] = (byte) ((val >>> 16) & 0xFF);
                bytes[offset + 3] = (byte) ((val >>> 24) & 0xFF);
            }

            @Override
            byte[] getBytes(int val) {
                return new byte[] {
                        (byte) (val & 0xFF),
                        (byte) ((val >>> 8) & 0xFF),
                        (byte) ((val >>> 16) & 0xFF),
                        (byte) ((val >>> 24) & 0xFF)
                };
            }
        };

        abstract int getInt(byte[] bytes, int offset);

        abstract void putInt(byte[] bytes, int val, int offset);

        abstract byte[] getBytes(int val);

        /**
         * 获取当前平台的字节序
         */
        private static ByteOrder platform() {
            long l = 0x0102030405060708L;
            switch ((byte) l) {
                case 0x01: // 表明系统是大端方式
                    return BIG_ENDIAN;
                case 0x08: // 表明系统是小端方式
                    return LITTLE_ENDIAN;
                default:
                    return BIG_ENDIAN;
            }
        }

        private static int genMagic(int magic) {
            // 如果当前系统使用的是大端方式，那么直接返回
            // 如果当前系统使用的是小端方式，那么需要将其转换为大端方式
            return platform() == BIG_ENDIAN
                    ? magic
                    : BIG_ENDIAN.getInt(BIG_ENDIAN.getBytes(magic), 0);
        }
    }

    public static void writeMagic(ByteArrayOutputStream bytes) throws IOException {
        byte[] magic = new byte[4]; // 魔数4个字节
        // 默认使用大端方式编码magic
        ByteOrder.BIG_ENDIAN.putInt(magic, MESSAGE_MAGIC, 0);
        bytes.write(magic);
    }

    public static void reset() {
        messageConverterMap.clear();
        defaultMessageConverter = null;
    }

    @Override
    public void addConverter(MessageConverter messageConverter) {
        if (!(messageConverter instanceof AbstractMessageConverter)) {
            throw new IllegalArgumentException("Not an AbstractMessageConverter subclass");
        }
        Integer type = checkMessageConverterType(messageConverter);
        AbstractMessageConverter shouldBeNull =
                messageConverterMap.putIfAbsent(type, (AbstractMessageConverter) messageConverter);
        if (Objects.nonNull(shouldBeNull)) {
            throw new IllegalStateException("MessageConverter's type: " + type + " already exist");
        }
    }

    @Override
    public MessageConverter replaceConverter(MessageConverter messageConverter) {
        if (!(messageConverter instanceof AbstractMessageConverter)) {
            throw new IllegalArgumentException("Not an AbstractMessageConverter subclass");
        }
        Integer type = checkMessageConverterType(messageConverter);
        return messageConverterMap.replace(type, (AbstractMessageConverter) messageConverter);
    }
}
