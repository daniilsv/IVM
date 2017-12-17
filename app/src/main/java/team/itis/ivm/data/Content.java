package team.itis.ivm.data;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

import team.itis.ivm.helpers.FFmpegHelper;

import static team.itis.ivm.ui.fragments.ProcessFragment.getDurationFromString;

public class Content {
    public Content originalContent = null;
    public String name;
    public String path;
    public boolean isVideo;
    public float trimStart;
    public float trimEnd;
    public float fadeInDuration;
    public float fadeOutDuration;
    public float duration;
    public String resolution;
    public ArrayList<Text> texts = new ArrayList<>();

    public Content() {
        isVideo = true;
        duration = -1;
        resolution = null;
    }

    public Content(String path, boolean isVideo, float trimStart, float trimEnd) {
        this.path = path;
        if (originalContent == null) {
            int pos = path.lastIndexOf("/");
            this.name = path.substring(pos + 1);
        } else {
            int pos = originalContent.path.lastIndexOf("/");
            this.name = originalContent.path.substring(pos);
        }
        this.isVideo = isVideo;
        this.trimStart = trimStart;
        this.trimEnd = trimEnd;
        this.duration = trimEnd - trimStart;
        this.fadeInDuration = 1;
        this.fadeOutDuration = 1;
    }

    public static Content createContent(Context context, String path, float duration) {
        final Content ret;
        if (isVideoFile(path)) {
            ret = new Content(path, true, 0, duration);
            ret.texts.add(new Text("I'm a video file\n" + path, 46, "150*t", "h/2+2*line_h"));
            FFmpegHelper.getInstance().executeFFmpeg("-i " + path, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String message) {
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        if (ret.duration == -1 && line.contains("Duration")) {
                            ret.duration = getDurationFromString(line);
                        } else if (ret.resolution == null && line.contains("Stream #")) {
                            ret.resolution = line.trim().replaceFirst(".* ([0-9]+x[0-9]+).*", "$1");
                        }
                    }
                }
            });
        } else if (isImageFile(path)) {
            Random r = new Random();
            ret = new Content(context.getExternalCacheDir() + "/" + r.nextLong() + ".mp4", true, 0, duration);
            ret.originalContent = new Content(path, false, 0, duration);
            ret.texts.add(new Text("I'm a image file\n" + path, 46, "w-150*t", "h/2+2*line_h"));
            FFmpegHelper.getInstance().executeFFmpeg("-i " + path, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String message) {
                    String[] lines = message.split("\n");
                    for (String line : lines) {
                        if (ret.resolution == null && line.contains("Stream #")) {
                            ret.resolution = line.trim().replaceFirst(".* ([0-9]+x[0-9]+).*", "$1");
                        }
                    }
                }
            });
        } else ret = null;
        return ret;
    }

    private static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    private static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public void setDuration(float duration) {
        this.duration = duration;
        trimStart = 0;
        trimEnd = duration;
    }
}
