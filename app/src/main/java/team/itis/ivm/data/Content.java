package team.itis.ivm.data;

public class Content {

    public String path;
    public String id;
    public boolean isVideo;
    public int start;
    public int end;

    public Content(String path, boolean isVideo, int start, int end) {
        this.path = path;
        this.isVideo = isVideo;
        this.start = start;
        this.end = end;
    }
}
