package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("pos")
public class PosFact {
    @Param(0) private int p;
    public PosFact() {}
    public PosFact(int p) { this.p = p; }
    public int getP() { return p; }
    public void setP(int p) { this.p = p; }
    @Override public String toString(){ return "pos(" + p + ")"; }
}