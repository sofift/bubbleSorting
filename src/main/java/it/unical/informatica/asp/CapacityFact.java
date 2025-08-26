package it.unical.informatica.asp;

import it.unical.mat.embasp.languages.Id;
import it.unical.mat.embasp.languages.Param;

@Id("capacity")
public class CapacityFact {
    @Param(0)
    private int capacity;

    public CapacityFact() {}

    public CapacityFact(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    @Override
    public String toString() {
        return String.format("capacity(%d).", capacity);
    }
}