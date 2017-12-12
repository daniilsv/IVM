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
import team.itis.ivm.data.Sound;
import team.itis.ivm.ui.adapters.UniversalAdapter;

public class SoundsFragment extends Fragment {
    RecyclerView recyclerView;
    UniversalAdapter<Sound> mAdapter;
    UniversalAdapter.ItemViewHolder.ItemViewHolderCallback<Sound> ivhc = new UniversalAdapter.ItemViewHolder.ItemViewHolderCallback<Sound>() {
        @Override
        public void onClick(View itemView, Sound item) {

        }

        @Override
        public void setItem(View itemView, Sound item) {
            ((TextView) itemView.findViewById(R.id.titleView)).setText(item.path);
        }
    };
    private ArrayList<Sound> items = new ArrayList<>();

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
        items.add(new Sound("11111", 1, 2));
        items.add(new Sound("22221", 1, 2));
        items.add(new Sound("3332", 1, 2));
        items.add(new Sound("443", 1, 2));
        items.add(new Sound("54", 1, 2));
    }
}