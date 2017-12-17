package team.itis.ivm.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;

import eu.davidea.flipview.FlipView;
import team.itis.ivm.BuildConfig;
import team.itis.ivm.R;
import team.itis.ivm.data.Content;
import team.itis.ivm.helpers.FFmpegCommand;
import team.itis.ivm.helpers.FFmpegHelper;

public class ProcessFragment extends Fragment {

    private static final String COMMAND_LOG_TAG = "ffmpeg command";
    ProgressBar pb;
    ArrayList<Content> inputs;
    float outDuration = 0;
    private Content image_1;
    private Content video_2;
    private Content image_3;
    private String outFileName;
    private ArrayDeque<FFmpegCommand> convertQueue;

    public static float getDurationFromString(String line) {
        String[] s = line.trim().replaceFirst(".*([0-9]{2}):([0-9]{2}):([0-9]{2})\\.([0-9]{2}).*", "$1:$2:$3:$4").split(":");
        return 60 * 60 * Integer.parseInt(s[0]) + 60 * Integer.parseInt(s[1]) + Integer.parseInt(s[2]) + 0.01f * Integer.parseInt(s[3]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View fragment_view = inflater.inflate(R.layout.fragment_process, container, false);
        final FlipView flipView = fragment_view.findViewById(R.id.flip_view);
        pb = fragment_view.findViewById(R.id.wait_progress);
        pb.setMax(1000);

        File movies_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        movies_dir.mkdirs();

        convertQueue = new ArrayDeque<>();

        inputs = new ArrayList<>();
        inputs.add(Content.createContent(getContext(), "/sdcard/image1.png", 5));
        inputs.add(Content.createContent(getContext(), "/sdcard/video1.mp4", 5));
        inputs.add(Content.createContent(getContext(), "/sdcard/image3.jpg", 5));
        inputs.add(Content.createContent(getContext(), "/sdcard/video3.mp4", 5));
        inputs.add(Content.createContent(getContext(), "/sdcard/image2.jpg", 5));

        processInputs();

        Content[] input = inputs.toArray(new Content[inputs.size()]);

        outFileName = movies_dir.getPath() + "/output" + System.currentTimeMillis() + ".mp4";

        convertQueue.add(createCommand(outFileName, input).setHandler(new ExecuteBinaryResponseHandler() {
            @Override
            public void onProgress(String message) {
                if (message.startsWith("frame="))
                    pb.setProgress((int) (1000.f * getDurationFromString(message) / outDuration));
            }

            @Override
            public void onFinish() {
                flipView.showNext();
            }
        }));

        fragment_view.findViewById(R.id.start_button).setOnClickListener(view -> {
            flipView.showNext();
            convertNext();
        });

        fragment_view.findViewById(R.id.share_button).setOnClickListener(view -> {
            createInstagramIntent(outFileName);
            flipView.flip(0, 0L);
        });
        return fragment_view;
    }

    private void processInputs() {
        for (int i = 0; i < inputs.size(); ++i) {
            Content content = inputs.get(i);

            if (content.originalContent != null)
                convertQueue.add(createImageCommand(content));

            if (i == 0)
                outDuration += content.trimEnd - content.trimStart - content.fadeOutDuration;
            else if (i == inputs.size() - 1) {
                outDuration += content.trimEnd - content.trimStart - content.fadeInDuration;
                outDuration += Math.max(content.fadeInDuration, inputs.get(i - 1).fadeOutDuration);
            } else {
                outDuration += content.trimEnd - content.trimStart - content.fadeInDuration - content.fadeOutDuration;
                outDuration += Math.max(content.fadeInDuration, inputs.get(i - 1).fadeOutDuration);
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private FFmpegCommand createImageCommand(Content content) {
        return FFmpegCommand.newBuilder()
                .addParam("-framerate", "1/" + content.duration)
                .addInput(content.originalContent.path)
                .setVideoCodec("mpeg4")
                .setOutput(content.path)
                .build(getContext());
    }

    private FFmpegCommand createCommand(String outFileName, Content... inputs) {
        FFmpegCommand.Builder builder = FFmpegCommand.newBuilder();
        builder.addInput("/sdcard/audio1.mp3");
        for (Content input : inputs) {
            if (!input.isVideo)
                builder.addParam("-framerate", "1/" + input.duration);

            builder.addInput(input.path);
        }
        for (int i = 0; i < inputs.length; ++i) {
            Content content = inputs[i];
            if (i == 0)
                builder
                        .addComplexFilter("[" + (i + 1) + ":v]",
                                "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'",
                                "crop=720:720",
                                FFmpegCommand.generateDrawTextFilters(getContext(), content.texts),
                                "split=2", "[input" + i + "a][input" + i + "b]")
                        .addComplexFilter("[input" + i + "a]", "trim=start=" + content.trimStart + ":end=" + (content.trimEnd - content.fadeOutDuration), "setpts=PTS-STARTPTS", "[clip" + i + "]")
                        .addComplexFilter("[input" + i + "b]", "trim=start=" + (content.trimEnd - content.fadeOutDuration) + ":end=" + content.trimEnd, "setpts=PTS-STARTPTS", "[clip" + i + "fadeoutsource]")
                        .addComplexFilter("[clip" + i + "fadeoutsource]", "format=pix_fmts=yuva420p",
                                "fade=t=out:st=0:d=" + content.fadeOutDuration + ":alpha=1",
                                "[clip" + i + "fadeout]")
                        .addComplexFilter("[clip" + i + "fadeout]", "fifo", "[clip" + i + "fadeoutfifo]");
            else if (i == inputs.length - 1)
                builder
                        .addComplexFilter("[" + (i + 1) + ":v]",
                                "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'",
                                "crop=720:720",
                                FFmpegCommand.generateDrawTextFilters(getContext(), content.texts),
                                "split=2", "[input" + i + "a][input" + i + "b]")
                        .addComplexFilter("[input" + i + "a]", "trim=start=" + content.trimStart + ":end=" + (content.trimStart + content.fadeInDuration), "setpts=PTS-STARTPTS", "[clip" + i + "fadeinsource]")
                        .addComplexFilter("[input" + i + "b]", "trim=start=" + (content.trimStart + content.fadeInDuration) + ":end=" + content.trimEnd, "setpts=PTS-STARTPTS", "[clip" + i + "]")
                        .addComplexFilter("[clip" + i + "fadeinsource]", "format=pix_fmts=yuva420p",
                                "fade=t=in:st=0:d=" + content.fadeInDuration + ":alpha=1",
                                "[clip" + i + "fadein]")
                        .addComplexFilter("[clip" + i + "fadein]", "fifo", "[clip" + i + "fadeinfifo]")
                        .addComplexFilter("[clip" + (i - 1) + "fadeoutfifo]", "[clip" + i + "fadeinfifo]", "overlay", "[clip" + (i - 1) + "to" + i + "crossfade]");
            else
                builder
                        .addComplexFilter("[" + (i + 1) + ":v]",
                                "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'",
                                "crop=720:720",
                                FFmpegCommand.generateDrawTextFilters(getContext(), content.texts),
                                "split=3", "[input" + i + "a][input" + i + "b][input" + i + "c]")
                        .addComplexFilter("[input" + i + "a]", "trim=start=" + content.trimStart + ":end=" + (content.trimStart + content.fadeInDuration), "setpts=PTS-STARTPTS", "[clip" + i + "fadeinsource]")
                        .addComplexFilter("[input" + i + "b]", "trim=start=" + (content.trimStart + content.fadeInDuration) + ":end=" + (content.trimEnd - content.fadeOutDuration), "setpts=PTS-STARTPTS", "[clip" + i + "]")
                        .addComplexFilter("[input" + i + "c]", "trim=start=" + (content.trimEnd - content.fadeOutDuration) + ":end=" + content.trimEnd, "setpts=PTS-STARTPTS", "[clip" + i + "fadeoutsource]")
                        .addComplexFilter("[clip" + i + "fadeoutsource]", "format=pix_fmts=yuva420p",
                                "fade=t=out:st=0:d=" + content.fadeOutDuration + ":alpha=1",
                                "[clip" + i + "fadeout]")
                        .addComplexFilter("[clip" + i + "fadeout]", "fifo", "[clip" + i + "fadeoutfifo]")
                        .addComplexFilter("[clip" + i + "fadeinsource]", "format=pix_fmts=yuva420p",
                                "fade=t=in:st=0:d=" + content.fadeInDuration + ":alpha=1",
                                "[clip" + i + "fadein]")
                        .addComplexFilter("[clip" + i + "fadein]", "fifo", "[clip" + i + "fadeinfifo]")
                        .addComplexFilter("[clip" + (i - 1) + "fadeoutfifo]", "[clip" + i + "fadeinfifo]", "overlay", "[clip" + (i - 1) + "to" + i + "crossfade]");
        }
        ArrayList<String> out = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < inputs.length; ++i) {
            if (i == 0)
                out.add("[clip" + i + "]");
            else {
                out.add("[clip" + (i - 1) + "to" + i + "crossfade]");
                out.add("[clip" + i + "]");
            }
        }
        out.add("concat=n=" + (inputs.length * 2 - 1));
        out.add("[output]");
        String[] outArray = new String[out.size()];
        outArray = out.toArray(outArray);
        builder.addComplexFilter(outArray);
        return builder
                .addMap("[output]")
                .addMap("0")

                .setVideoCodec("mpeg4")
                .addParam("-vtag", "xvid")
                .addParam("-strict", "experimental")
                .addParam("-qscale:v", "5")
                .addParam("-r", "24")
                .addParam("-pix_fmt", "yuv420p")
                .addParam("-level", "3.0")

                .addParam("-shortest", null)

                .setOutput(outFileName)
                .build(getContext());
    }

    private void createInstagramIntent(String mediaPath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("video/*");
        File media = new File(mediaPath);
        Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, media);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share to"));
    }

    private void convertNext() {
        if (convertQueue.size() == 0)
            return;
        FFmpegCommand command = convertQueue.pop();
        FFmpegHelper.getInstance().executeFFmpeg(command, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
                command.getHandler().onSuccess(message);
            }

            @Override
            public void onProgress(String message) {
                command.getHandler().onProgress(message);
            }

            @Override
            public void onFailure(String message) {
                command.getHandler().onFailure(message);
            }

            @Override
            public void onStart() {
                command.getHandler().onStart();
            }

            @Override
            public void onFinish() {
                command.getHandler().onFinish();
                convertNext();
            }
        });
    }
}