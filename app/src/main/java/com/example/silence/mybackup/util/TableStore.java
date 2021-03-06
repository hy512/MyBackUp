package com.example.silence.mybackup.util;


import android.util.Log;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/*
    第 0 行数据表示表头（字段名称）
 */
@JsonSerialize(using = TableStoreSerializer.class)
@JsonDeserialize(using = TableStoreDeserializer.class)
public class TableStore extends SimpleList<TableStore.Row> implements Serializable, Cloneable {
    private static final long serialVersionUID = 100L;

    String[] head;
    Class[] type;
    int width;


    public TableStore(String[] head) {
        super();
        this.width = head.length;
        this.head = head;
    }

    public TableStore(String[] head, Class[] type) {
        this(head);
        this.type = type;
    }

    public TableStore(String[] head, Class[] type, Object[] data) {
        super(data);
        this.head = head;
        this.width = head.length;
        this.type = type;
    }


    public int getWidth() {
        return width;
    }

    // 自定义方法
    public String field(int index) {
        if (index < 0) index += width;
        if (index < 0 || index >= width)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        return head[index];
    }

    public int field(String title) {
        Objects.requireNonNull(title);
        for (int i = width - 1; i >= 0; i--)
            if (title.equals(head[i])) return i;
        return -1;
    }

    public String[] fields() {
        return Arrays.copyOf(head, width);
    }

    // 去除相等的数据
    // exclude 排除比较的列
    public void distinct(int[] exclude) {
        for (int r = size - 1; r >= 0; r--) {
            for (int p = r - 1, c; p >= 0; p--) {
                for ( c = width - 1; c >= 0; c--) {
                    // 排除的值，将不进行比较(表示默认相同)
                    if (Arrays.binarySearch(exclude, c) >= 0 ) {
                        continue;
                    }

                    // 有不相等的值，移动到下一行
                    if (retrieve(r, c) != null)
                        if (!retrieve(r, c).equals(retrieve(p, c))) break;
                    if (retrieve(r, c) == null && retrieve(p, c) != null) break;
                }
                // 判断为相等 (迭代全走完了，没有不同 break)，删除相同的行
                if (c == -1) {
                    // 删除 p 行
                    remove(p);
                    if (--r < 0) return;
                }
            }
        }
    }

    // 获取指定元素
    public <K extends Object> K retrieve(int row, int col) {
        if (row < 0) row += size;
        if (col < 0) col += width;
        if (row < 0 || row >= size || col < 0 || col >= width)
            throw new IndexOutOfBoundsException(String.format("row: %d, col: %d", row, col));
        if (get(row) == null) throw new NullPointerException("Why row ".concat(row + " is null?"));
        return (K) get(row).get(col);
    }

    public <K extends Object> K retrieve(int row, String title) {
        if (row < 0) row += size;
        if (row >= size || row < 0) throw new IndexOutOfBoundsException(outOfBoundsMsg(row, size));
        int col = field(title);
        if (col < 0) throw new IllegalStateException("没有 " + title);
        return retrieve(row, col);
    }

    // 获取指定行
    public Object[] retrieveRow(int index) {
        if (index < 0) index += size;
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        return get(index).toArray();
    }

    // 获取指定列
    public Object[] retrieveColumn(int index) {
        if (index < 0) index += width;
        if (index < 0 || index >= width)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        Object[] col = new Object[size];
        for (int i = 0; i < size; i++)
            col[i] = retrieve(i, index);
        return col;
    }

    // 获取指定列
    public Object[] retrieveColumn(String head) {
        int index = -1;
        if (head == null) {
            for (int i = 0; i < width; i++)
                if (this.head[i] == null) index = i;
        } else {
            for (int i = 0; i < width; i++)
                if (head.equals(this.head[i])) index = i;
        }
        if (index == -1) throw new IllegalStateException("没有这个 title.");
        return retrieveColumn(index);
    }

    // 插入行
    public void insertRow(Object[] r) {
        add(new Row(r, this.width));
    }

    public void insertRow(int index, Object[] r) {
        add(index, new Row(r, this.width));
    }

    // 插入空列
    public void insertColumnOfNull(int index, String h) {
        if (index < 0) index += width;
        if (index < 0 || index > width)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, width));

        // 插入 head
        int numMoved = width - index;
        if (numMoved > 0) {
            this.head = Arrays.copyOf(this.head, width + 1);
            System.arraycopy(this.head, index, this.head, index + 1, numMoved);
        }

        modCount++;
        this.head[index] = h;
        width++;
        // 插入内容
        for (Row r : this) {
            r.add(index, null);
        }
    }

    public boolean removeColumn(int index) {
        if (index < 0) index += width;
        if (index < 0 || index >= width)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        boolean modified = false;

        int numMoved = width - index - 1;
        if (numMoved >= 0) {
            System.arraycopy(head, index + 1, head, index, numMoved);
            head[width - 1] = null;
            for (int i = size - 1; i >= 0; i--) {
                get(i).remove(index);
            }

            modCount += size + 1;
            width--;
            modified = true;
        }
        return modified;
    }

    public boolean removeColumn(String field) {
        return removeColumn(field(field));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < width; i++)
            builder.append(head[i] + '\t');
        builder.append('\n');
        for (int i = 0; i < size; i++)
            builder.append((get(i) != null ? get(i).toString() : "null\n"));
        return builder.toString();
    }


    public static class Row extends SimpleList {
        public Row(Object[] data, int width) {
            super(data);
        }

        public <K extends Object> K retrieve(int index) {
            return (K) get(index);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null) {
                if (obj instanceof Object[]) {
                    return Arrays.equals(elementData, (Object[]) obj);
                }
                if (obj instanceof Row) {
                    return Arrays.equals(elementData, ((Row) obj).elementData);
                }
            }
            return false;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                builder.append((elementData[i] != null ? elementData[i].toString() : "null") + "\t");
            }
            builder.append('\n');
            return builder.toString();
        }
    }


    @Override
    public TableStore clone() throws CloneNotSupportedException {
//        return (TableStore) super.clone();
        return new TableStore(head, type, toArray());
    }
}

