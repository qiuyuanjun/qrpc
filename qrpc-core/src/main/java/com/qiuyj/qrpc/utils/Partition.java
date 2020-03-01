package com.qiuyj.qrpc.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 对{@link List#subList(int, int)}的封装的简便使用的类
 * @author qiuyj
 * @since 2020-03-01
 */
public class Partition<E> implements Iterator<List<E>> {

    private List<E> source;

    private int partitionSize;

    private int cursor = -1;

    private int size;

    public Partition(List<E> list) {
        this(list, 10);
    }

    public Partition(List<E> list, int partitionSize) {
        this.source = Objects.requireNonNull(list, "sourceList is null");
        if (partitionSize <= 0) {
            throw new IllegalArgumentException("partitionSize <= 0");
        }
        int listSize = list.size();
        this.size = listSize <= partitionSize
                ? 1
                : (listSize % partitionSize == 0
                        ? listSize / partitionSize
                        : (listSize / partitionSize) + 1);
        this.partitionSize = partitionSize;
    }

    @Override
    public boolean hasNext() {
        return ++cursor < size;
    }

    @Override
    public List<E> next() {
        int fromIndex = cursor * partitionSize;
        int toIndex = cursor == size - 1 ? source.size() : fromIndex + partitionSize;
        return source.subList(fromIndex, toIndex);
    }

    public List<E> getSource() {
        return source;
    }

    public void reset() {
        cursor = -1;
    }
}
