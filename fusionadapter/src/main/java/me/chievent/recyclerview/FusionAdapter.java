/*
 * Copyright (C) 2017 Chievent (chievent@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.chievent.recyclerview;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * This Adapter provides a convenient way to add items with different data and styles
 *
 * @author chievent
 * @version 1.0
 */
public class FusionAdapter extends RecyclerView.Adapter {

    private int mCounter;
    private final List<AdapterWrapper> mOldAdapterWrapperList = new ArrayList<>();
    private final List<AdapterWrapper> mAdapterWrapperList = new ArrayList<>();
    private final DiffUtil.Callback mDiffCallback = new DiffCallback();

    public FusionAdapter(IAdapter baseAdapter) {
        if (baseAdapter instanceof AbsAdapter) {
            ((AbsAdapter) baseAdapter).mAdapter = this;
        }
        mAdapterWrapperList.add(new AdapterWrapper(baseAdapter));
        mOldAdapterWrapperList.addAll(mAdapterWrapperList);
    }

    public void addAdapter(int index, IAdapter adapter) {
        checkAdapter(adapter);
        adapter.onBindAdapter(this);
        mOldAdapterWrapperList.clear();
        mOldAdapterWrapperList.addAll(mAdapterWrapperList);
        mAdapterWrapperList.add(index, new AdapterWrapper(adapter));
        updateData();
    }

    public void addAdapter(IAdapter adapter) {
        addAdapter(mAdapterWrapperList.size(), adapter);
    }

    public boolean removeAdapter(int index) {
        mOldAdapterWrapperList.clear();
        mOldAdapterWrapperList.addAll(mAdapterWrapperList);
        AdapterWrapper wrapper = mAdapterWrapperList.remove(index);
        if (wrapper != null) {
            wrapper.adapter.onUnbindAdapter(this);
            updateData();
            return true;
        }

        return false;
    }

    public boolean removeAdapter(IAdapter adapter) {
        mOldAdapterWrapperList.clear();
        mOldAdapterWrapperList.addAll(mAdapterWrapperList);
        for (AdapterWrapper wrapper : mAdapterWrapperList) {
            if (wrapper.adapter == adapter) {
                boolean result = mAdapterWrapperList.remove(wrapper);
                if (result) {
                    wrapper.adapter.onUnbindAdapter(this);
                    updateData();
                    return true;
                }
                break;
            }
        }

        return false;
    }

    public int getAdapterCount() {
        return mAdapterWrapperList.size();
    }

    public IAdapter getAdapter(int index) {
        return mAdapterWrapperList.get(index).adapter;
    }

    @Override
    public int getItemViewType(int position) {
        AdapterWrapper wrapper = getItem(mAdapterWrapperList, position);
        int type = wrapper.adapter.getItemViewType(position);
        if ((type & 0xffff0000) != 0) {
            throw new RuntimeException(wrapper.adapter.getClass().getName() + " has invalid item view type: " + wrapper);
        }
        type = wrapper.flag << 16 & 0xffff0000 | type;
        return type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (AdapterWrapper wrapper : mAdapterWrapperList) {
            if (viewType >> 16 == wrapper.flag) {
                return wrapper.adapter.onCreateViewHolder(parent, viewType & 0x0000ffff);
            }
        }

        throw new RuntimeException("Unknown viewType: " + Integer.toHexString(viewType));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AdapterWrapper wrapper = getItem(mAdapterWrapperList, position);
        wrapper.adapter.onBindViewHolder(holder, wrapper.convertedPosition);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (AdapterWrapper wrapper : mAdapterWrapperList) {
            count += wrapper.adapter.getItemCount();
        }
        return count;
    }

    private void checkAdapter(IAdapter adapter) {
        for (AdapterWrapper wrapper : mAdapterWrapperList) {
            if (wrapper.adapter == adapter) {
                throw new IllegalArgumentException("this adapter is already in");
            }
        }
    }

    /**
     * This method must be call after data set changes
     */
    public void updateData() {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(mDiffCallback);
        mOldAdapterWrapperList.clear();
        mOldAdapterWrapperList.addAll(mAdapterWrapperList);
        result.dispatchUpdatesTo(this);
    }

    private static AdapterWrapper getItem(List<AdapterWrapper> adapterWrapperList, int position) {
        AdapterWrapper result = optItem(adapterWrapperList, position);
        if (result == null) {
            throw new RuntimeException("Invalid position: " + position);
        }
        return result;
    }

    private static AdapterWrapper optItem(List<AdapterWrapper> adapterWrapperList, int position) {
        int start = 0;
        for (AdapterWrapper wrapper : adapterWrapperList) {
            IAdapter adapter = wrapper.adapter;
            position -= start;
            start += adapter.getItemCount();
            if (position < adapter.getItemCount()) {
                wrapper.convertedPosition = position;
                return wrapper;
            }
        }
        return null;
    }

    private class DiffCallback extends DiffUtil.Callback {
        @Override
        public int getOldListSize() {
            int count = 0;
            for (AdapterWrapper wrapper : mOldAdapterWrapperList) {
                count += wrapper.adapter.getOldListSize();
            }
            return count;
        }

        @Override
        public int getNewListSize() {
            int count = 0;
            for (AdapterWrapper wrapper : mAdapterWrapperList) {
                count += wrapper.adapter.getNewListSize();
            }
            return count;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            AdapterWrapper oldItemWrapper = optItem(mOldAdapterWrapperList, oldItemPosition);
            AdapterWrapper newItemWrapper = optItem(mAdapterWrapperList, newItemPosition);

            return !(oldItemWrapper == null || newItemWrapper == null)
                    && oldItemWrapper == newItemWrapper
                    && oldItemWrapper.adapter.areItemsTheSame(oldItemPosition, newItemPosition);

        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            AdapterWrapper oldItemWrapper = optItem(mOldAdapterWrapperList, oldItemPosition);
            AdapterWrapper newItemWrapper = optItem(mAdapterWrapperList, newItemPosition);

            return !(oldItemWrapper == null || newItemWrapper == null)
                    && oldItemWrapper == newItemWrapper
                    && oldItemWrapper.adapter.areContentsTheSame(oldItemPosition, newItemPosition);
        }
    }

    private class AdapterWrapper {
        final int flag;
        final IAdapter adapter;
        int convertedPosition;

        public AdapterWrapper(IAdapter adapter) {
            this.flag = mCounter++ % 0x0000ffff;
            this.adapter = adapter;
        }
    }

    public interface IAdapter {

        void onBindAdapter(FusionAdapter adapter);

        void onUnbindAdapter(FusionAdapter adapter);

        int getItemViewType(int position);

        RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

        void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

        int getItemCount();

        int getOldListSize();

        int getNewListSize();

        boolean areItemsTheSame(int oldItemPosition, int newItemPosition);

        boolean areContentsTheSame(int oldItemPosition, int newItemPosition);
    }

    public static abstract class AbsAdapter implements IAdapter {
        FusionAdapter mAdapter;

        @Override
        public void onBindAdapter(FusionAdapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public void onUnbindAdapter(FusionAdapter adapter) {
            // ignore
        }

        public void updateData() {
            mAdapter.updateData();
        }
    }
}
