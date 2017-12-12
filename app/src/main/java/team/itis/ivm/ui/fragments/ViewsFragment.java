package team.itis.ivm.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import team.itis.ivm.R;
import team.itis.ivm.data.Content;
import team.itis.ivm.ui.adapters.UniversalAdapter;

public class ViewsFragment extends Fragment {
    RecyclerView recyclerView;
    UniversalAdapter<Content> mAdapter;
    UniversalAdapter.ItemViewHolder.ItemViewHolderCallback<Content> ivhc = new UniversalAdapter.ItemViewHolder.ItemViewHolderCallback<Content>() {
        @Override
        public void onClick(View itemView, Content item) {

        }

        @Override
        public void setItem(View itemView, Content item) {
            ((TextView) itemView.findViewById(R.id.titleView)).setText(item.path);
        }
    };
    private ArrayList<Content> items = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment_view = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = fragment_view.findViewById(R.id.recycler_view);
        mAdapter = new UniversalAdapter<>(items, R.layout.row_project);
        mAdapter.setItemViewHolderCallback(ivhc);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return fragment_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setData();
        mAdapter.setItems(items);
        recyclerView.swapAdapter(mAdapter, false);
    }

    private void setData() {
        if(items.size() != 0)return;
        items.add(new Content("11111", true, 1, 2));
        items.add(new Content("22221", false, 1, 2));
        items.add(new Content("3332", true, 1, 2));
        items.add(new Content("443", false, 1, 2));
        items.add(new Content("54", true, 1, 2));
    }
}