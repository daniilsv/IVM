package team.itis.ivm.helpers;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;

public class FFmpegCommand {
    private String command = "";

    private FFmpegCommand() {
    }

    @NonNull
    public static Builder newBuilder() {
        return new FFmpegCommand().new Builder();
    }

    @Override
    public String toString() {
        return command;
    }

    public class Builder {
        private ArrayList<String> mParams = new ArrayList<>();
        private ArrayList<String> mVideoFilters = new ArrayList<>();
        private String output = "";
        private String input = "";
        private String mVcodec = "mpeg4";
        private String mAcodec = "copy";
        private String mAr = "48000";
        private String mAb = "192k";

        public Builder addParam(String key, String value) {
            if (key == null)
                mParams.add(value);
            else
                mParams.add(key + " " + value);
            return this;
        }

        public Builder addVideoFilter(String key, String[] value) {
            mVideoFilters.add(key + "=" + TextUtils.join(":", value));
            return this;
        }

        public Builder setInput(String input) {
            this.input = input;
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

        public FFmpegCommand build() {
            String videoFilters = TextUtils.join(",", mVideoFilters);

            this.addParam("-vcodec", mVcodec)
                    .addParam("-acodec", mAcodec)
                    .addParam("-ar", mAr)
                    .addParam("-ab", mAb)
                    .addParam("-vf", videoFilters)
                    .addParam(null, output);

            command = TextUtils.join(" ", mParams);
            return FFmpegCommand.this;
        }
    }
}
