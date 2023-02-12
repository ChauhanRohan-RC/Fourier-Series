/*
 * Copyright (c) 1998, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package provider;

import misc.CollectionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A List of {@link FunctionProviderI function providers}
 */
public class FunctionProviderList extends AbstractListModel<FunctionProviderI> implements MutableComboBoxModel<FunctionProviderI> {

    private static final boolean DEFAULT_ENSURE = true;

    @NotNull
    private final List<FunctionProviderI> providers;
    private int mSelectedIndex = -1;

    public FunctionProviderList() {
        providers = Collections.synchronizedList(new ArrayList<>());
    }

    public FunctionProviderList(@NotNull Collection<FunctionProviderI> initialProviders, int selectedIndex) {
        providers = Collections.synchronizedList(new ArrayList<>(initialProviders.size() + 4));
        CollectionUtil.addAll(initialProviders, providers);

        setSelectedIndex(selectedIndex);
    }

    @Override
    public int getSize() {
        return providers.size();
    }

    public boolean isEmpty() {
        return providers.isEmpty();
    }

    public boolean isIndexInvalid(int index) {
        return index < 0 || index >= getSize();
    }

    public boolean noSelection() {
        return isIndexInvalid(mSelectedIndex);
    }

    private void checkThrowIndex(int index) throws IndexOutOfBoundsException {
        if (isIndexInvalid(index))
            throw new IndexOutOfBoundsException(index);
    }

    @NotNull
    public FunctionProviderI get(int index) throws IndexOutOfBoundsException {
        return providers.get(index);
    }

    @Nullable
    @Override
    public FunctionProviderI getElementAt(int index) {
        if (isIndexInvalid(index))
            return null;
        return get(index);
    }

    public int index(@NotNull Object fp) {
        return providers.indexOf(fp);
    }

    public boolean contains(@NotNull FunctionProviderI fp) {
        return providers.contains(fp);
    }

    @NotNull
    public Predicate<FunctionProviderI> notContainFilter() {
        return fp -> !contains(fp);
    }

    private void considerAutoSelect() {
        if (noSelection()) {
            setSelectedIndex(0);
        }
    }

    public boolean add(@NotNull FunctionProviderI fp, boolean ensure) {
        if (ensure && contains(fp))
            return false;

        final int index = getSize();
        providers.add(fp);
        fireIntervalAdded(this, index, index);

        // Auto select first
        if (getSize() == 1) {
            considerAutoSelect();
        }

        return true;
    }

    public boolean add(@NotNull FunctionProviderI fp) {
        return add(fp, DEFAULT_ENSURE);
    }

    public boolean add(int index, @NotNull FunctionProviderI fp, boolean ensure) throws IndexOutOfBoundsException {
        if (ensure && contains(fp))
            return false;

        providers.add(index, fp);
        fireIntervalAdded(this, index, index);

        // Auto select first
        if (getSize() == 1) {
            considerAutoSelect();
        }
        return true;
    }

    public boolean add(int index, @NotNull FunctionProviderI fp) throws IndexOutOfBoundsException {
        return add(index, fp, DEFAULT_ENSURE);
    }

    public int addAll(Collection<? extends FunctionProviderI> c, boolean ensure) {
        if (CollectionUtil.isEmpty(c)) {
            return 0;
        }

        if (ensure) {
            c = c.stream().filter(notContainFilter()).collect(Collectors.toList());
            if (c.isEmpty())
                return 0;
        }

        final int startIndex = getSize();
        providers.addAll(c);
        final int newSize = getSize();
        fireIntervalAdded(this, startIndex, newSize - 1);

        // Auto select first
        if (startIndex == 0) {
            considerAutoSelect();
        }

        return newSize - startIndex;
    }

    public int addAll(Collection<? extends FunctionProviderI> c) {
        return addAll(c, DEFAULT_ENSURE);
    }

    public int addAll(int index, Collection<? extends FunctionProviderI> c, boolean ensure) throws IndexOutOfBoundsException {
        if (CollectionUtil.isEmpty(c)) {
            return 0;
        }

        if (ensure) {
            c = c.stream().filter(notContainFilter()).collect(Collectors.toList());
            if (c.isEmpty())
                return 0;
        }

        final int prevSize = getSize();
        providers.addAll(index, c);
        fireIntervalAdded(this, index, index + c.size() - 1);

        // Auto select first
        if (prevSize == 0) {
            considerAutoSelect();
        }

        return c.size();
    }

    public int addAll(int index, Collection<? extends FunctionProviderI> c) throws IndexOutOfBoundsException {
        return addAll(index, c, DEFAULT_ENSURE);
    }

    @NotNull
    public FunctionProviderI remove(int index) throws IndexOutOfBoundsException {
        checkThrowIndex(index);

        if (mSelectedIndex == index) {
            setSelectedIndex(index + (index == 0? 1: -1));
        }

        final FunctionProviderI prev = providers.remove(index);
        fireIntervalRemoved(this, index, index);
        return prev;
    }

    public boolean remove(Object obj) {
        final int index = index(obj);
        if (index == -1)
            return false;

        remove(index);
        return true;
    }



    @NotNull
    public List<FunctionProviderI> getAll() {
        return CollectionUtil.arrayListCopy(providers);
    }

    @NotNull
    public List<FunctionProviderI> getAll(@Nullable Predicate<FunctionProviderI> filter) {
        if (filter == null) {
            return getAll();
        }

        return providers.stream().filter(filter).collect(Collectors.toList());
    }

    public int removeIf(@NotNull Predicate<FunctionProviderI> filter) {
        int remCount = 0;

        for (int i = 0; i < getSize();) {
            final FunctionProviderI fp = get(i);
            if (filter.test(fp)) {
                remove(i);
                remCount++;
            } else {
                i++;
            }
        }

        return remCount;
    }

    public int clear() {
        final int size = getSize();
        setSelectedIndex(-1);
        if (size == 0)
            return 0;

        providers.clear();
        fireIntervalRemoved(this, 0, size - 1);
        return size;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if (isIndexInvalid(selectedIndex))
            selectedIndex = -1;

        if (mSelectedIndex == selectedIndex)
            return;

        mSelectedIndex = selectedIndex;
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public void setSelectedItem(@Nullable Object item) {
        final int index = item != null? index(item): -1;
        setSelectedIndex(index);
    }

    @Nullable
    @Override
    public FunctionProviderI getSelectedItem() {
        return getElementAt(mSelectedIndex);
    }


    public void ensureAddSelect(@NotNull FunctionProviderI fp) {
        int index = index(fp);
        if (index == -1) {
            index = getSize();
            add(fp);
        }

        setSelectedIndex(index);
    }




    @Override
    public void addElement(FunctionProviderI item) {
        add(item);
    }

    @Override
    public void removeElement(Object obj) {
        remove(obj);
    }

    @Override
    public void insertElementAt(FunctionProviderI item, int index) {
        add(index, item);
    }

    @Override
    public void removeElementAt(int index) {
        remove(index);
    }


    public record Stats(@NotNull Map<FunctionType, Integer> countMap, int noDefinitionFunctionsCount) {
    }

    @NotNull
    public Stats getStats() {
        final EnumMap<FunctionType, Integer> countMap = new EnumMap<>(FunctionType.class);
        int noDefCount = 0;

        for (FunctionProviderI fp: providers) {
            final FunctionMeta meta = fp.getFunctionMeta();
            final FunctionType ft = meta.functionType();
            Integer count = countMap.get(ft);
            int newCount;
            if (count == null) {
                newCount = 1;
            } else {
                newCount = count + 1;
            }

            countMap.put(ft, newCount);

            if (!meta.hasBaseDefinition()) {
                noDefCount++;
            }
        }

        return new Stats(countMap, noDefCount);
    }

}
