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
import team.itis.ivm.helpers.FFmpegCommand;
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
                FFmpegCommand command = createCommand();
                FFmpegHelper.getInstance().executeFFmpeg(command, new ExecuteBinaryResponseHandler() {
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

    private FFmpegCommand createCommand() {
        return FFmpegCommand.newBuilder()
                .setInput("/sdcard/video.mp4")
                .addVideoFilter("drawtext", new String[]{
                        "fontsize=32",
                        "fontfile=/sdcard/bauhs.ttf",
                        "fontcolor=white",
                        "textfile=/sdcard/text.txt",
                        "y=h-line_h-25*t",
                        "x=w-100*t"
                })
                .addVideoFilter("fade", new String[]{
                        "t=in",
                        "d=5"
                })
                .addVideoFilter("fade", new String[]{
                        "t=out",
                        "st=10",
                        "d=5"
                })
                .setOutput("/sdcard/fadeInOut" + System.currentTimeMillis() + ".mp4")
                .build();
    }
}