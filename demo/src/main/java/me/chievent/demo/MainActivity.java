package me.chievent.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.chievent.recyclerview.FusionAdapter;

public class MainActivity extends AppCompatActivity {

    private MyAdapter1 mMyAdapter1 = new MyAdapter1();
    private FusionAdapter mAdapter = new FusionAdapter(mMyAdapter1);
    private MyAdapter2 mMyAdapter2 = new MyAdapter2();
    private boolean hasAddedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
    }

    public void addAdapter(View view) {
        if (!hasAddedAdapter) {
            mAdapter.addAdapter(mMyAdapter2);
            hasAddedAdapter = true;
        }
    }

    public void removeAdapter(View view) {
        if (hasAddedAdapter) {
            mAdapter.removeAdapter(mMyAdapter2);
            hasAddedAdapter = false;
        }
    }

    public void addItemInAdapter(View view) {
        mMyAdapter1.add();
    }

    public void removeItemInAdapter(View view) {
        mMyAdapter1.remove();
    }

    static class MyAdapter1 extends FusionAdapter.AbsAdapter {

        int oldCount = 10;
        int count = 10;

        void add() {
            oldCount = count++;
            updateData();
        }

        void remove() {
            oldCount = count--;
            updateData();
        }

        @Override
        public int getItemViewType(int position) {
            return position % 3;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case 0:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_1, parent, false));
                case 1:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_2, parent, false));
                case 2:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_3, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder textHolder = (ViewHolder) holder;
            textHolder.textView.setText("Position: " + position);
        }

        @Override
        public int getItemCount() {
            return count;
        }

        @Override
        public int getOldListSize() {
            return oldCount;
        }

        @Override
        public int getNewListSize() {
            return count;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItemPosition == newItemPosition;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItemPosition == newItemPosition;
        }

    }

    static class MyAdapter2 implements FusionAdapter.IAdapter {

        int oldCount = 10;
        int count = 10;

        FusionAdapter mFusionAdapter;

        void add() {
            oldCount = count++;
            mFusionAdapter.updateData();
        }

        void remove() {
            oldCount = count--;
            mFusionAdapter.updateData();
        }

        @Override
        public void onBindAdapter(FusionAdapter adapter) {
            mFusionAdapter = adapter;
        }

        @Override
        public void onUnbindAdapter(FusionAdapter adapter) {
            // ignore
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case 0:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_4, parent, false));
                case 1:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_5, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder textHolder = (ViewHolder) holder;
            textHolder.textView.setText("Position: " + position);
        }

        @Override
        public int getItemCount() {
            return count;
        }

        @Override
        public int getOldListSize() {
            return oldCount;
        }

        @Override
        public int getNewListSize() {
            return count;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItemPosition == newItemPosition;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItemPosition == newItemPosition;
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_view);
        }
    }
}
