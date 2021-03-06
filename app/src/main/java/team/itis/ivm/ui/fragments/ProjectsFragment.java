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
import team.itis.ivm.data.Project;
import team.itis.ivm.ui.adapters.UniversalAdapter;

public class ProjectsFragment extends Fragment {
    RecyclerView recyclerView;
    UniversalAdapter<Project> mAdapter;
    UniversalAdapter.ItemViewHolder.ItemViewHolderCallback<Project> ivhc = new UniversalAdapter.ItemViewHolder.ItemViewHolderCallback<Project>() {
        @Override
        public void onClick(View itemView, Project item) {

        }

        @Override
        public void setItem(View itemView, Project item) {
            ((TextView) itemView.findViewById(R.id.titleView)).setText(item.name);
        }
    };
    private ArrayList<Project> items = new ArrayList<>();

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
    }
}