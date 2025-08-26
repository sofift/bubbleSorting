package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("num_tubes")
public  class NumTubesFact {
    @Param(0)
    private int numTubes;

    public NumTubesFact() {}

    public NumTubesFact(int numTubes) {
        this.numTubes = numTubes;
    }

    public int getNumTubes() { return numTubes; }
    public void setNumTubes(int numTubes) { this.numTubes = numTubes; }

    @Override
    public String toString() {
        return String.format("num_tubes(%d).", numTubes);
    }
}