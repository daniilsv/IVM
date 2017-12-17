package team.itis.ivm.data;

public class Sound {

    public String path;
    public String id;
    public float start;
    public float end;

    public Sound(String path, float start, float end) {
        this.path = path;
        this.start = start;
        this.end = end;
    }
}