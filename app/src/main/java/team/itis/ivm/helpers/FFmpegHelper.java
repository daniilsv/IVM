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
    private FFmpeg ffmpeg = null;

    private FFmpegHelper() {
    }

    public static FFmpegHelper getInstance() {
        if (instance == null)
            instance = new FFmpegHelper();
        return instance;
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

    public void executeFFmpeg(FFmpegCommand command, final ExecuteBinaryResponseHandler handler) {
        executeFFmpeg(command.toString(), handler);
    }

    public void executeFFmpeg(String cmd, final ExecuteBinaryResponseHandler handler) {
        if (ffmpeg == null)
            return;
        String[] command = cmd.split(" ");
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    if (handler != null) handler.onStart();
                    else Log.i(FFMPEG_LOG_TAG, "exe Finished");
                }

                @Override
                public void onProgress(String message) {
                    if (handler != null) handler.onProgress(message);
                    else Log.d(FFMPEG_LOG_TAG, "exe Progress: " + message);
                }

                @Override
                public void onFailure(String message) {
                    if (handler != null) handler.onFailure(message);
                    else Log.e(FFMPEG_LOG_TAG, "exe Failure: " + message);
                }

                @Override
                public void onSuccess(String message) {
                    if (handler != null) handler.onSuccess(message);
                    else Log.i(FFMPEG_LOG_TAG, "exe Success: " + message);
                }

                @Override
                public void onFinish() {
                    if (handler != null) handler.onFinish();
                    else Log.i(FFMPEG_LOG_TAG, "exe Finished");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.w(FFMPEG_LOG_TAG, "exe FFmpeg is already running");
        }
    }
}
