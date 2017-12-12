package team.itis.ivm.helpers;

import android.content.Context;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

public class FFmpegHelper {

    private static final String FFMPEG_LOG_TAG = "ivm_ffmpeg";
    private static FFmpegHelper instance = null;
    FFmpeg ffmpeg = null;

    private FFmpegHelper() {
    }

    public static FFmpegHelper getInstance() {
        if (instance == null)
            instance = new FFmpegHelper();
        return instance;
    }

    private void run() {
        String cmd = "-i /sdcard/video.mp4 " +
                "-vcodec mpeg4 " +
                "-acodec copy " +
                "-ar 48000 " +
                "-ab 192k " +
                "-movflags faststart " +
                "-vf " +

                "drawtext=" +
                    "fontsize=32:" +
                    "fontfile=/sdcard/bauhs.ttf:" +
                    "fontcolor=white:" +
                    "textfile=/sdcard/text.txt:" +
                    "y=h-line_h-25*t:" +
                    "x=w-100*t," +
                "fade=t=in:d=5," +
                "fade=t=out:st=10:d=5" +

                " /sdcard/fadeInOut" + System.currentTimeMillis() + ".mp4";
        executeFFmpeg(cmd, null);
    }

    public void initFFmpeg(Context context) {
        if (ffmpeg == null)
            ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.i(FFMPEG_LOG_TAG, "Started");
                }

                @Override
                public void onFailure() {
                    Log.e(FFMPEG_LOG_TAG, "Failure");
                }

                @Override
                public void onSuccess() {
                    Log.i(FFMPEG_LOG_TAG, "Success");
                }

                @Override
                public void onFinish() {
                    Log.i(FFMPEG_LOG_TAG, "Finished");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.w(FFMPEG_LOG_TAG, "FFmpeg does not supported");
        }
    }

    //TODO:Заставить принимать FFmpegCommand
    public void executeFFmpeg(String cmd, final ExecuteBinaryResponseHandler handler) {
        if (ffmpeg == null)
            return;
        String[] command = cmd.split(" ");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.i(FFMPEG_LOG_TAG, "exe Finished");
                    if (handler != null) handler.onStart();
                }

                @Override
                public void onProgress(String message) {
                    Log.d(FFMPEG_LOG_TAG, "exe Progress: " + message);
                    if (handler != null) handler.onProgress(message);
                }

                @Override
                public void onFailure(String message) {
                    Log.e(FFMPEG_LOG_TAG, "exe Failure: " + message);
                    if (handler != null) handler.onFailure(message);
                }

                @Override
                public void onSuccess(String message) {
                    Log.i(FFMPEG_LOG_TAG, "exe Success: " + message);
                    if (handler != null) handler.onSuccess(message);
                }

                @Override
                public void onFinish() {
                    Log.i(FFMPEG_LOG_TAG, "exe Finished");
                    if (handler != null) handler.onFinish();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.w(FFMPEG_LOG_TAG, "exe FFmpeg is already running");
        }
    }
}
