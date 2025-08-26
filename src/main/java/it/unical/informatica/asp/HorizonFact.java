package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("horizon")
public class HorizonFact {
    @Param(0)
    private int horizon;

    public HorizonFact() {}

    public HorizonFact(int horizon) {
        this.horizon = horizon;
    }

    public int getHorizon() { return horizon; }
    public void setHorizon(int horizon) { this.horizon = horizon; }

    @Override
    public String toString() {
        return String.format("horizon(%d).", horizon);
    }
}