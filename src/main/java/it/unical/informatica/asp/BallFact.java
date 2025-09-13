package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("init_ball")
public class BallFact {
    @Param(0)
    private int tube;
    @Param(1)
    private int position;
    @Param(2)
    private String color;

    public BallFact() {}

    public BallFact(int tube, int position, String color) {
        this.tube = tube;
        this.position = position;
        this.color = color;
    }

    public int getTube() { return tube; }
    public void setTube(int tube) { this.tube = tube; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    @Override
    public String toString() {
        return String.format("init_ball(%d,%d,%s)", tube, position, color);
    }
}