package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.*;
import it.unical.mat.embasp.languages.asp.*;

@Id("show_move")
public class ShowMove {

    @Param(0)
    private int from;

    @Param(1)
    private int to;

    @Param(2)
    private int step;

    public ShowMove() {} // obbligatorio

    public ShowMove(int from, int to, int step) {
        this.from = from;
        this.to = to;
        this.step = step;
    }

    public int getFrom() { return from; }
    public void setFrom(int from) { this.from = from; }

    public int getTo() { return to; }
    public void setTo(int to) { this.to = to; }

    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }

    @Override public String toString() {
        return "show_move(" + from + "," + to + "," + step + ")";
    }
}
