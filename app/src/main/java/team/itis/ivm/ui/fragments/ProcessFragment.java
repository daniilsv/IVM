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

import eu.davidea.flipview.FlipView;
import team.itis.ivm.BuildConfig;
import team.itis.ivm.R;
import team.itis.ivm.data.Content;
import team.itis.ivm.helpers.FFmpegCommand;
import team.itis.ivm.helpers.FFmpegHelper;

public class ProcessFragment extends Fragment {

    private static final String COMMAND_LOG_TAG = "ffmpeg command";
    String output_file;
    ProgressBar pb;

    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static int getDurationFromString(String line) {
        String[] s = line.trim().replaceFirst(".*([0-9]{2}:[0-9]{2}:[0-9]{2}).*", "$1").split(":");
        return 60 * 60 * Integer.parseInt(s[0]) + 60 * Integer.parseInt(s[1]) + Integer.parseInt(s[2]);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View fragment_view = inflater.inflate(R.layout.fragment_process, container, false);
        final FlipView flipView = fragment_view.findViewById(R.id.flip_view);
        fragment_view.findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipView.showNext();
//                getContentInfo("/sdcard/video1.mp4");
//                getContentInfo("/sdcard/video2.mp4");
//                getContentInfo("/sdcard/image1.png");
//                getContentInfo("/sdcard/image2.jpg");
                pb = fragment_view.findViewById(R.id.wait_progress);
                pb.setMax(100);
                FFmpegCommand command = createCommand();
                FFmpegHelper.getInstance().executeFFmpeg(command, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onProgress(String message) {
                        if (message.startsWith("frame=")) {
                            pb.setProgress(100 * getDurationFromString(message) / 30);
                            Log.d(COMMAND_LOG_TAG, 100 * getDurationFromString(message) / 30 + "%");
                        }
                    }

                    @Override
                    public void onFinish() {
                        flipView.showNext();
                    }
                });

            }
        });
        fragment_view.findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                createInstagramIntent(output_file);
                flipView.flip(0, 0L);
            }
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

    private FFmpegCommand createCommand() {
        File movies_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        movies_dir.mkdirs();
        output_file = movies_dir.getPath() + "/output" + System.currentTimeMillis() + ".mp4";
        return FFmpegCommand.newBuilder()
                .addInput("/sdcard/video1.mp4")
                .addInput("/sdcard/video2.mp4")
                .addInput("/sdcard/audio1.mp3")

                .addComplexFilter("[0:v]", "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'", "crop=720:720", "split=2", "[input1a][input1b]")
                .addComplexFilter("[input1a]", "trim=start=2:end=20", "setpts=PTS-STARTPTS", "[clip1]")
                .addComplexFilter("[input1b]", "trim=start=20:end=22", "setpts=PTS-STARTPTS", "[clip1fadeoutsource]")

                .addComplexFilter("[1:v]", "scale=w='if(gt(a,1),-1,720):h=if(gt(a,1),720,-1)'", "crop=720:720", "split=2", "[input2a][input2b]")
                .addComplexFilter("[input2a]", "trim=start=388:end=390", "setpts=PTS-STARTPTS", "[clip2fadeinsource]")
                .addComplexFilter("[input2b]", "trim=start=390:end=400", "setpts=PTS-STARTPTS", "[clip2]")

                .addComplexFilter("[clip1fadeoutsource]", "format=pix_fmts=yuva420p",
                        "fade=t=out:st=0:d=2:alpha=1",
                        "[clip1fadeout]")
                .addComplexFilter("[clip2fadeinsource]", "format=pix_fmts=yuva420p",
                        "fade=t=in:st=0:d=2:alpha=1",
                        "[clip2fadein]")

                .addComplexFilter("[clip1fadeout]", "fifo", "[clip1fadeoutfifo]")
                .addComplexFilter("[clip2fadein]", "fifo", "[clip2fadeinfifo]")
                .addComplexFilter("[clip1fadeoutfifo]", "[clip2fadeinfifo]", "overlay", "[clip1to2crossfade]")

                .addComplexFilter("[clip1]", "[clip1to2crossfade]", "[clip2]", "concat=n=3",
                        "drawtext=fontsize=32:fontfile=/sdcard/plain.otf:fontcolor=red:textfile=/sdcard/text.txt:y=h-15*t:x=w-75*t",
                        "drawtext=fontsize=32:fontfile=/sdcard/cour.ttf:fontcolor=blue:textfile=/sdcard/text.txt:y=15*t:x=50*t",
                        "[output]")

                .addMap("[output]")
                .addMap("2")

                .setVideoCodec("mpeg4")
                .addParam("-vtag", "xvid")
                .addParam("-strict", "experimental")
                .addParam("-qscale:v", "3")
                .addParam("-r", "24")
                .addParam("-pix_fmt", "yuv420p")
                .addParam("-level", "3.0")

                .addParam("-shortest", null)

                .setOutput(output_file)
                .build();
    }

    private void createInstagramIntent(String mediaPath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("video/*");
        File media = new File(mediaPath);
        Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID, media);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share to"));
    }
}