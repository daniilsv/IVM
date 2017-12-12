package team.itis.ivm.data;

public class Text {
    public String text;
    public int coordX;
    public int coordY;
    public int size;
    public int start;
    public int end;

    public Text(String text, int coordX, int coordY, int size, int start, int end) {
        this.text = text;
        this.coordX = coordX;
        this.coordY = coordY;
        this.size = size;
        this.start = start;
        this.end = end;
    }
}