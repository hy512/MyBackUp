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
    String[] head;
    Class[] type;
    Row[] body;
    int length;
    int width;
    private static final int minLength = 16;
    private AtomicBoolean operating;

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

    @Override
    public boolean add(Row row) {
        this.body[this.length] = row;
        if (this.length++ > this.body.length)
            grow();
        return true;
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
    public boolean addAll(int index, @NonNull Collection<? extends Row> c) {
        return false;
    }

    @Override
    public boolean addAll(@NonNull Collection c) {
        return false;
    }

    @Override
    public void clear() {
        this.length = 0;
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
    public void add(int index, Row element) {

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

    private void grow() {
        int newLength = this.length + this.length >> 1;
        if (newLength > Integer.MAX_VALUE)
            newLength = Integer.MAX_VALUE;
        if (newLength < this.minLength)
            newLength = this.minLength;
        for (int l = 0; l < width; l++)
            this.body = Arrays.copyOf(this.body, newLength);
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

    protected class Iter implements ListIterator<Row> {
        int cursor;
        int limit;

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

            return null;
        }

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
