package com.example.silence.mybackup.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.silence.mybackup.entiry.MyContact;

import org.codehaus.jackson.map.ObjectMapper;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class SimpleStore<T> {
    private Object[][] store;
    private int length;
    private int width = 2;
    private static final int minLength = 16;

    public SimpleStore() {
        this.store = new Object[2][minLength];
    }

    public SimpleStore(@NonNull Object[][] store) {
        this.store = store;
        this.length = store[0].length;
        this.width = store.length;
    }

    // 添加一个元素
    public void put(T k, T v) {
        this.store[0][this.length] = k;
        this.store[1][this.length] = v;
        if (this.length++ >= this.store[0].length)
            grow();
    }

    public void remove(int i) {
        if (i < 0 || i >= length) throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
        int l = i;
        while (l < length) {
            for (int p = 0; p < width; p++)
                store[p][l] = store[p][l + 1];
            l++;
        }
        length--;
    }


    public Ent get(int i) {
        return new Ent(i);
    }

    public T[] getRow(int i) {
        Object[] row = new Object[width];
        for (int l = 0; l < row.length; l++)
            row[l] = store[l][i];
        return (T[]) row;
    }

    public T getKey(int i) {
        if (i > this.length || i < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
        return (T) this.store[0][i];
    }

    public T getValue(int i) {
        if (i > this.length || i < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(i));
        return (T) this.store[1][i];
    }

    // 是否为空
    public boolean isEmpty() {
        if (this.length == 0)
            return true;
        return false;
    }

    // 大小
    public int size() {
        return this.length;
    }

    public SimpleStore<T> concat(SimpleStore<T> s) {
        Object[][] newStore;
        // 写入较长的二维长度
        {
            int maxWidth = this.store.length > s.length ? this.store.length : s.length;
            newStore = new Object[maxWidth][];
        }
        // 复制一维数组数据
        int length = this.size() + s.size();
        for (int l = 0; l < newStore.length; l++) {
            newStore[l] = new Object[length];
            if (this.store[l] != null)
                System.arraycopy(this.store[l], 0, newStore[l], 0, this.size());
            else Arrays.fill(newStore, 0, this.size() - 1, null);
            if (s.store[l] != null)
                System.arraycopy(s.store[l], 0, newStore[l], this.size(), s.size());
            else Arrays.fill(newStore, this.size(), newStore[l].length - 1, null);
        }
        return new SimpleStore<T>(newStore);
    }

    // 迭代
    public Iterator<Map.Entry<T, T>> iterator() {
        return new Itr();
    }

    // 去除重复的数据
    public void distinct() {
        Log.d("--->", this.toString());
        for (int l = length - 1; l >= 0; l--) {
            for (int p = l - 1; p >= 0; p--) {
                for (int s = width - 1; s >= 0; s--) {
                    Log.d("-->", String.format("%d, %d, %d", l, p, s));
                    Log.d("-->", String.format("%s === %s",
                            store[s][l] == null? "null":store[s][l].toString(),
                            store[s][p] == null? "null":store[s][p].toString()));
                    if ((store[s][l] == null && store[s][p] == null) ||
                            (store[s][l] != null &&
                                    store[s][p] != null &&
                                    store[s][l].equals(store[s][p]))) {
                        if (s == 0) {
                            remove(p);
                            if (--l < 0) return;
                            Log.d("--->", "delete");
                        }
                    } else break;
                }
                Log.d("--->", "next");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{ ");
        Iterator<Map.Entry<T, T>> ite = this.iterator();
        while (ite.hasNext()) {
            Map.Entry<T, T> ent = ite.next();
            builder.append(ent.getKey() + "=" + ent.getValue() + ", ");
        }
        builder.delete(builder.length() - 2, builder.length()).append(" }");
        return builder.toString();
    }

    // 扩容
    private void grow() {
        int newLength = this.length + this.length >> 1;
        if (newLength > Integer.MAX_VALUE)
            newLength = Integer.MAX_VALUE;
        if (newLength < this.minLength)
            newLength = this.minLength;
        for (int l = 0; l < width; l++)
            this.store[l] = Arrays.copyOf(this.store[l], newLength);
//        this.store[1] = Arrays.copyOf(this.store[1], newLength);
    }

    // 越界信息
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Length: " + this.length;
    }

    // 迭代器
    private class Itr implements Iterator<Map.Entry<T, T>> {
        int cursor;
        int limit;

        {
            limit = SimpleStore.this.length;
            cursor = 0;
        }

        @Override
        public boolean hasNext() {
            if (cursor < limit)
                return true;
            return false;
        }

        @Override
        public Map.Entry<T, T> next() {
            int cur = this.cursor;
            if (this.cursor++ > this.limit)
                throw new NoSuchElementException();
            return new Ent(cur);
        }

        public void remove() {
            if (cursor < limit) {

            }
        }
    }

    // 键值对
    public class Ent implements Map.Entry<T, T> {
        int index;

        public Ent(int i) {
            this.index = i;
        }

        @Override
        public T getKey() {
            return (T) SimpleStore.this.store[0][this.index];
        }

        @Override
        public T getValue() {
            return (T) SimpleStore.this.store[1][this.index];
        }

        @Override
        public T setValue(T value) {
            return (T) (SimpleStore.this.store[0][this.index] = value);
        }

        @Override
        public boolean equals(Object obj) {
            // 类型不同
            if (obj == null || !(obj instanceof SimpleStore.Ent))
                return false;
            SimpleStore.Ent ent = (SimpleStore.Ent) obj;
            if (this.getKey() != null && this.getValue() != null &&
                    this.getKey().equals(ent.getKey()) && this.getValue().equals(ent.getValue()))
                return true;
            return false;
        }
    }
}
