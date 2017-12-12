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

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;

import java.io.File;

import eu.davidea.flipview.FlipView;
import team.itis.ivm.BuildConfig;
import team.itis.ivm.R;
import team.itis.ivm.helpers.FFmpegCommand;
import team.itis.ivm.helpers.FFmpegHelper;

public class ProcessFragment extends Fragment {

    String output_file;

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

    private FFmpegCommand createCommand() {
        File movies_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        movies_dir.mkdirs();
        output_file = movies_dir.getPath() + "/output" + System.currentTimeMillis() + ".mp4";
        return FFmpegCommand.newBuilder()
                .addInput("/sdcard/video1.mp4")
                .addInput("/sdcard/video2.mp4")
                .addInput("/sdcard/audio1.mp3")
                .addComplexFilter(
                        new String[]{"[0:v]"},
                        new String[]{"crop",
                                "720",
                                "720"},
                        new String[]{"split",
                                "2"
                        },
                        new String[]{"[input1a][input1b]"}
                )
                .addComplexFilter(
                        new String[]{"[input1a]"},
                        new String[]{"trim",
                                "start=2",
                                "end=10"
                        },
                        new String[]{"setpts",
                                "PTS-STARTPTS"
                        },
                        new String[]{"[clip1]"}
                )
                .addComplexFilter(
                        new String[]{"[input1b]"},
                        new String[]{"trim",
                                "start=10",
                                "end=12"
                        },
                        new String[]{"setpts",
                                "PTS-STARTPTS"
                        },
                        new String[]{"[clip1fadeoutsource]"}
                )

                .addComplexFilter(
                        new String[]{"[1:v]"},
                        new String[]{"crop",
                                "720",
                                "720"},
                        new String[]{"split",
                                "2"
                        },
                        new String[]{"[input2a][input2b]"}
                )
                .addComplexFilter(
                        new String[]{"[input2a]"},
                        new String[]{"trim",
                                "start=0",
                                "end=2"
                        },
                        new String[]{"setpts",
                                "PTS-STARTPTS"
                        },
                        new String[]{"[clip2fadeinsource]"}
                )
                .addComplexFilter(
                        new String[]{"[input2b]"},
                        new String[]{"trim",
                                "start=2"
                        },
                        new String[]{"setpts",
                                "PTS-STARTPTS"
                        },
                        new String[]{"[clip2]"}
                )

                .addComplexFilter(
                        new String[]{"[clip1fadeoutsource]"},
                        new String[]{"format",
                                "pix_fmts=yuva420p"
                        },
                        new String[]{"fade",
                                "t=out",
                                "st=0",
                                "d=2",
                                "alpha=1"
                        },
                        new String[]{"[clip1fadeout]"}
                )
                .addComplexFilter(
                        new String[]{"[clip2fadeinsource]"},
                        new String[]{"format",
                                "pix_fmts=yuva420p"
                        },
                        new String[]{"fade",
                                "t=in",
                                "st=0",
                                "d=2",
                                "alpha=1"
                        },
                        new String[]{"[clip2fadein]"}
                )
                .addComplexFilter(
                        new String[]{"[clip1fadeout]"},
                        new String[]{"fifo"},
                        new String[]{"[clip1fadeoutfifo]"}
                )
                .addComplexFilter(
                        new String[]{"[clip2fadein]"},
                        new String[]{"fifo"},
                        new String[]{"[clip2fadeinfifo]"}
                )
                .addComplexFilter(
                        new String[]{"[clip1fadeoutfifo]"},
                        new String[]{"[clip2fadeinfifo]"},
                        new String[]{"overlay"},
                        new String[]{"[clip1to2crossfade]"}
                )
                .addComplexFilter(
                        new String[]{"[clip1]"},
                        new String[]{"[clip1to2crossfade]"},
                        new String[]{"[clip2]"},
                        new String[]{"concat",
                                "n=3"
                        },
                        new String[]{"drawtext",
                                "fontsize=32",
                                "fontfile=/sdcard/plain.otf",
                                "fontcolor=red",
                                "textfile=/sdcard/text.txt",
                                "y=h-15*t",
                                "x=w-75*t"
                        },
                        new String[]{"drawtext",
                                "fontsize=32",
                                "fontfile=/sdcard/cour.ttf",
                                "fontcolor=blue",
                                "textfile=/sdcard/text.txt",
                                "y=15*t",
                                "x=50*t"
                        },
                        new String[]{"[output]"}
                )
                .addMap("[output]")
                .addMap("2")
                .setVideoCodec("mpeg4")
                .addParam("-vtag", "xvid")
                .addParam("-strict", "experimental")
                .addParam("-qscale:v", "3")
                .addParam("-r", "30")
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