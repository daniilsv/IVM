package team.itis.ivm.data;

public class Text {
    public String text;
    public String coordX;
    public String coordY;
    public int fontSize;
    public String color;

    public Text(String text, int fontSize, String coordX, String coordY) {
        this.text = text;
        this.coordX = coordX;
        this.coordY = coordY;
        this.fontSize = fontSize;
        this.color = "red";
    }
}