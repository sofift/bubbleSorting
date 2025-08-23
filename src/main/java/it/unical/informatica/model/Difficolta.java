package it.unical.informatica.model;

public enum Difficolta {
    FACILE(6, 4, 4, 5),       // 6 tubi, 4 colori, capacità 4, 5 livelli
    MEDIO(7, 5, 4, 5),     // 7 tubi, 5 colori
    DIFFICILE(9, 7, 4, 5);       // 9 tubi, 7 colori

    public final int tubes;       // T
    public final int colors;      // C
    public final int capacity;    // K (tipicamente 4)
    public final int levels;      // 5 livelli per modalità

    Difficolta(int tubes, int colors, int capacity, int levels) {
        this.tubes = tubes;
        this.colors = colors;
        this.capacity = capacity;
        this.levels = levels;
    }

    public int getTubes() {
        return tubes;
    }

    public int getColors() {
        return colors;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getLevels() {
        return levels;
    }

    public int getEmptyTubes() {
        return 0; // --> dipende dal numero di tubi totali
    }


    @Override
    public String toString() {
        switch (this) {
            case FACILE: return "Facile";
            case MEDIO: return "Medio";
            case DIFFICILE: return "Difficile";
            default: return name();
        }
    }
}