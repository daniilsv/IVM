package team.itis.ivm.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import team.itis.ivm.data.Text;

public class FFmpegCommand {
    private String command = "";
    private ExecuteBinaryResponseHandler handler = new ExecuteBinaryResponseHandler();

    private FFmpegCommand() {
    }

    @NonNull
    public static Builder newBuilder() {
        return new FFmpegCommand().new Builder();
    }

    public static String generateDrawTextFilters(Context context, ArrayList<Text> texts) {

        Random r = new Random();

        ArrayList<String> ret = new ArrayList<>();
        for (Text text : texts) {
            String tmpComplexFilterFile = context.getExternalCacheDir().getAbsolutePath() + "/tmp_text_" + r.nextLong() + ".txt";
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(tmpComplexFilterFile));
                bw.write(text.text);
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ret.add("drawtext=fontsize=" + text.fontSize + ":" +
                    "fontfile=/sdcard/plain.otf:" +
                    "fontcolor=" + text.color + ":" +
                    "textfile=" + tmpComplexFilterFile + ":" +
                    "y=" + text.coordY + ":" +
                    "x=" + text.coordX);
        }
        return TextUtils.join(",", ret);
    }

    public ExecuteBinaryResponseHandler getHandler() {
        return handler;
    }

    public FFmpegCommand setHandler(ExecuteBinaryResponseHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public String toString() {
        return command;
    }

    public class Builder {
        private ArrayList<String> mParams = new ArrayList<>();
        private ArrayList<String> mVideoFilters = new ArrayList<>();
        private ArrayList<String> mComplexNodes = new ArrayList<>();
        private ArrayList<String> mMap = new ArrayList<>();

        private String output = "";
        private String input = "";
        private String mVcodec = "mpeg4";
        private String mAcodec = "copy";
        private String mAr = "44100";
        private String mFrameRate = "24";

        public Builder addParam(String key, String value) {
            if (value == null)
                mParams.add(key);
            else
                mParams.add(key + " " + value);
            return this;
        }

        public Builder addVideoFilter(String value) {
            mVideoFilters.add(value);
            return this;
        }

        public Builder addComplexFilter(String... filters) {
            StringBuilder value = new StringBuilder();
            for (String filter : filters) {
                if (filter.contains("]"))
                    value.append(filter);
                else {
                    if (value.length() != 0 && value.charAt(value.length() - 1) != ']')
                        value.append(",");
                    value.append(filter);
                }
            }

            mComplexNodes.add(value.toString());
            return this;
        }

        public Builder addInput(String input) {
            addParam("-i", input);
            return this;
        }

        public Builder setOutput(String output) {
            this.output = output;
            return this;
        }

        public Builder setVideoCodec(String codec) {
            mVcodec = codec;
            return this;
        }

        public Builder setAudioCodec(String codec) {
            mAcodec = codec;
            return this;
        }

        public Builder addMap(String map) {
            mMap.add(map);
            return this;
        }

        public FFmpegCommand build(Context context) {
            if (mComplexNodes.size() > 0) {
                String videoComplexFilters = TextUtils.join(";\n", mComplexNodes);
                try {
                    String tmpComplexFilterFile = context.getExternalCacheDir().getAbsolutePath() + "/tmp_complex_" + System.currentTimeMillis() + ".txt";
                    BufferedWriter bw = new BufferedWriter(new FileWriter(tmpComplexFilterFile));
                    bw.write(videoComplexFilters);
                    bw.flush();
                    bw.close();
                    addParam("-filter_complex_script", tmpComplexFilterFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mVideoFilters.size() > 0) {
                String videoFilters = TextUtils.join(",", mVideoFilters);
                addParam("-vf", videoFilters);
            }

            for (String map : mMap) {
                addParam("-map", map);
            }

            this.addParam("-vcodec", mVcodec)
                    //.addParam("-acodec", mAcodec)
                    .addParam("-ar", mAr)
                    .addParam("-r", mFrameRate);

            addParam(output, null);

            command = TextUtils.join(" ", mParams);
            System.out.println(command);
            return FFmpegCommand.this;
        }
    }
}
