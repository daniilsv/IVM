package team.itis.ivm.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import eu.davidea.flipview.FlipView;
import team.itis.ivm.R;
import team.itis.ivm.helpers.FFmpegHelper;

public class ProcessFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment_view = inflater.inflate(R.layout.fragment_process, container, false);
        final FlipView flipView = fragment_view.findViewById(R.id.flip_view);
        fragment_view.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipView.showNext();

                FFmpegHelper.getInstance().executeFFmpeg("-version", new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(getActivity().getApplicationContext(), "Success :)))", Toast.LENGTH_LONG).show();
                        flipView.showNext(4000);
                    }
                });

            }
        });
        fragment_view.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipView.flip(0, 0L);
            }
        });
        return fragment_view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}