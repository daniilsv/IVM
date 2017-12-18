package team.itis.ivm.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.miguelgaeta.media_picker.MediaPicker;
import com.miguelgaeta.media_picker.MediaPickerRequest;

import java.io.File;
import java.io.IOException;

import team.itis.ivm.R;
import team.itis.ivm.data.Content;
import team.itis.ivm.ui.activities.MainActivity;
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
            ((TextView) itemView.findViewById(R.id.titleView)).setText(item.name);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment_view = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = fragment_view.findViewById(R.id.recycler_view);
        mAdapter = new UniversalAdapter<>(((MainActivity) getActivity()).getCurProject().getViewItems(), R.layout.row_content);
        mAdapter.setItemViewHolderCallback(ivhc);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Button mButton = fragment_view.findViewById(R.id.button_add);
        mButton.setOnClickListener(v -> MediaPicker.startForDocuments(ViewsFragment.this, Throwable::printStackTrace));
        return fragment_view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setData();
        mAdapter.setItems(((MainActivity) getActivity()).getCurProject().getViewItems());
        recyclerView.swapAdapter(mAdapter, false);
    }

    private void setData() {
        if (((MainActivity) getActivity()).getCurProject().getViewItems().size() != 0) return;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MediaPicker.handleActivityResult(getContext(), requestCode, resultCode, data, new MediaPicker.OnResult() {
            @Override
            public void onSuccess(File mediaFile, MediaPickerRequest request) {
                ((MainActivity) getActivity()).getCurProject().getViewItems().add(Content.createContent(getContext(), mediaFile.getAbsolutePath(), 10));
                mAdapter.setItems(((MainActivity) getActivity()).getCurProject().getViewItems());
                recyclerView.swapAdapter(mAdapter, true);
            }

            @Override
            public void onCancelled() {

            }

            @Override
            public void onError(IOException e) {
            }
        });
    }
}