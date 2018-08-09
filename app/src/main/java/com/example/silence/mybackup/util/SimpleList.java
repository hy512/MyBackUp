package com.example.silence.mybackup.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ArrayList;

public class SimpleList<T> implements List<T> {

    protected transient int modCount = 0;
    protected final static Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    protected final static Object[] EMPTY_ELEMENTDATA = {};
    protected final static int DEFAULT_CAPACITY = 10;
    protected final static int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    protected Object[] elementData;
    protected int size;

    public SimpleList() {
        elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    public SimpleList(Object[] a) {
        elementData = a;
        if ((size = elementData.length) != 0) {
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            elementData = EMPTY_ELEMENTDATA;
        }
    }

    @Override
    public boolean add(T row) {
        ensureCapacityInternal(size + 1);
        this.elementData[this.size++] = row;
        return true;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        ensureCapacityInternal(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        Object[] els = c.toArray();
        int numNew = els.length;
        ensureCapacityInternal(size + numNew);
        System.arraycopy(els, 0, elementData, size, numNew);
        size += numNew;
        return true;
    }

    public boolean addAll(T[] arr) {
        int numNew = arr.length;
        ensureCapacityInternal(size + numNew);
        System.arraycopy(arr, 0, elementData, size, numNew);
        size += numNew;
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));

        Object[] els = c.toArray();
        int numNew = els.length;
        ensureCapacityInternal(size + numNew);

        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);

        // 复制新元素
        System.arraycopy(elementData, index, els, 0, numNew);
        this.size += numNew;
        return numNew != 0;
    }

    @Override
    public void clear() {
        modCount++;
        for (int l = 0; l < size; l++)
            elementData[l] = null;
        this.size = 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) > 0;
    }

    @Override
    public boolean containsAll(Collection c) {
        for (Object e : c)
            if (!contains(e)) return false;
        return true;
    }

    @Override
    public T get(int index) {
        rangeCheck(index);
        return (T) elementData[index];
    }

    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (elementData[i] != null && elementData[i].equals(o)) return i;
                // if (o.equals(body[i])) return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator iterator() {
        return new Itr();
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null) return i;
            }
        } else for (int i = size - 1; i >= 0; i--)
            if (o.equals(elementData[i])) return i;
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ListItr(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        return new ListItr(index);
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index > -1 && index < size) {
            fastRemove(index);
            return true;
        }
        return false;
    }

    @Override
    public T remove(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
        modCount++;
        Object oldValue = elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--size] = null;
        return (T) oldValue;
    }

    @Override
    public boolean removeAll(Collection c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    @Override
    public boolean retainAll(Collection c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    @Override
    public T set(int index, T element) {
        rangeCheck(index);

        Object oldValue = elementData[index];
        elementData[index] = element;
        return (T) oldValue;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        throw new Error("不支持的操作.");
//        return null;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size) a[size] = null;
        return a;
    }


    private boolean batchRemove(Collection<? extends TableStore.Row> c, boolean complement) {
        final Object[] elements = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size(); r++) {
                if (c.contains(elements[r]) == complement)
                    elements[w++] = elements[r];
            }
        } finally {
            // c.contains() 异常，前面的迭代没有成功完成。
            if (r != size) {
                System.arraycopy(elements, r, elements, w, size - r);
                // 操作后的元素数量
                w += size - r;
            }
            if (w != size) {
                for (int i = w; i < size; i++)
                    elements[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }

        return modified;
    }

    private void ensureCapacityInternal(int minCapacity) {
        // 初始没有分配空间
        if (this.elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        if (minCapacity - elementData.length > 0) {
            grow(minCapacity);
        }
    }

    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + 1, numMoved);
        elementData[--size] = null;
    }

    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        this.elementData = Arrays.copyOf(this.elementData, newCapacity);
    }

    private int hugeCapacity(int minCapacity) {
        if (minCapacity < 0)
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }


    // 越界信息
    protected static String outOfBoundsMsg(int index, int size) {
        return "Index: " + index + ", Length: " + size;
    }

    private void rangeCheck(int index) {
        if (index >= size) throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
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


    protected class Itr implements Iterator<T> {
        int cursor;
        protected int limit = SimpleList.this.size;
        int lastRet = -1;

        int expectedModCount = modCount;


        @Override
        public boolean hasNext() {
            return cursor < limit;
        }

        @Override
        public T next() {
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            int i = cursor;
            if (i >= limit)
                throw new NoSuchElementException();
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (T) elementData[lastRet = i];
        }

        @Override
        public void remove() {
            if (lastRet > 0)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            try {
                SimpleList.this.remove(lastRet);
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

    protected class ListItr extends Itr implements ListIterator<T> {

        ListItr(int index) {
            super();
            cursor = index;

        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public T previous() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            int i = cursor - 1;
            if (i < 0) throw new NoSuchElementException();
            Object[] elements = SimpleList.this.elementData;
            if (i >= elements.length) throw new ConcurrentModificationException();
            cursor = i;
            return (T) elements[lastRet = i];
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
        public void set(T row) {
            if (lastRet < 0) throw new IllegalStateException();
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            try {
                SimpleList.this.set(lastRet, row);
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void add(T row) {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            try {
                int i = cursor;
                SimpleList.this.add(i, row);
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
