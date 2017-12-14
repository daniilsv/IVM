package team.itis.ivm.data;

public class Content {

    public String path;
    public boolean isVideo;
    public int start;
    public int end;
    public int duration;
    public String resolution;

    public Content() {
        isVideo = false;
        duration = -1;
        resolution = null;
    }

    public Content(String path, boolean isVideo, int start, int end) {
        this.path = path;
        this.isVideo = isVideo;
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return path + " " + (isVideo ? "V" : "I") + " " + duration + " " + resolution;
    }
}
