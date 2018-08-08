package com.example.silence.mybackup.util;

import android.support.annotation.NonNull;

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
public class TableStore implements List<TableStore.Row>, Serializable {
    private static final long serialVersionUID = 100L;

    String[] head;
    Class[] type;
    Row[] body;
    int length;
    int width;
    private AtomicBoolean operating;

    protected transient int modCount = 0;
    private final static Row[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    private final static int DEFAULT_CAPACITY = 10;
    private final static int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    public TableStore() {
        this.body = new Row[DEFAULT_CAPACITY];
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
        ensureCapacityInternal(length + 1);
        this.body[this.length++] = row;
        return true;
    }
    @Override
    public void add(int index, Row element) {
        if (index < 0 || index > length)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        ensureCapacityInternal(length + 1);
        System.arraycopy(body, index, body, index + 1, length - index);
        body[index] = element;
        length++;
    }
    @Override
    public boolean addAll(Collection<? extends Row> c) {
        Object[] els = c.toArray();
        int numNew = els.length;
        ensureCapacityInternal(length+numNew);
        System.arraycopy(this.body, length, els, 0, numNew);
        length+=numNew;
        return true;
    }
    @Override
    public boolean addAll(int index, @NonNull Collection<? extends Row> c) {
        if (index < 0 || index > length) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));

        Object[] els = c.toArray();
        int numNew = els.length;
        ensureCapacityInternal(length + numNew);

        int numMoved = length - index;
        if (numMoved > 0) System.arraycopy(body, index, body, index+numNew, numMoved);

        // 复制新元素
        System.arraycopy(body, index, els, 0, numNew);
        this.length += numNew;
        return numNew != 0;
    }

    @Override
    public void clear() {
        modCount++;
        for (int l = 0; l < length; l++)
            body[l] = null;
        this.length = 0;
    }
    @Override
    public boolean contains(Object o) {
        return indexOf(o) > 0;
    }
    @Override
    public boolean containsAll(@NonNull Collection c) {
        for (Object e: c)
            if (!contains(e)) return false;
        return true;
    }
    @Override
    public Row get(int index) {
        rangeCheck(index);
        return body[index];
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < length; i++) {
                if (body[i] == null) return i;
            }
        } else {
            for (int i = 0; i < length; i++) {
                if (o.equals(body[i])) return i;
            }
        }
        return -1;
    }
    @Override
    public boolean isEmpty() {
        return length == 0;
    }
    @Override
    public Iterator iterator() {
        return new Itr();
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o==null) {
            for (int i=length-1; i>=0; i--) {
                if (body[i] == null) return i;
            }
        } else for (int i=length-1; i>=0; i--)
            if (o.equals(body[i])) return i;
        return -1;
    }

    @NonNull
    @Override
    public ListIterator<Row> listIterator() {
        return new ListItr(0);
    }

    @NonNull
    @Override
    public ListIterator<Row> listIterator(int index) {
        if (index < 0 || index > length) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        return new ListItr(index);
    }
    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index >-1 && index < length) {
            fastRemove(index);
            return true;
        }
        return false;
    }
    @Override
    public Row remove(int index) {
        if (index >= length)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        modCount++;
        Row oldValue = body[index];

        int numMoved = length-index-1;
        if (numMoved > 0)
            System.arraycopy(body, index+1, body, index, numMoved);
        body[--length] = null;
        return oldValue;
    }
    @Override
    public boolean removeAll(@NonNull Collection c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }
    @Override
    public boolean retainAll(@NonNull Collection c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }
    @Override
    public Row set(int index, Row element) {
        rangeCheck(index);

        Row oldValue = body[index];
        body[index] = element;
        return oldValue;
    }
    @Override
    public int size() {
        return length;
    }

    @NonNull
    @Override
    public List<Row> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, length);
        return null;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(body, length);
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < length)
            return (T[])Arrays.copyOf(body, length, a.getClass());
        System.arraycopy(body, 0, a, 0, length);
        if (a.length > length) a[length] = null;
        return a;
    }

    private boolean batchRemove(Collection<? extends Row> c, boolean complement) {
        final Object[] elements = this.body;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for(; r < size(); r ++) {
                if (c.contains(elements[r]) == complement)
                    elements[w++] = elements[r];
            }
        } finally {
            // c.contains() 异常，前面的迭代没有成功完成。
            if (r!= length) {
                System.arraycopy(elements, r, elements, w, length - r);
                // 操作后的元素数量
                w += length -r;
            }
            if (w != length) {
                for (int i = w; i< length; i++)
                    elements[i] = null;
                modCount += length - w;
                length = w;
                modified = true;
            }
        }

        return modified;
    }

    private void ensureCapacityInternal(int minCapacity) {
        // 初始没有分配空间
        if (this.body == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        if (minCapacity - body.length > 0) {
            grow(minCapacity);
        }
    }

    private void fastRemove(int index) {
        modCount++;
        int numMoved = length - index - 1;
        if (numMoved > 0)
        System.arraycopy(body, index, body, index +1, numMoved);
        body[--length] = null;
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

    private int hugeCapacity(int minCapacity) {
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }


    // 越界信息
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Length: " + this.length;
    }

    private void rangeCheck(int index) {
        if (index >= length)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException(String.format("fromIndex = %d", fromIndex));
        if (toIndex > size)
            throw new IndexOutOfBoundsException(String.format("toIndex = %d", toIndex));
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
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

    protected class Itr implements Iterator<Row> {
        int cursor;
        protected int limit = TableStore.this.length;
        int lastRet = -1;

        int expectedModCount = modCount;


        @Override
        public boolean hasNext() {
            return cursor < limit;
        }

        @Override
        public Row next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            int i = cursor;
            if (i >= limit)
                throw new NoSuchElementException();
            if (i >= body.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return body[lastRet = i];
        }

        @Override
        public void remove() {
            if (lastRet > 0)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            try {
                TableStore.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
                limit--;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

//        @Override
//        public void forEachRemaining(Consumer<? super Row> consumer) {
//            Objects.requireNonNull(consumer);
//            int size = TableStore.this.length;
//            int i = cursor;
//            if (i > size) return;
//            Row[] elements = body;
//            if (i >= body.length) throw new ConcurrentModificationException();
//
//            while (i != size && expectedModCount == modCount) consumer.accept(elements[i++]);
//            cursor = i;
//            lastRet = i - 1;
//            if (expectedModCount != modCount) throw new ConcurrentModificationException();
//        }
    }

    protected class ListItr extends Itr implements ListIterator<Row> {

        ListItr(int index) {
            super();
            cursor = index;

        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public Row previous() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            int i = cursor - 1;
            if (i < 0) throw new NoSuchElementException();
            Row[] elements = TableStore.this.body;
            if (i >= elements.length) throw new ConcurrentModificationException();
            cursor = i;
            return elements[lastRet = i];
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void set(Row row) {
            if (lastRet < 0) throw new IllegalStateException();
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            try {
                TableStore.this.set(lastRet, row);
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void add(Row row) {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            try {
                int i = cursor;
                TableStore.this.add(i, row);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
                limit++;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
