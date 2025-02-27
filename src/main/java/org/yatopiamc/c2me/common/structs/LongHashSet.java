package org.yatopiamc.c2me.common.structs;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class LongHashSet implements LongSet {

    private final HashSet<Long> delegate = new HashSet<>();

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public @Nonnull
    LongIterator iterator() {
        final Iterator<Long> iterator = delegate.iterator();
        return new LongIterator() {
            @Override
            public long nextLong() {
                return iterator.next();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Nonnull
    @Override
    public <T> T[] toArray(@Nonnull T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends Long> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean add(long key) {
        return delegate.add(key);
    }

    @Override
    public boolean contains(long key) {
        return delegate.contains(key);
    }

    @Override
    public long[] toLongArray() {
        return delegate.stream().mapToLong(value -> value).toArray();
    }

    @Override
    public long[] toLongArray(long[] a) {
        final long[] longs = toLongArray();
        for (int i = 0; i < longs.length && i < a.length; i++) {
            a[i] = longs[i];
        }
        return a;
    }

    @Override
    public long[] toArray(long[] a) {
        return toLongArray(a);
    }

    @Override
    public boolean addAll(LongCollection c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean containsAll(LongCollection c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean removeAll(LongCollection c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(LongCollection c) {
        return delegate.retainAll(c);
    }

    @Override
    public boolean remove(long k) {
        return delegate.remove(k);
    }
}
