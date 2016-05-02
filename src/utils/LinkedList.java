package utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by Artem on 27.03.2016.
 */
public class LinkedList<T extends Object> implements Iterable<T> {
    private static class Element<T> {
        private T value;
        private Element<T> next;
        private Element<T> previous;

        public Element(T value) {
            this.value = value;
            next = null;
            previous = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Element)) return false;

            Element<?> element = (Element<?>) o;

            return value != null ? value.equals(element.value) : element.value == null;

        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    private Element<T> head;
    private Element<T> tail;

    private Map<T, Element<T>> map;

    private int size;

    public LinkedList() {
        head = null;
        tail = null;

        map = new HashMap<>();

        size = 0;
    }

    public void add(T value) {
        Element<T> new_element = new Element<>(value);

        if (head == null) {
            new_element.next = new_element;
            new_element.previous = new_element;

            head = new_element;
            tail = new_element;
        } else {
            new_element.next = head;
            new_element.previous = tail;

            tail.next = new_element;
            head.previous = new_element;

            tail = new_element;
        }

        map.put(value, new_element);
        ++size;
    }

    public T removeFirst() {
        if (head == null)
            throw new NullPointerException("List is empty");

        T value;

        if (size == 1) {
            value = head.value;
            this.clear();

            return value;
        }

        value = head.value;

        head = head.next;
        tail.next = head;
        head.previous = tail;

        map.remove(value);
        --size;

        return value;
    }

    public T removeLast() {
        if (head == null)
            throw new NullPointerException("List is empty");

        T value;

        if (size == 1) {
            value = head.value;
            this.clear();

            return value;
        }

        value = tail.value;

        tail = tail.previous;
        tail.next = head;
        head.previous = tail;

        map.remove(value);
        --size;

        return value;
    }

    public T get(int index) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        Element<T> elem = getElementAtIndex(index);

        return elem.value;
    }

    public void remove(T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if(!map.containsKey(value))
            throw new NoSuchElementException("List does not contain value " + value);

        if (size == 1) {
            this.clear();
            return;
        }

        Element<T> element = map.get(value);

        if (element == head)
            head = head.next;

        if (element == tail)
            tail = tail.previous;

        element.previous.next = element.next;
        element.next.previous = element.previous;

        map.remove(value);
        --size;
    }

    public void insertAfter(T position, T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (map.containsKey(value))
            throw new IllegalArgumentException("List already contains value " + value);

        if (!map.containsKey(position))
            throw new NoSuchElementException("List does not contain value " + position);

        Element<T> new_element = new Element<>(value);
        Element<T> pos = map.get(position);

        new_element.previous = pos;
        new_element.next = pos.next;

        pos.next.previous = new_element;
        pos.next = new_element;

        map.put(value, new_element);
        ++size;
    }

    public void insertBefore(T position, T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (map.containsKey(value))
            throw new IllegalArgumentException("List already contains value " + value);

        if (!map.containsKey(position))
            throw new NoSuchElementException("List does not contain value " + position);

        Element<T> new_element = new Element<>(value);
        Element<T> pos = map.get(position);

        new_element.previous = pos.previous;
        new_element.next = pos;

        pos.previous.next = new_element;
        pos.previous = new_element;

        map.put(value, new_element);
        ++size;
    }

    public void insertAt(int index, T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (map.containsKey(value))
            throw new IllegalArgumentException("List already contains value " + value);

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        Element<T> element = getElementAtIndex(index - 1);

        Element<T> new_element = new Element<>(value);

        new_element.previous = element;
        new_element.next = element.next;

        element.next.previous = new_element;
        element.next = new_element;

        map.put(value, new_element);
        ++size;
    }

    public T removeAt(int index) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        Element<T> element = getElementAtIndex(index - 1);

        T value = element.next.value;

        element.next = element.next.next;
        element.next.previous = element;

        map.remove(value);
        --size;

        return value;
    }

    public T next(T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (!map.containsKey(value))
            throw new NoSuchElementException("List does not contain value " + value);

        Element<T> element = map.get(value);
        return element.next.value;
    }

    public T previous(T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (!map.containsKey(value))
            throw new NoSuchElementException("List does not contain value " + value);

        Element<T> element = map.get(value);
        return element.previous.value;
    }

    public T getFirst() {
        if (head == null)
            throw new NullPointerException("List is empty");

        return head.value;
    }

    public T getLast() {
        if (head == null)
            throw new NullPointerException("List is empty");

        return tail.value;
    }

    public int indexOf(T value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (!map.containsKey(value))
            return -1;

        Element<T> cur = head;
        int index = 0;

        while (!cur.value.equals(value)) {
            cur = cur.next;
            ++index;
        }

        return index;
    }

    public void replace(T old_value, T new_value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (!map.containsKey(old_value))
            throw new IllegalArgumentException("No such element " + old_value);

        Element<T> index = map.get(old_value);
        index.value = new_value;

        map.remove(old_value);
        map.put(new_value, index);
    }

    public void replaceAt(int index, T new_value) {
        if (head == null)
            throw new NullPointerException("List is empty");

        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        Element<T> elem = getElementAtIndex(index);
    }

    public int size() {
        return size;
    }

    public boolean contains(T value) {
        return map.containsKey(value);
    }

    public void clear() {
        head = null;
        tail = null;

        map.clear();
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkedList)) return false;

        LinkedList<?> that = (LinkedList<?>) o;

        if (size != that.size) return false;
        if (head != null ? !head.equals(that.head) : that.head != null) return false;
        if (tail != null ? !tail.equals(that.tail) : that.tail != null) return false;
        return map != null ? map.equals(that.map) : that.map == null;

    }

    @Override
    public int hashCode() {
        int result = head != null ? head.hashCode() : 0;
        result = 31 * result + (tail != null ? tail.hashCode() : 0);
        result = 31 * result + (map != null ? map.hashCode() : 0);
        result = 31 * result + size;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        Element<T> cur = head;

        for (int i = 0; i < size; ++i) {
            text.append(cur);
            text.append(" ");

            cur = cur.next;
        }

        return text.toString();
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<T> iter = new Iterator<T>() {

            private Element<T> current = head;
            private int counter = 0;

            @Override
            public boolean hasNext() {
                return counter < size;
            }

            @Override
            public T next() {
                T value = current.value;
                current = current.next;

                ++counter;

                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        return iter;
    }

    private Element<T> getElementAtIndex(int index) {
        Element<T> current = head;

        for (int i = 0; i <= index; ++i) {
            current = current.next;
        }

        return current;
    }
}
