package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("tube")
public  class TubeFact {
    @Param(0)
    private int tube;

    public TubeFact() {}

    public TubeFact(int tube) {
        this.tube = tube;
    }

    public int getTube() { return tube; }
    public void setTube(int tube) { this.tube = tube; }

    @Override
    public String toString() {
        return String.format("tube(%d).", tube);
    }
}