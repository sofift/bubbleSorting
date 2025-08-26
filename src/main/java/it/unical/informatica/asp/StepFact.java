package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("step")
public class StepFact {
    @Param(0) private int s;
    public StepFact() {}
    public StepFact(int s) { this.s = s; }
    public int getS() { return s; }
    public void setS(int s) { this.s = s; }
    @Override public String toString(){ return "step(" + s + ")"; }
}
