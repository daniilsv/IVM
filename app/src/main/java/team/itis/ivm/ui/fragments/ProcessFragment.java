package team.itis.ivm.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import java.io.File;
import java.net.URLConnection;
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
    private String tmp_image_1;
    private String tmp_image_2;
    private String tmp_image_3;

    private String outFileName;

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

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

        tmp_image_1 = "/sdcard/v1.mp4";
        tmp_image_2 = "/sdcard/video1.mp4";
        tmp_image_3 = "/sdcard/v3.mp4";

        outFileName = movies_dir.getPath() + "/output" + System.currentTimeMillis() + ".mp4";

        float outDuration = 13;

        fragment_view.findViewById(R.id.start_button).setOnClickListener(view -> {
            flipView.showNext();
            FFmpegHelper.getInstance().executeFFmpeg(createCommand(outFileName, tmp_image_1, tmp_image_2, tmp_image_3), new ExecuteBinaryResponseHandler() {
                @Override
                public void onProgress(String message) {
                    if (message.startsWith("frame="))
                        pb.setProgress((int) (1000.f * getDurationFromString(message) / outDuration));

                }

                @Override
                public void onFinish() {
                    flipView.showNext();
                }
            });

        });
        fragment_view.findViewById(R.id.share_button).setOnClickListener(view -> {
            createInstagramIntent(outFileName);
            flipView.flip(0, 0L);
        });
        return fragment_view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public Content getContentInfo(String file) {
        final Content ret = new Content();
        ret.path = file;
        ret.isVideo = isVideoFile(file);
        FFmpegHelper.getInstance().executeFFmpeg("-i " + file, new ExecuteBinaryResponseHandler() {
            @Override
            public void onFailure(String message) {
                String[] lines = message.split("\n");
                for (String line : lines) {
                    if (ret.isVideo && ret.duration == -1 && line.contains("Duration")) {
                        ret.duration = getDurationFromString(line);
                    } else if (ret.resolution == null && line.contains("Stream #")) {
                        ret.resolution = line.trim().replaceFirst(".* ([0-9]+x[0-9]+).*", "$1");
                    }
                }
                Log.d(COMMAND_LOG_TAG, ret.toString());
            }
        });
        return ret;
    }

    private FFmpegCommand createImageCommand(String fileName, String outFileName) {
        return FFmpegCommand.newBuilder()
                .addParam("-f", "concat")
                .addParam("-safe", "0")
                .addInput(fileName)
                .addParam("-framerate", "1/5")
                .setVideoCodec("mpeg4")

                .setOutput(outFileName)
                .build(getContext());
    }

    private FFmpegCommand createCommand(String outFileName, String... videos) {
        FFmpegCommand.Builder builder = FFmpegCommand.newBuilder();
        for (String video : videos) {
            builder.addInput(video);
        }
        builder.addInput("/sdcard/audio1.mp3");
        for (int i = 0; i < videos.length; ++i) {
            if (i == 0)
                builder
                        .addComplexFilter("[" + i + ":v]",
                                "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'",
                                "crop=720:720",
                                "drawtext=fontsize=46:fontfile=/sdcard/plain.otf:fontcolor=red:textfile=/sdcard/text.txt:y=h-15*t:x=w-75*t",
                                "split=2", "[input" + i + "a][input" + i + "b]")
                        .addComplexFilter("[input" + i + "a]", "trim=start=0:end=4", "setpts=PTS-STARTPTS", "[clip" + i + "]")
                        .addComplexFilter("[input" + i + "b]", "trim=start=4:end=5", "setpts=PTS-STARTPTS", "[clip" + i + "fadeoutsource]")
                        .addComplexFilter("[clip" + i + "fadeoutsource]", "format=pix_fmts=yuva420p",
                                "fade=t=out:st=0:d=1:alpha=1",
                                "[clip" + i + "fadeout]")
                        .addComplexFilter("[clip" + i + "fadeout]", "fifo", "[clip" + i + "fadeoutfifo]");
            else if (i == videos.length - 1)
                builder
                        .addComplexFilter("[" + i + ":v]",
                                "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'",
                                "crop=720:720",
                                "drawtext=fontsize=46:fontfile=/sdcard/plain.otf:fontcolor=red:textfile=/sdcard/text.txt:y=h-15*t:x=w-75*t",
                                "split=2", "[input" + i + "a][input" + i + "b]")
                        .addComplexFilter("[input" + i + "a]", "trim=start=0:end=1", "setpts=PTS-STARTPTS", "[clip" + i + "fadeinsource]")
                        .addComplexFilter("[input" + i + "b]", "trim=start=1:end=5", "setpts=PTS-STARTPTS", "[clip" + i + "]")
                        .addComplexFilter("[clip" + i + "fadeinsource]", "format=pix_fmts=yuva420p",
                                "fade=t=in:st=0:d=1:alpha=1",
                                "[clip" + i + "fadein]")
                        .addComplexFilter("[clip" + i + "fadein]", "fifo", "[clip" + i + "fadeinfifo]")
                        .addComplexFilter("[clip" + (i - 1) + "fadeoutfifo]", "[clip" + i + "fadeinfifo]", "overlay", "[clip" + (i - 1) + "to" + i + "crossfade]");
            else
                builder
                        .addComplexFilter("[" + i + ":v]",
                                "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'",
                                "crop=720:720",
                                "drawtext=fontsize=46:fontfile=/sdcard/plain.otf:fontcolor=red:textfile=/sdcard/text.txt:y=h-15*t:x=w-75*t",
                                "split=3", "[input" + i + "a][input" + i + "b][input" + i + "c]")
                        .addComplexFilter("[input" + i + "a]", "trim=start=0:end=1", "setpts=PTS-STARTPTS", "[clip" + i + "fadeinsource]")
                        .addComplexFilter("[input" + i + "b]", "trim=start=1:end=4", "setpts=PTS-STARTPTS", "[clip" + i + "]")
                        .addComplexFilter("[input" + i + "c]", "trim=start=4:end=5", "setpts=PTS-STARTPTS", "[clip" + i + "fadeoutsource]")
                        .addComplexFilter("[clip" + i + "fadeoutsource]", "format=pix_fmts=yuva420p",
                                "fade=t=out:st=0:d=1:alpha=1",
                                "[clip" + i + "fadeout]")
                        .addComplexFilter("[clip" + i + "fadeout]", "fifo", "[clip" + i + "fadeoutfifo]")
                        .addComplexFilter("[clip" + i + "fadeinsource]", "format=pix_fmts=yuva420p",
                                "fade=t=in:st=0:d=1:alpha=1",
                                "[clip" + i + "fadein]")
                        .addComplexFilter("[clip" + i + "fadein]", "fifo", "[clip" + i + "fadeinfifo]")
                        .addComplexFilter("[clip" + (i - 1) + "fadeoutfifo]", "[clip" + i + "fadeinfifo]", "overlay", "[clip" + (i - 1) + "to" + i + "crossfade]");
        }
        ArrayList<String> out = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < videos.length; ++i) {
            if (i == 0)
                out.add("[clip" + i + "]");
            else {
                out.add("[clip" + (i - 1) + "to" + i + "crossfade]");
                out.add("[clip" + i + "]");
            }
        }
        out.add("concat=n=" + (videos.length * 2 - 1));
        out.add("[output]");
        String[] outArray = new String[out.size()];
        outArray = out.toArray(outArray);
        builder.addComplexFilter(outArray);
        return builder
                .addMap("[output]")
                .addMap("" + videos.length)

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

    }
}