package team.itis.ivm.data;

public class Sound {

    public String path;
    public String id;
    public int start;
    public int end;

    public Sound(String path, int start, int end) {
        this.path = path;
        this.start = start;
        this.end = end;
    }
}