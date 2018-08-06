package com.example.silence.mybackup.util;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

/*
    第 0 行数据表示表头（字段名称）
 */
public class TableStore implements List<TableStore.Row>, Serializable {
    private static final long serialVersionUID = 100L;

    String[] head;
    Class[] type;
    Row[] body;
    int length;
    int width;
    private AtomicBoolean operating;

    private final static int DEFAULT_CAPACITY = 10;
    private final static int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public TableStore() {
        this.body = new Row[minLength];
        this.length = 0;
    }

    public TableStore(String[] head) {
        this();
        this.width = head.length;
        this.head = head;
    }

    public TableStore(String[] head, Class[] type) {
        this(head);
        this.type = type;
    }

    @Override
    public boolean add(Row row) {
        ensureCapacityInternal(length+1);
        this.body[this.length++ ] = row;
        return true;
    }
    @Override
    public void add(int index, Row element) {
        if (index < 0 || index > length)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        ensureCapacityInternal(length+1);
        System.arraycopy(body, index, body, index+1, length - index);
        body[index] = element;
        length++;
    }

    @Override
    public boolean addAll(@NonNull Collection c) {
        return false;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends Row> c) {
        return false;
    }


    @Override
    public void clear() {
        for (int l=0; l<length; l++)
            body[l] = null;
        this.length = 0;
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public boolean isEmpty() {
        if (this.length > 0) return false;
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) > 0;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return body;
    }


    @NonNull
    @Override
    public Object[] toArray(@NonNull Object[] a) {
        return new Object[0];
    }


    @Override
    public boolean remove(Object o) {
        return false;
    }





    @Override
    public Row get(int index) {
        return null;
    }

    @Override
    public Row set(int index, Row element) {
        return null;
    }



    @Override
    public Row remove(int index) {
        return null;
    }

    @Override
    public boolean retainAll(@NonNull Collection c) {
        return false;
    }

    @Override
    public boolean removeAll(@NonNull Collection c) {
        return false;
    }

    @Override
    public boolean containsAll(@NonNull Collection c) {
        return false;
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for(int i =0; i<length; i++) {
                if (body[i] == null) return i;
            }
        } else {
            for (int i=0; i<length; i++) {
                if (o.equals(body[i])) return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @NonNull
    @Override
    public ListIterator<Row> listIterator() {
        return null;
    }

    @NonNull
    @Override
    public ListIterator<Row> listIterator(int index) {
        return null;
    }

    @NonNull
    @Override
    public List<Row> subList(int fromIndex, int toIndex) {
        return null;
    }

    private void ensureCapacityInternal(int minCapacity) {
        // 初始没有分配空间
        if (this.body == null) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
    }
    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - body.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = body.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        this.body = Arrays.copyOf(this.body, newCapacity);
    }


    private void grow() {
        int newLength = this.length + this.length >> 1;
        if (newLength > Integer.MAX_VALUE)
            newLength = Integer.MAX_VALUE;
        if (newLength < this.minLength)
            newLength = this.minLength;
        for (int l = 0; l < width; l++)
            this.body = Arrays.copyOf(this.body, newLength);
    }
    private int hugeCapacity(int minCapacity) {
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE)? Integer.MAX_VALUE: MAX_ARRAY_SIZE;
    }


    // 越界信息
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Length: " + this.length;
    }

    protected static class Row {
        Object[] data;

        public Row(Object[] data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Row) {
                return Arrays.equals(data, ((Row) obj).data);
            }
            return false;
        }
    }

    protected  class Iter implements Iterator<Row> {
        int cursor;
        int limit;ArrayList

        Iter() {
            limit = TableStore.this.length;
            cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return cursor < limit;
        }

        @Override
        public Row next() {

            if ()
                return null;
        }
    }
    protected class ListIter extends Iter implements ListIterator<Row> {


        @Override
        public boolean hasPrevious() {
            return cursor>0;
        }

        @Override
        public Row previous() {
            return null;
        }

        @Override
        public int nextIndex() {
            return 0;
        }

        @Override
        public int previousIndex() {
            return 0;
        }

        @Override
        public void remove() {

        }

        @Override
        public void set(Row row) {

        }

        @Override
        public void add(Row row) {

        }
    }
}
